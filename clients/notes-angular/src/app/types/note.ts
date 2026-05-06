import { NoteResponse } from '@notes/notes_service';

export type Note = NoteResponse & {
  shared: boolean;
  canEdit: boolean;
};
