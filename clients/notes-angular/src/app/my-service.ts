import { inject, Injectable } from '@angular/core';
import { NotesAPIService } from '@notes/notes_service';

@Injectable({
  providedIn: 'root',
})
export class MyService {
  private readonly noteApi = inject(NotesAPIService);

  getNotes() {
    this.noteApi.getNotes().subscribe((notes) => {
      console.log(notes);
    });
  }
}
