import { BehaviorSubject, Subject, of, EMPTY, throwError } from 'rxjs';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { NotePermission } from '@notes/notes_service';
import { NoteService } from './note.service';
import { Note } from '../../types/note';

describe('NoteService', () => {
  let notesApi: any;
  let service: NoteService;
  let noteEventsService: any;
  let authService: any;

  const createNote = (overrides: Partial<Note> = {}): Note => ({
    id: '1',
    authorId: 'auth-1',
    title: 'Title',
    content: 'Content',
    created: new Date().toISOString() as any,
    updated: new Date().toISOString() as any,
    pinned: false,
    shared: false,
    canEdit: true, // Default to true as per expectations in many tests
    ...overrides,
  });

  beforeEach(() => {
    notesApi = {
      searchNotes: vi.fn(),
      getPermissions: vi.fn().mockReturnValue(of([])),
      updateNote: vi.fn(),
      createNote: vi.fn(),
      deleteNote: vi.fn(),
      setNotePermissions: vi.fn(),
      removeNoteAccess: vi.fn(),
    };
    noteEventsService = {
      noteEvents$: new Subject(),
      noteUpdatedEvents$: new Subject(),
      noteDeletedEvents$: new Subject(),
    };
    authService = {
      getCurrentUserValue: vi.fn().mockReturnValue({ id: 'auth-1' }),
      logged$: new BehaviorSubject(true),
    };

    notesApi.searchNotes.mockReturnValue(of([])); // Default fallback

    service = new NoteService(notesApi as any, authService as any, noteEventsService as any);
  });

  it('loads notes when logged$ is true', () => {
    const authLoggedSubject = new BehaviorSubject<boolean>(false);
    authService.logged$ = authLoggedSubject;
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    authLoggedSubject.next(true);

    expect(notesApi.searchNotes).toHaveBeenCalled();
  });

  it('clears notes when logged$ is false', () => {
    const authLoggedSubject = new BehaviorSubject<boolean>(true);
    authService.logged$ = authLoggedSubject;
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    // Manually add a note to the subject
    (service as any).notesSubject.next([createNote({ id: '1' })]);

    authLoggedSubject.next(false);

    let latestNotes: Note[] = [];
    service.notes$.subscribe((notes) => {
      latestNotes = notes;
    });

    expect(latestNotes).toEqual([]);
  });

  it('loads notes from API stream', () => {
    const notesSubject = new BehaviorSubject<Note[]>([]);
    notesApi.searchNotes.mockReturnValue(notesSubject);
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);
    let latestNotes: Note[] = [];

    service.notes$.subscribe((notes) => {
      latestNotes = notes;
    });

    const nextNotes = [createNote({ id: '1', pinned: true }), createNote({ id: '2' })];
    notesSubject.next(nextNotes);

    expect(notesApi.searchNotes).toHaveBeenCalled();
    expect(latestNotes[0].id).toBe('1');
  });

  it('updateNote should add note when not present (idx === -1)', () => {
    const notesSubject = new BehaviorSubject<Note[]>([createNote({ id: '1' })]);
    notesApi.searchNotes.mockReturnValue(notesSubject);
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    const newNote = createNote({ id: '2', title: 'New' });
    // mock updateNote to return the new note
    (notesApi as any).updateNote = vi.fn().mockReturnValue(of(newNote));

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    return new Promise<void>((resolve) => {
      service.updateNote('2', { title: 'New' } as any).subscribe(() => {
        expect((notesApi as any).updateNote).toHaveBeenCalledWith({ title: 'New' }, '2');
        // new note should be prepended
        expect(latest[0].id).toBe('2');
        resolve();
      });
    });
  });

  it('updateNote should replace existing note when present (idx !== -1)', () => {
    const existing = createNote({ id: '1', title: 'Old' });
    const notesSubject = new BehaviorSubject<Note[]>([existing]);
    notesApi.searchNotes.mockReturnValue(notesSubject);
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    const updated = createNote({ id: '1', title: 'Updated' });
    (notesApi as any).updateNote = vi.fn().mockReturnValue(of(updated));

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    return new Promise<void>((resolve) => {
      service.updateNote('1', { title: 'Updated' } as any).subscribe(() => {
        expect((notesApi as any).updateNote).toHaveBeenCalledWith({ title: 'Updated' }, '1');
        // existing note should be replaced
        expect(latest.length).toBe(1);
        expect(latest[0].title).toBe('Updated');
        resolve();
      });
    });
  });

  it('createNote + same SSE event should keep a single note', () => {
    const events = new Subject<any>();
    authService.logged$ = of(true);
    const service = new NoteService(
      {
        ...notesApi,
        searchNotes: vi.fn().mockReturnValue(of([])),
        createNote: vi.fn(),
      } as any,
      authService as any,
      { noteEvents$: events, noteUpdatedEvents$: EMPTY, noteDeletedEvents$: EMPTY } as any,
    );

    const created = createNote({ id: 'sse-1', title: 'SSE' });
    (service as any).notesApi.createNote.mockReturnValue(of(created));

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    return new Promise<void>((resolve) => {
      service.createNote({ title: 'SSE' } as any).subscribe(() => {
        events.next({ payload: created });
        expect(latest).toHaveLength(1);
        expect(latest[0].id).toBe('sse-1');
        resolve();
      });
    });
  });

  it('ignores SSE events without payload', () => {
    const events = new Subject<any>();
    const initial = [createNote({ id: '1' })];
    authService.logged$ = of(true);
    const service = new NoteService(
      {
        ...notesApi,
        searchNotes: vi.fn().mockReturnValue(of(initial)),
      } as any,
      authService as any,
      { noteEvents$: events, noteUpdatedEvents$: EMPTY, noteDeletedEvents$: EMPTY } as any,
    );

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    events.next({});
    events.next(null);

    expect(latest.map(n => n.id)).toEqual(['1']);
  });

  it('updateNote replaces only the matching note in a multi-note list', () => {
    const first = createNote({ id: '1', title: 'First' });
    const second = createNote({ id: '2', title: 'Second' });
    notesApi.searchNotes.mockReturnValue(new BehaviorSubject<Note[]>([first, second]));
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    const updatedSecond = createNote({ id: '2', title: 'Updated second' });
    (notesApi as any).updateNote = vi.fn().mockReturnValue(of(updatedSecond));

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    return new Promise<void>((resolve) => {
      service.updateNote('2', { title: 'Updated second' } as any).subscribe(() => {
        expect(latest).toHaveLength(2);
        expect(latest[0].title).toBe('First');
        expect(latest[1].title).toBe('Updated second');
        resolve();
      });
    });
  });

  it('deleteNote removes the note from the subject after API success', () => {
    const first = createNote({ id: '1' });
    const second = createNote({ id: '2' });
    notesApi.searchNotes.mockReturnValue(of([first, second]));
    (notesApi as any).deleteNote = vi.fn().mockReturnValue(of(undefined));
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    return new Promise<void>((resolve, reject) => {
      service.deleteNote('1').subscribe({
        next: () => {
          try {
            expect((notesApi as any).deleteNote).toHaveBeenCalledWith('1');
            expect(latest.map(n => n.id)).toEqual(['2']);
            resolve();
          } catch (e) {
            reject(e);
          }
        },
        error: reject
      });
    });
  });

  it('removes note when delete SSE event arrives', () => {
    const first = createNote({ id: '1' });
    const second = createNote({ id: '2' });
    notesApi.searchNotes.mockReturnValue(of([first, second]));
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    noteEventsService.noteDeletedEvents$.next({ payload: { id: '1' } });

    expect(latest.map(n => n.id)).toEqual(['2']);
  });

  it('ignores delete SSE events without payload id', () => {
    const initial = [createNote({ id: '1' }), createNote({ id: '2' })];
    notesApi.searchNotes.mockReturnValue(of(initial));
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    noteEventsService.noteDeletedEvents$.next({});
    noteEventsService.noteDeletedEvents$.next({ payload: {} });
    noteEventsService.noteDeletedEvents$.next(null);

    expect(latest.map(n => n.id)).toEqual(['1', '2']);
  });

  it('upserts note from SSE updated event when payload exists', () => {
    const initial = [createNote({ id: '1', title: 'Initial' })];
    notesApi.searchNotes.mockReturnValue(new BehaviorSubject<Note[]>(initial));
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    const updated = createNote({ id: '1', title: 'Updated via SSE' });
    noteEventsService.noteUpdatedEvents$.next({ payload: updated });

    expect(latest).toHaveLength(1);
    expect(latest[0].title).toBe('Updated via SSE');
  });

  it('adds new note from SSE updated event if not present in list', () => {
    const initial = [createNote({ id: '1' })];
    notesApi.searchNotes.mockReturnValue(new BehaviorSubject<Note[]>(initial));
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    const newNote = createNote({ id: '2', title: 'New from SSE' });
    noteEventsService.noteUpdatedEvents$.next({ payload: newNote });

    expect(latest).toHaveLength(2);
    expect(latest[0].id).toBe('2');
    expect(latest[1].id).toBe('1');
  });

  it('ignores SSE updated events without payload', () => {
    const initial = [createNote({ id: '1' })];
    notesApi.searchNotes.mockReturnValue(of(initial));
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    noteEventsService.noteUpdatedEvents$.next({});
    noteEventsService.noteUpdatedEvents$.next(null);
    noteEventsService.noteUpdatedEvents$.next({ payload: undefined });

    expect(latest.map(n => n.id)).toEqual(['1']);
  });

  it('updates multiple notes in correct positions from SSE updated events', () => {
    const first = createNote({ id: '1', title: 'First' });
    const second = createNote({ id: '2', title: 'Second' });
    const third = createNote({ id: '3', title: 'Third' });
    notesApi.searchNotes.mockReturnValue(new BehaviorSubject<Note[]>([first, second, third]));
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    const updatedSecond = createNote({ id: '2', title: 'Updated Second' });
    noteEventsService.noteUpdatedEvents$.next({ payload: updatedSecond });

    expect(latest).toHaveLength(3);
    expect(latest[1].title).toBe('Updated Second');
    expect(latest[0].title).toBe('First');
    expect(latest[2].title).toBe('Third');
  });

  it('getPermissions delegates to notesApi.getPermissions', () => {
    notesApi.searchNotes.mockReturnValue(of([]));
    notesApi.getPermissions.mockReturnValue(of([]));
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    service.getPermissions('note-1').subscribe();

    expect(notesApi.getPermissions).toHaveBeenCalledWith('note-1');
  });

  it('setNotePermissions uses READ default permission', () => {
    notesApi.searchNotes.mockReturnValue(of([]));
    notesApi.setNotePermissions.mockReturnValue(of({}));
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    service.setNotePermissions('note-2', 'user@example.com').subscribe();

    expect(notesApi.setNotePermissions).toHaveBeenCalledWith(
      { email: 'user@example.com', permissions: [NotePermission.READ] },
      'note-2',
    );
  });

  it('removeNoteAccess delegates to notesApi.removeNoteAccess', () => {
    notesApi.searchNotes.mockReturnValue(of([]));
    notesApi.removeNoteAccess.mockReturnValue(of({}));
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);

    service.removeNoteAccess('note-3', 'target-1').subscribe();

    expect(notesApi.removeNoteAccess).toHaveBeenCalledWith('note-3', 'target-1');
  });

  it('updates permissions for a note correctly', () => {
    const noteId = '123';
    notesApi.getPermissions.mockReturnValue(of([{ userId: 'auth-1', permissions: [NotePermission.EDIT] }]));

    // initially unshared
    (service as any).notesSubject.next([createNote({ id: noteId, shared: false, canEdit: false })]);

    (service as any).loadPermissionsForNote(noteId);

    const note = (service as any).notesSubject.value[0];
    expect(note.shared).toBe(true);
    expect(note.canEdit).toBe(true);
  });

  it('handles errors when loading permissions for a note', () => {
    const noteId = 'error-note';
    notesApi.getPermissions.mockReturnValue(throwError(() => new Error('API Error')));
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    (service as any).notesSubject.next([createNote({ id: noteId, shared: true, canEdit: true })]);

    (service as any).loadPermissionsForNote(noteId);

    const note = (service as any).notesSubject.value[0];
    expect(note.shared).toBe(false);
    expect(note.canEdit).toBe(false);
    expect(errorSpy).toHaveBeenCalled();
    errorSpy.mockRestore();
  });

  it('refreshPermissions triggers single note permission load', () => {
    const loadSpy = vi.spyOn(service as any, 'loadPermissionsForNote');
    service.refreshPermissions('456');
    expect(loadSpy).toHaveBeenCalledWith('456');
  });
});
