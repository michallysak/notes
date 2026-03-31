import { BehaviorSubject } from 'rxjs';
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
});

