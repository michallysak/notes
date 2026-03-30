import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { NoteResponse, NotesAPIService } from '@notes/notes_service';

@Injectable({ providedIn: 'root' })
export class NoteService {
  private notesSubject = new BehaviorSubject<NoteResponse[]>([]);
  public notes$ = this.notesSubject.asObservable();

  constructor(private notesApi: NotesAPIService) {
    this.notesApi.getNotes().subscribe((res) => {
      // TODO fix codegen wrong type
      this.notesSubject.next(res as unknown as NoteResponse[]);
    });
  }
}

