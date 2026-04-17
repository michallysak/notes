import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
  CreateNoteRequest,
  NoteCreatedEventDTO,
  NoteDeletedEventDTO,
  NotesAPIService,
  NoteUpdatedEventDTO,
  NoteUpdateRequest,
} from '@notes/notes_service';
import { Note } from '../../types/note';
import { NoteEventsService } from './note-events.service';

@Injectable({ providedIn: 'root' })
export class NoteService {
  private notesSubject = new BehaviorSubject<Note[]>([]);
  public notes$ = this.notesSubject.asObservable();

  constructor(
    private notesApi: NotesAPIService,
    noteEventsService: NoteEventsService,
  ) {
    this.notesApi.getNotes().subscribe((noteResponse) => {
      this.notesSubject.next(noteResponse);
    });

    noteEventsService.noteEvents$.subscribe((value: NoteCreatedEventDTO) => {
      if (!value?.payload) {
        return;
      }
      const note: Note = { ...value.payload };
      this.upsertNoteInSubject(note);
    });

    noteEventsService.noteUpdatedEvents$.subscribe((value: NoteUpdatedEventDTO) => {
      if (!value?.payload) {
        return;
      }
      const note: Note = { ...value.payload };
      this.upsertNoteInSubject(note);
    });

    noteEventsService.noteDeletedEvents$.subscribe((value: NoteDeletedEventDTO) => {
      const id = value?.payload?.id;
      if (!id) {
        return;
      }
      this.removeNoteFromSubject(id);
    });
  }

  private upsertNoteInSubject(value: Note) {
    const current = this.notesSubject.value;
    const idx = current.findIndex(({ id }) => id === value.id);
    if (idx === -1) {
      this.notesSubject.next([value, ...current]);
      return;
    }

    const next = [...current];
    next[idx] = value;
    this.notesSubject.next(next);
  }

  private removeNoteFromSubject(id: string) {
    const current = this.notesSubject.value;
    this.notesSubject.next(current.filter((n) => n.id !== id));
  }

  updateNote(id: string, body: NoteUpdateRequest) {
    return this.notesApi.updateNote(body, id).pipe(
      tap((res: Note) => {
        const current = this.notesSubject.value;
        const idx = current.findIndex((n) => n.id === res.id);
        let next: Note[];
        if (idx === -1) {
          next = [res, ...current];
        } else {
          next = current.map((n) => (n.id === res.id ? res : n));
        }
        this.notesSubject.next(next);
      }),
    );
  }

  createNote(body: CreateNoteRequest) {
    return this.notesApi.createNote(body).pipe(tap((note: Note) => this.upsertNoteInSubject(note)));
  }

  deleteNote(id: string) {
    return this.notesApi.deleteNote(id).pipe(
      tap(() => {
        this.removeNoteFromSubject(id);
      }),
    );
  }
}
