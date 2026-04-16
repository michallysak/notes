import { BehaviorSubject, Subject, of } from 'rxjs';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { NoteService } from './note.service';
import { Note } from '../../types/note';

describe('NoteService', () => {
  const notesApi = {
    getNotes: vi.fn(),
  };
  let noteEvents$: Subject<any>;
  let noteEventsService: { noteEvents$: Subject<any> };

  beforeEach(() => {
    notesApi.getNotes.mockReset();
    noteEvents$ = new Subject<any>();
    noteEventsService = { noteEvents$ };
  });

  const createNote = (overrides: Partial<Note> = {}): Note => ({
    id: '1',
    title: 'Title',
    content: 'Content',
    pinned: false,
    created: new Date('2026-01-01T10:00:00Z'),
    updated: undefined,
    ...overrides,
  });

  it('loads notes from API stream', () => {
    const notesSubject = new BehaviorSubject<Note[]>([]);
    notesApi.getNotes.mockReturnValue(notesSubject);
    const service = new NoteService(notesApi as any, noteEventsService as any);
    let latestNotes: Note[] = [];

    service.notes$.subscribe((notes) => {
      latestNotes = notes;
    });

    const nextNotes = [createNote({ id: '1', pinned: true }), createNote({ id: '2' })];
    notesSubject.next(nextNotes);

    expect(notesApi.getNotes).toHaveBeenCalled();
    expect(latestNotes).toEqual(nextNotes);
  });

  it('updateNote should add note when not present (idx === -1)', () => {
    const notesSubject = new BehaviorSubject<Note[]>([createNote({ id: '1' })]);
    notesApi.getNotes.mockReturnValue(notesSubject);
    const service = new NoteService(notesApi as any, noteEventsService as any);

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
    notesApi.getNotes.mockReturnValue(notesSubject);
    const service = new NoteService(notesApi as any, noteEventsService as any);

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
    const service = new NoteService(
      {
        ...notesApi,
        getNotes: vi.fn().mockReturnValue(of([])),
        createNote: vi.fn(),
      } as any,
      { noteEvents$: events } as any,
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
    const service = new NoteService(
      {
        ...notesApi,
        getNotes: vi.fn().mockReturnValue(of(initial)),
      } as any,
      { noteEvents$: events } as any,
    );

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    events.next({});
    events.next(null);

    expect(latest).toEqual(initial);
  });

  it('updateNote replaces only the matching note in a multi-note list', () => {
    const first = createNote({ id: '1', title: 'First' });
    const second = createNote({ id: '2', title: 'Second' });
    notesApi.getNotes.mockReturnValue(new BehaviorSubject<Note[]>([first, second]));
    const service = new NoteService(notesApi as any, noteEventsService as any);

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
    notesApi.getNotes.mockReturnValue(new BehaviorSubject<Note[]>([first, second]));
    (notesApi as any).deleteNote = vi.fn().mockReturnValue(of(undefined));
    const service = new NoteService(notesApi as any, noteEventsService as any);

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    return new Promise<void>((resolve) => {
      service.deleteNote('1').subscribe(() => {
        expect((notesApi as any).deleteNote).toHaveBeenCalledWith('1');
        expect(latest).toEqual([second]);
        resolve();
      });
    });
  });
});

