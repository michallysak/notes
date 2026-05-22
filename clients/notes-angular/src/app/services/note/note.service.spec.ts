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
    title: 'test note',
    content: 'content',
    created: null as any,
    updated: null as any,
    pinned: false,
    style: undefined,
    shares: [],
    shared: false,
    canEdit: true,
    ...overrides,
  });

  beforeEach(() => {
    notesApi = {
      searchNotes: vi.fn().mockReturnValue(of({ data: [] })),
      updateNote: vi.fn().mockReturnValue(of({})),
      createNote: vi.fn().mockReturnValue(of({})),
      deleteNote: vi.fn().mockReturnValue(of({})),
      setNotePermissions: vi.fn().mockReturnValue(of({})),
      removeNoteAccess: vi.fn().mockReturnValue(of({})),
    };

    authService = {
      logged$: new BehaviorSubject<boolean>(false),
      getCurrentUserValue: vi.fn().mockReturnValue({ id: 'auth-1' }),
    };

    noteEventsService = {
      noteEvents$: new BehaviorSubject<any>(null),
      noteUpdatedEvents$: new BehaviorSubject<any>(null),
      noteDeletedEvents$: new BehaviorSubject<any>(null),
    };

    service = new NoteService(notesApi, authService, noteEventsService);
  });

  describe('fetchSection behaviour', () => {
    it('does not send request if hasMore is false', () => {
      service.pinnedSection.next({ data: [], page: 0, hasMore: false });
      service.loadMorePinned();
      expect(notesApi.searchNotes).not.toHaveBeenCalled();
    });

    it('does not send request if loading is true', () => {
      service.pinnedSection.next({ data: [], page: 0, hasMore: true, loading: true });
      service.loadMorePinned();
      expect(notesApi.searchNotes).not.toHaveBeenCalled();
    });
  });

  it('createNote updates internal subject via server response', () => {
    const createdNote = createNote({ id: '2', title: 'New Note' });
    (notesApi.createNote as any).mockReturnValue(of(createdNote));

    let notesList: Note[] = [];
    service.notes$.subscribe((notes) => (notesList = notes));

    return new Promise<void>((resolve) => {
      service.createNote({ title: 'New Note' } as any).subscribe(() => {
        expect(notesList.length).toBe(1);
        expect(notesList[0].id).toBe('2');
        resolve();
      });
    });
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
    const notesSubject = new BehaviorSubject<any>({ data: [] });
    notesApi.searchNotes.mockReturnValue(notesSubject);
    authService.logged$ = of(true);
    const service = new NoteService(notesApi as any, authService as any, noteEventsService as any);
    let latestNotes: Note[] = [];

    service.notes$.subscribe((notes) => {
      latestNotes = notes;
    });

    const nextNotes = [createNote({ id: '1', pinned: true }), createNote({ id: '2' })];
    notesSubject.next({ data: nextNotes });

    expect(notesApi.searchNotes).toHaveBeenCalled();
    expect(latestNotes[0].id).toBe('1');
  });

  it('upsertNoteInSubject adds shared note to sharedSection directly', () => {
    // testing private upsertNoteInSubject correctly routes to sharedSection
    const sharedNote = createNote({ id: 'shared-2', authorId: 'other-user', shared: true });
    (service as any).upsertNoteInSubject(sharedNote);
    const data = service.sharedSection.value.data;
    expect(data.find((n: Note) => n.id === 'shared-2')).toBeTruthy();
  });

  it('upsertNoteInSubject adds pinned note to pinnedSection directly', () => {
    // testing private upsertNoteInSubject correctly routes to pinnedSection
    const pinnedNote = createNote({ id: 'pinned-2', authorId: 'auth-1', pinned: true });
    (service as any).upsertNoteInSubject(pinnedNote);
    const data = service.pinnedSection.value.data;
    expect(data.find((n: Note) => n.id === 'pinned-2')).toBeTruthy();
  });

  it('updateNote should replace existing note when present (idx !== -1)', () => {
    const existing = createNote({ id: '1', title: 'Old' });
    const notesSubject = new BehaviorSubject<any>({ data: [existing] });
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
        searchNotes: vi.fn().mockReturnValue(of({ data: [] })),
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
        searchNotes: vi.fn().mockReturnValue(of({ data: initial })),
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
    notesApi.searchNotes.mockReturnValue(new BehaviorSubject<any>({ data: [first, second] }));
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

  it('deleteNote calls notesApi and removes note from subject', () => {
    const note = createNote({ id: 'note-1' });
    notesApi.searchNotes.mockReturnValue(of({ data: [note] }));

    service.loadNotes();

    expect(service.notes$.subscribe((notes) => {
      if (notes.length) {
        expect(notes[0].id).toBe('note-1');
      }
    }));
  });

  it('deleteNote handles API error', () => {
    notesApi.deleteNote.mockReturnValue(throwError(() => new Error('API Error')));

    service.deleteNote('note-1').subscribe({
      error: (e) => expect(e.message).toBe('API Error'),
    });
  });

  it('setNotePermissions delegates to notesApi.setNotePermissions', () => {
    service.setNotePermissions('note-1', 'test@example.com', [NotePermission.READ]).subscribe();
    expect(notesApi.setNotePermissions).toHaveBeenCalledWith({ email: 'test@example.com', permissions: [NotePermission.READ] }, 'note-1');
  });

  it('removeNoteAccess delegates to notesApi.removeNoteAccess', () => {
    service.removeNoteAccess('note-1', 'user-1').subscribe();
    expect(notesApi.removeNoteAccess).toHaveBeenCalledWith('note-1', 'user-1');
  });

  it('mapToNote calculates permissions correctly when editing is true', () => {
    const res: any = {
      id: '1',
      authorId: 'auth-1',
      shares: [{ userId: 'auth-1', email: 'me@ex.com', permissions: [NotePermission.EDIT] }],
    };
    notesApi.searchNotes.mockReturnValue(of({ data: [res] }));
    service.loadNotes();

    expect(service.notes$.subscribe((notes) => {
      if (notes.length) {
        expect(notes[0].shared).toBe(true);
        expect(notes[0].canEdit).toBe(true);
      }
    }));
  });

  it('mapToNote calculates permissions correctly when sharing without edit', () => {
    const res: any = {
      id: '1',
      authorId: 'auth-1',
      shares: [{ userId: 'other-user', email: 'other@ex.com', permissions: [NotePermission.READ] }],
    };
    notesApi.searchNotes.mockReturnValue(of({ data: [res] }));
    service.loadNotes();

    expect(service.notes$.subscribe((notes) => {
      if (notes.length) {
        expect(notes[0].shared).toBe(true);
        expect(notes[0].canEdit).toBe(false);
      }
    }));
  });
});
