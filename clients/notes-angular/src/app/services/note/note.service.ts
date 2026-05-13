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

  loadMorePinned() {
    this.fetchSection(this.pinnedSection, true, false, undefined);
  }

  loadMoreOther() {
    this.fetchSection(this.otherSection, false, false, undefined);
  }

  loadMoreShared() {
    const currentUserId = this.auth.getCurrentUserValue()?.id;
    this.fetchSection(this.sharedSection, false, true, (n) => n.authorId !== currentUserId);
  }

  private fetchSection(section: BehaviorSubject<NotesSection>, isPinned: boolean, isShared: boolean, filterFn?: (n: Note) => boolean) {
    const current = section.value;
    if (!current.hasMore || current.loading) return;

    section.next({ ...current, loading: true });

    this.notesApi.searchNotes(isPinned, isShared, current.page, this.pageSize).subscribe(res => {
      let data = res?.data || [];
      let mappedData = data.map(n => this.mapToNote(n));
      if (filterFn) {
        mappedData = mappedData.filter(filterFn);
      }
      this.loadPermissionsForNotes(mappedData);

      section.next({
        data: [...current.data, ...mappedData],
        page: current.page + 1,
        hasMore: (res?.data?.length || 0) >= this.pageSize,
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
    }
  }

  private upsertNoteInSubject(value: Note) {
    const current = this.notesSubject.value;
    const idx = current.findIndex(({ id }) => id === value.id);
    if (idx === -1) {
      this.notesSubject.next([value, ...current]);
      if (value.shared) {
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

    this.upsertNoteInSection(this.pinnedSection, next[idx]);
    this.upsertNoteInSection(this.otherSection, next[idx]);
    this.upsertNoteInSection(this.sharedSection, next[idx]);

    if (value.id) {
      this.loadPermissionsForNote(value.id);
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
