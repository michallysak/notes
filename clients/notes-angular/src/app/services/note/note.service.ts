import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { NotesAPIService, NoteResponse, NoteUpdateRequest } from '@notes/notes_service';
import { Note } from '../../types/note';

@Injectable({ providedIn: 'root' })
export class NoteService {
  private notesSubject = new BehaviorSubject<Note[]>([]);
  public notes$ = this.notesSubject.asObservable();

  constructor(private notesApi: NotesAPIService) {
    this.notesApi.getNotes().subscribe((noteResponse) => {
      this.notesSubject.next(noteResponse);
    });
  }

  updateNote(id: string, body: NoteUpdateRequest) {
    return this.notesApi.updateNote(id, body).pipe(
      tap((res: Note) => {
        const current = this.notesSubject.value;
        const idx = current.findIndex((n) => n.id === res.id);
        let next: Note[];
        if (idx === -1) {
          next = [res, ...current];
        } else {
          next = current.map((n) => (n.id === res.id ? (res) : n));
        }
        this.notesSubject.next(next);
      })
    );
  }
}
