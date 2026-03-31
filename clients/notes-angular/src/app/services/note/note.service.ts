import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { NotesAPIService } from '@notes/notes_service';
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
}

