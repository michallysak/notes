import { Injectable } from '@angular/core';
import { BehaviorSubject, map } from 'rxjs';
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
  DomainEventDTO,
  NotePermissionsSetEventDTO,
  NoteAccessRemovedEventDTO,
} from '@notes/notes_service';
import { Note } from '../../types/note';
import { NoteEventsService } from './note-events.service';
import { AuthService } from '../auth/auth.service';

export interface NotesSection {
  data: Note[];
  page: number;
  hasMore: boolean;
  loading?: boolean;
}

@Injectable({ providedIn: 'root' })
export class NoteService {
  private notesSubject = new BehaviorSubject<Note[]>([]);
  public notes$ = this.notesSubject.asObservable();

  public pinnedSection = new BehaviorSubject<NotesSection>({ data: [], page: 0, hasMore: true });
  public otherSection = new BehaviorSubject<NotesSection>({ data: [], page: 0, hasMore: true });
  public sharedSection = new BehaviorSubject<NotesSection>({ data: [], page: 0, hasMore: true });

  private readonly pageSize = 5;

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

    noteEventsService.domainEvents$.subscribe((value: DomainEventDTO) => {
      if (!value) return;

      switch(value.type) {
        case NoteCreatedEventDTO.TypeEnum.NOTECREATEDEVENT:
        case NoteUpdatedEventDTO.TypeEnum.NOTEUPDATEDEVENT:
          if (value.payload) {
            this.upsertNoteInSubject(this.mapToNote(value.payload as NoteResponse));
          }
          break;
        case NoteDeletedEventDTO.TypeEnum.NOTEDELETEDEVENT:
          if (value.payload?.id) {
            this.removeNoteFromSubject(value.payload.id);
          }
          break;
        case NotePermissionsSetEventDTO.TypeEnum.NOTEPERMISSIONSSETEVENT:
          if (value.payload?.noteId) {
            this.getNoteById(value.payload.noteId).subscribe(note => {
              this.upsertNoteInSubject(note);
            });
          }
          break;
        case NoteAccessRemovedEventDTO.TypeEnum.NOTEACCESSREMOVEDEVENT:
          if (value.payload?.noteId) {
            const currentUserId = this.auth.getCurrentUserValue()?.id;
            const noteId = value.payload.noteId;
            if (value.payload.userId === currentUserId) {
              this.removeNoteFromSubject(noteId);
            } else {
              this.getNoteById(noteId).subscribe({
                next: (note) => this.upsertNoteInSubject(note),
                error: (err) => {
                  if (err?.status === 403) {
                    this.removeNoteFromSubject(noteId);
                  }
                }
              });
            }
          }
          break;
      }
    });
  }

  loadMorePinned() {
    const currentUserId = this.auth.getCurrentUserValue()?.id;
    this.fetchSection(this.pinnedSection, true, undefined, (n) => n.authorId === currentUserId);
  }

  loadMoreOther() {
    const currentUserId = this.auth.getCurrentUserValue()?.id;
    this.fetchSection(this.otherSection, false, undefined, (n) => n.authorId === currentUserId);
  }

  loadMoreShared() {
    const currentUserId = this.auth.getCurrentUserValue()?.id;
    this.fetchSection(this.sharedSection, undefined, true, (n) => n.authorId !== currentUserId);
  }

  private fetchSection(section: BehaviorSubject<NotesSection>, isPinned: boolean | undefined, isShared: boolean | undefined, filterFn?: (n: Note) => boolean) {
    const current = section.value;
    if (!current.hasMore || current.loading) return;

    section.next({ ...current, loading: true });

    this.notesApi.searchNotes(isPinned, isShared, current.page, undefined, this.pageSize).subscribe(res => {
      let data = res?.data || [];
      let mappedData = data.map(n => this.mapToNote(n));
      if (filterFn) {
        mappedData = mappedData.filter(filterFn);
      }

      const nextPage = current.page + 1;
      const hasMore = nextPage * this.pageSize < (res?.total || 0);

      section.next({
        data: [...current.data, ...mappedData],
        page: nextPage,
        hasMore,
        loading: false
      });
      this.syncAllNotes();
    });
  }

  private syncAllNotes() {
    const all = [
      ...this.pinnedSection.value.data,
      ...this.otherSection.value.data,
      ...this.sharedSection.value.data
    ];
    const unique = Array.from(new Map(all.map(item => [item.id, item])).values());
    this.notesSubject.next(unique);
  }

  private upsertNoteInSection(section: BehaviorSubject<NotesSection>, value: Note) {
    const current = section.value;
    const idx = current.data.findIndex(({ id }) => id === value.id);
    if (idx !== -1) {
      const nextData = [...current.data];
      nextData[idx] = { ...nextData[idx], ...value };
      section.next({ ...current, data: nextData });
    } else {
      section.next({ ...current, data: [value, ...current.data] });
    }
  }

  private removeNoteFromSection(section: BehaviorSubject<NotesSection>, noteId: string) {
    const current = section.value;
    const newData = current.data.filter(n => n.id !== noteId);
    if (newData.length !== current.data.length) {
      section.next({ ...current, data: newData });
    }
  }

  private upsertNoteInSubject(value: Note) {
    const current = this.notesSubject.value;
    const idx = current.findIndex(({ id }) => id === value.id);
    if (idx === -1) {
      this.notesSubject.next([value, ...current]);
      const currentUserId = this.auth.getCurrentUserValue()?.id;
      if (value.authorId !== currentUserId) {
        this.sharedSection.next({ ...this.sharedSection.value, data: [value, ...this.sharedSection.value.data] });
      } else if (value.pinned) {
        this.pinnedSection.next({ ...this.pinnedSection.value, data: [value, ...this.pinnedSection.value.data] });
      } else {
        this.otherSection.next({ ...this.otherSection.value, data: [value, ...this.otherSection.value.data] });
      }

      return;
    }

    const next = [...current];
    next[idx] = { ...next[idx], ...value };
    this.notesSubject.next(next);

    const currentUserId = this.auth.getCurrentUserValue()?.id;
    const isOwnNote = value.authorId === currentUserId;

    if (!isOwnNote) {
      this.upsertNoteInSection(this.sharedSection, next[idx]);
    } else {
      if (value.pinned) {
        this.upsertNoteInSection(this.pinnedSection, next[idx]);
        this.removeNoteFromSection(this.otherSection, value.id);
      } else {
        this.upsertNoteInSection(this.otherSection, next[idx]);
        this.removeNoteFromSection(this.pinnedSection, value.id);
      }
    }
  }

  private removeNoteFromSubject(id: string) {
    const current = this.notesSubject.value;
    this.notesSubject.next(current.filter((n) => n.id !== id));

    const removeFn = (sec: BehaviorSubject<NotesSection>) => {
      sec.next({ ...sec.value, data: sec.value.data.filter(n => n.id !== id) });
    };
    [this.pinnedSection, this.otherSection, this.sharedSection].forEach(removeFn);
  }

  loadNotes() {
    this.clearNotes();

    // Load initial page for each section
    this.loadMorePinned();
    this.loadMoreOther();
    this.loadMoreShared();
  }

  clearNotes() {
    this.notesSubject.next([]);
    const reset = { data: [], page: 0, hasMore: true };
    this.pinnedSection.next(reset);
    this.otherSection.next(reset);
    this.sharedSection.next(reset);
  }

  getNoteById(id: string) {
    return this.notesApi.getNote(id).pipe(map(res => this.mapToNote(res)));
  }

  updateNote(id: string, body: NoteUpdateRequest) {
    return this.notesApi.updateNote(body, id).pipe(
      tap((res: NoteResponse) => {
        const mappedNote = this.mapToNote(res);
        this.upsertNoteInSubject(mappedNote);
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

  setNotePermissions(id: string, email: string, permissions: NotePermission[] = [NotePermission.READ]) {
    const body: SetNotePermissionsRequest = { email, permissions };
    return this.notesApi.setNotePermissions(body, id);
  }

  removeNoteAccess(id: string, targetUserId: string) {
    return this.notesApi.removeNoteAccess(id, targetUserId);
  }

  private mapToNote(res: NoteResponse): Note {
    const shares = res.shares || [];
    const currentUser = this.auth.getCurrentUserValue();
    const currentUserId = currentUser?.id;

    const shared = shares.length > 0;
    const isAuthor = res.authorId === currentUserId;
    const canEdit = isAuthor || shares.some(
      (p) => p.userId === currentUserId && (p.permissions ?? []).includes(NotePermission.EDIT)
    );

    return {
      ...res,
      shared,
      canEdit,
    };
  }
}
