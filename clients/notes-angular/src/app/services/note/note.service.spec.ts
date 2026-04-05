import { BehaviorSubject, of } from 'rxjs';
import { NoteService } from './note.service';
import { Note } from '../../types/note';

describe('NoteService', () => {
  const notesApi = {
    getNotes: vi.fn(),
  };

  beforeEach(() => {
    notesApi.getNotes.mockReset();
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
    const service = new NoteService(notesApi as any);
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
    const service = new NoteService(notesApi as any);

    const newNote = createNote({ id: '2', title: 'New' });
    // mock updateNote to return the new note
    (notesApi as any).updateNote = vi.fn().mockReturnValue(of(newNote));

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    return new Promise<void>((resolve) => {
      service.updateNote('2', { title: 'New' } as any).subscribe(() => {
        expect((notesApi as any).updateNote).toHaveBeenCalledWith('2', { title: 'New' });
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
    const service = new NoteService(notesApi as any);

    const updated = createNote({ id: '1', title: 'Updated' });
    (notesApi as any).updateNote = vi.fn().mockReturnValue(of(updated));

    let latest: Note[] = [];
    service.notes$.subscribe((n) => (latest = n));

    return new Promise<void>((resolve) => {
      service.updateNote('1', { title: 'Updated' } as any).subscribe(() => {
        expect((notesApi as any).updateNote).toHaveBeenCalledWith('1', { title: 'Updated' });
        // existing note should be replaced
        expect(latest.length).toBe(1);
        expect(latest[0].title).toBe('Updated');
        resolve();
      });
    });
  });
});

