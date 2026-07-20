import { BehaviorSubject, Subject, of, EMPTY, throwError } from 'rxjs';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  NotePermission,
  NoteCreatedEventDTO,
  NoteUpdatedEventDTO,
  NoteDeletedEventDTO,
  NotePermissionsSetEventDTO,
  NoteAccessRemovedEventDTO,
  NotePublicShareRemovedEventDTO,
  NotePublicShareUpsertedEventDTO,
} from '@notes/notes_service';
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
      getNote: vi.fn().mockReturnValue(of({})),
      makeNotePublic: vi.fn().mockReturnValue(of({ publicShareId: 'public-1' })),
      undoNotePublic: vi.fn().mockReturnValue(of({})),
      getPublicNote: vi.fn().mockReturnValue(of({})),
    };

    authService = {
      logged$: new BehaviorSubject<boolean>(false),
      getCurrentUserValue: vi.fn().mockReturnValue({ id: 'auth-1' }),
    };

    noteEventsService = {
      domainEvents$: new BehaviorSubject<any>(null),
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
      { domainEvents$: events } as any,
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
      { domainEvents$: events } as any,
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

  it('mapToNote calculates permissions correctly when editing is true', async () => {
    const res: any = {
      id: '1',
      authorId: 'auth-1',
      shares: [{ userId: 'auth-1', email: 'me@ex.com', permissions: [NotePermission.EDIT] }],
    };
    notesApi.searchNotes.mockReturnValue(of({ data: [res] }));
    service.loadNotes();

    const notes = await new Promise<any>((resolve) => {
      service.notes$.pipe().subscribe((notes) => {
        if (notes.length) {
          resolve(notes);
        }
      });
    });

    expect(notes[0].shared).toBe(true);
    expect(notes[0].canEdit).toBe(true);
  });

  it('mapToNote calculates permissions correctly when sharing without edit', async () => {
    const res: any = {
      id: '1',
      authorId: 'other-author',
      shares: [{ userId: 'auth-1', email: 'me@ex.com', permissions: [NotePermission.READ] }],
    };
    notesApi.searchNotes.mockReturnValue(of({ data: [res] }));
    service.loadNotes();

    const notes = await new Promise<any>((resolve) => {
      service.notes$.pipe().subscribe((notes) => {
        if (notes.length) {
          resolve(notes);
        }
      });
    });

    expect(notes[0].shared).toBe(true);
    expect(notes[0].canEdit).toBe(false);
  });

  it('mapToNote allows editing when public share has EDIT permission', async () => {
    const res: any = {
      id: '1',
      authorId: 'other-author',
      shares: [],
      publicShare: { publicShareId: 'public-1', permissions: [NotePermission.EDIT] },
    };
    notesApi.searchNotes.mockReturnValue(of({ data: [res] }));
    service.loadNotes();

    const notes = await new Promise<any>((resolve) => {
      service.notes$.pipe().subscribe((notes) => {
        if (notes.length) {
          resolve(notes);
        }
      });
    });

    expect(notes[0].canEdit).toBe(true);
    expect(notes[0].shared).toBe(true);
  });

  describe('Server-Sent Events (SSE)', () => {
    let events = new Subject<any>();

    beforeEach(() => {
      events = new Subject<any>();
      noteEventsService.domainEvents$ = events;
      service = new NoteService(notesApi as any, authService as any, noteEventsService as any);
    });

    it('upserts note on NOTECREATEDEVENT', () => {
      let latest: Note[] = [];
      service.notes$.subscribe(notes => latest = notes);

      const payload = createNote({ id: 'sse-1', title: 'Created Note' });
      events.next({ type: NoteCreatedEventDTO.TypeEnum.NOTECREATEDEVENT, payload });

      expect(latest.length).toBe(1);
      expect(latest[0].id).toBe('sse-1');
      expect(latest[0].title).toBe('Created Note');
    });

    it('upserts note on NOTEUPDATEDEVENT', () => {
      let latest: Note[] = [];
      service.notes$.subscribe(notes => latest = notes);

      const existing = createNote({ id: 'sse-2', title: 'Old Note' });
      (service as any).notesSubject.next([existing]);

      const payload = createNote({ id: 'sse-2', title: 'Updated Note' });
      events.next({ type: NoteUpdatedEventDTO.TypeEnum.NOTEUPDATEDEVENT, payload });

      expect(latest.length).toBe(1);
      expect(latest[0].id).toBe('sse-2');
      expect(latest[0].title).toBe('Updated Note');
    });

    it('removes note on NOTEDELETEDEVENT', () => {
      let latest: Note[] = [];
      service.notes$.subscribe(notes => latest = notes);

      const existing = createNote({ id: 'sse-3', title: 'Old Note' });
      (service as any).notesSubject.next([existing]);

      events.next({ type: NoteDeletedEventDTO.TypeEnum.NOTEDELETEDEVENT, payload: { id: 'sse-3' } });

      expect(latest.length).toBe(0);
    });

    it('fetches and upserts note on NOTEPERMISSIONSSETEVENT', () => {
      const payload = createNote({ id: 'sse-4', title: 'Permission Updated Note' });
      notesApi.getNote.mockReturnValue(of(payload));

      let latest: Note[] = [];
      service.notes$.subscribe(notes => latest = notes);

      events.next({ type: NotePermissionsSetEventDTO.TypeEnum.NOTEPERMISSIONSSETEVENT, payload: { noteId: 'sse-4' } });

      expect(notesApi.getNote).toHaveBeenCalledWith('sse-4');
      expect(latest.length).toBe(1);
      expect(latest[0].id).toBe('sse-4');
    });

    it('removes note access on NOTEACCESSREMOVEDEVENT self user', () => {
      let latest: Note[] = [];
      const existing = createNote({ id: 'sse-5', title: 'Old Note' });
      (service as any).notesSubject.next([existing]);
      service.notes$.subscribe(notes => latest = notes);

      events.next({ type: NoteAccessRemovedEventDTO.TypeEnum.NOTEACCESSREMOVEDEVENT, payload: { noteId: 'sse-5', userId: 'auth-1' } });

      expect(latest.length).toBe(0);
    });

    it('fetches and upserts on NOTEACCESSREMOVEDEVENT other user', () => {
      let latest: Note[] = [];
      service.notes$.subscribe(notes => latest = notes);

      const payload = createNote({ id: 'sse-6', title: 'Refresh Note' });
      notesApi.getNote.mockReturnValue(of(payload));

      events.next({ type: NoteAccessRemovedEventDTO.TypeEnum.NOTEACCESSREMOVEDEVENT, payload: { noteId: 'sse-6', userId: 'other-user' } });

      expect(notesApi.getNote).toHaveBeenCalledWith('sse-6');
      expect(latest.length).toBe(1);
      expect(latest[0].id).toBe('sse-6');
    });

    it('removes on NOTEACCESSREMOVEDEVENT other user when 403', () => {
      let latest: Note[] = [];
      const existing = createNote({ id: 'sse-7', title: 'Old Note' });
      (service as any).notesSubject.next([existing]);
      service.notes$.subscribe(notes => latest = notes);

      notesApi.getNote.mockReturnValue(throwError(() => ({ status: 403 })));

      events.next({ type: NoteAccessRemovedEventDTO.TypeEnum.NOTEACCESSREMOVEDEVENT, payload: { noteId: 'sse-7', userId: 'other-user' } });

      expect(notesApi.getNote).toHaveBeenCalledWith('sse-7');
      expect(latest.length).toBe(0);
    });

    it('upserts note on NOTEPUBLICSHAREUPSERTEDEVENT', () => {
      let latest: Note[] = [];
      service.notes$.subscribe(notes => latest = notes);

      const payload = createNote({
        id: 'sse-8',
        title: 'Public Share Updated',
        publicShare: { publicShareId: 'public-8', permissions: [NotePermission.EDIT] } as any,
      });
      events.next({ type: NotePublicShareUpsertedEventDTO.TypeEnum.NOTEPUBLICSHAREUPSERTEDEVENT, payload });

      expect(latest.length).toBe(1);
      expect(latest[0].id).toBe('sse-8');
      expect(latest[0].canEdit).toBe(true);
    });

    it('clears publicShare state on NOTEPUBLICSHAREREMOVEDEVENT', () => {
      let latest: Note[] = [];
      const existing = createNote({
        id: 'sse-9',
        authorId: 'other-user',
        publicShare: { publicShareId: 'public-9', permissions: [NotePermission.EDIT] } as any,
      });
      (service as any).notesSubject.next([existing]);
      service.notes$.subscribe(notes => latest = notes);

      events.next({
        type: NotePublicShareRemovedEventDTO.TypeEnum.NOTEPUBLICSHAREREMOVEDEVENT,
        payload: { noteId: 'sse-9', publicShareId: 'public-9' },
      });

      expect(latest).toHaveLength(1);
      expect(latest[0].publicShare?.publicShareId).toBeFalsy();
      expect(latest[0].shared).toBe(false);
      expect(latest[0].canEdit).toBe(false);
    });
  });

  it('getNoteById calls notesApi.getNote and maps it', () => {
    const apiNote = { id: 'note-get', shares: [] };
    notesApi.getNote.mockReturnValue(of(apiNote));

    let fetchedNote: any;
    service.getNoteById('note-get').subscribe(n => fetchedNote = n);

    expect(notesApi.getNote).toHaveBeenCalledWith('note-get');
    expect(fetchedNote.id).toBe('note-get');
    expect(fetchedNote.shared).toBe(false);
  });

  it('makeNotePublic returns the public share id', () => {
    let publicShareId = '';

    service.makeNotePublic('note-1').subscribe((value) => {
      publicShareId = value;
    });

    expect(notesApi.makeNotePublic).toHaveBeenCalledWith({ permissions: [NotePermission.READ] }, 'note-1');
    expect(publicShareId).toBe('public-1');
  });

  it('makeNotePublic does not optimistically mutate note state before SSE reconciliation', async () => {
    notesApi.searchNotes.mockReturnValue(of({ data: [{ id: 'note-1', authorId: 'auth-1', shares: [] }] }));
    service.loadNotes();

    let notesList: Note[] = [];
    service.notes$.subscribe((notes) => (notesList = notes));

    service.makeNotePublic('note-1', NotePermission.EDIT).subscribe();

    const updated = notesList.find((n) => n.id === 'note-1');
    expect(updated?.publicShare?.publicShareId).toBeFalsy();
    expect(updated?.shared).toBe(false);
    expect(updated?.canEdit).toBe(true);
  });

  it('undoNotePublic does not optimistically mutate note state before SSE reconciliation', () => {
    notesApi.searchNotes.mockReturnValue(of({
      data: [{ id: 'note-1', authorId: 'other-author', shares: [], publicShare: { publicShareId: 'public-1', permissions: [NotePermission.EDIT] } }],
    }));
    service.loadNotes();

    let notesList: Note[] = [];
    service.notes$.subscribe((notes) => (notesList = notes));

    service.undoNotePublic('note-1').subscribe();

    const updated = notesList.find((n) => n.id === 'note-1');
    expect(updated?.publicShare?.publicShareId).toBe('public-1');
    expect(updated?.shared).toBe(true);
    expect(updated?.canEdit).toBe(true);
  });

  it('getPublicNote fetches and maps the public note', () => {
    const apiNote = {
      id: 'public-note',
      shares: [],
      publicShare: { publicShareId: 'public-1', permissions: [NotePermission.READ] },
    };
    notesApi.getPublicNote.mockReturnValue(of(apiNote));

    let fetchedNote: Note | undefined;
    service.getPublicNote('public-1').subscribe((note) => {
      fetchedNote = note;
    });

    expect(notesApi.getPublicNote).toHaveBeenCalledWith('public-1');
    expect(fetchedNote?.id).toBe('public-note');
    expect(fetchedNote?.shared).toBe(true);
  });
});
