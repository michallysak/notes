import { Injectable } from '@angular/core';
import { BehaviorSubject, catchError, forkJoin, map, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
  CreateNoteRequest,
  NotePermission,
  NoteCreatedEventDTO,
  NoteDeletedEventDTO,
  NoteResponse,
  NotesAPIService,
  NoteUpdatedEventDTO,
  NoteUpdateRequest,
  SetNotePermissionsRequest,
} from '@notes/notes_service';
import { Note } from '../../types/note';
import { NoteEventsService } from './note-events.service';
import { AuthService } from '../auth/auth.service';

@Injectable({ providedIn: 'root' })
export class NoteService {
  private notesSubject = new BehaviorSubject<Note[]>([]);
  public notes$ = this.notesSubject.asObservable();

  constructor(
    private notesApi: NotesAPIService,
    private auth: AuthService,
    noteEventsService: NoteEventsService,
  ) {
    this.auth.logged$.subscribe((isLogged) => {
      if (isLogged) {
        this.loadNotes();
      } else {
        this.clearNotes();
      }
    });

    noteEventsService.noteEvents$.subscribe((value: NoteCreatedEventDTO) => {
      if (!value?.payload) {
        return;
      }
      this.upsertNoteInSubject(this.mapToNote(value.payload));
    });

    noteEventsService.noteUpdatedEvents$.subscribe((value: NoteUpdatedEventDTO) => {
      if (!value?.payload) {
        return;
      }
      this.upsertNoteInSubject(this.mapToNote(value.payload));
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
    next[idx] = { ...next[idx], ...value };
    this.notesSubject.next(next);

    if (value.id) {
      this.loadPermissionsForNote(value.id);
    }
  }

  private removeNoteFromSubject(id: string) {
    const current = this.notesSubject.value;
    this.notesSubject.next(current.filter((n) => n.id !== id));
  }

  loadNotes() {
    this.notesApi.searchNotes().subscribe((noteResponse) => {
      const notes = noteResponse.data.map((res) => this.mapToNote(res));
      this.notesSubject.next(notes);
      this.loadPermissionsForNotes(notes);
    });
  }

  clearNotes() {
    this.notesSubject.next([]);
  }

  private loadPermissionsForNotes(notes: Note[]) {
    const ids = notes.map((n) => n.id).filter((id): id is string => !!id);
    if (!ids.length) return;

    forkJoin(ids.map((id) => this.getPermissionsForNote(id))).subscribe((updates) => {
      const current = this.notesSubject.value;
      const next = current.map((n) => {
        const update = updates.find((u) => u.id === n.id);
        return update ? { ...n, ...update } : n;
      });
      this.notesSubject.next(next);
    });
  }

  private loadPermissionsForNote(id: string) {
    this.getPermissionsForNote(id).subscribe((update) => {
      const current = this.notesSubject.value;
      const next = current.map((n) => (n.id === id ? { ...n, ...update } : n));
      this.notesSubject.next(next);
    });
  }

  private getPermissionsForNote(id: string) {
    return this.notesApi.getPermissions(id).pipe(
      map((permissions) => {
        const currentUser = this.auth.getCurrentUserValue();
        const currentUserId = currentUser?.id;

        const shared = (permissions ?? []).length > 0;

        const canEdit = (permissions ?? []).some(
          (p) => p.userId === currentUserId && (p.permissions ?? []).includes(NotePermission.EDIT)
        );

        return {
          id,
          shared,
          canEdit,
        };
      }),
      catchError((err) => {
        console.error(`Error loading permissions for ${id}`, err);
        return of({ id, shared: false, canEdit: false });
      }),
    );
  }

  updateNote(id: string, body: NoteUpdateRequest) {
    return this.notesApi.updateNote(body, id).pipe(
      tap((res: NoteResponse) => {
        const current = this.notesSubject.value;
        const idx = current.findIndex((n) => n.id === res.id);
        let next: Note[];
        if (idx === -1) {
          next = [this.mapToNote(res), ...current];
        } else {
          next = current.map((n) => (n.id === res.id ? { ...n, ...res } : n));
        }
        this.notesSubject.next(next);
      }),
    );
  }

  createNote(body: CreateNoteRequest) {
    return this.notesApi.createNote(body).pipe(tap((res: NoteResponse) => this.upsertNoteInSubject(this.mapToNote(res))));
  }

  deleteNote(id: string) {
    return this.notesApi.deleteNote(id).pipe(
      tap(() => {
        this.removeNoteFromSubject(id);
      }),
    );
  }

  getPermissions(id: string) {
    return this.notesApi.getPermissions(id);
  }

  setNotePermissions(id: string, email: string, permissions: NotePermission[] = [NotePermission.READ]) {
    const body: SetNotePermissionsRequest = { email, permissions };
    return this.notesApi.setNotePermissions(body, id);
  }

  removeNoteAccess(id: string, targetUserId: string) {
    return this.notesApi.removeNoteAccess(id, targetUserId);
  }

  refreshPermissions(id: string) {
    this.loadPermissionsForNote(id);
  }

  private mapToNote(res: NoteResponse): Note {
    return {
      ...res,
      shared: false,
      canEdit: false,
    };
  }
}
