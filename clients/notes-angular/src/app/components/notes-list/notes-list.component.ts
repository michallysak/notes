import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NoteService } from '../../services/note/note.service';
import { NoteCardComponent } from '../note-card/note-card.component';
import { catchError, forkJoin, map, Observable, of, Subscription } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TextareaModule } from 'primeng/textarea';
import { InputTextModule } from 'primeng/inputtext';
import { TranslatePipe } from '@ngx-translate/core';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { Note } from '../../types/note';
import { NoteChangeDialogComponent } from '../note-change-dialog/note-change-dialog.component';
import { NoteUpdateRequest } from '@notes/notes_service';
import { NoteShareDialogComponent } from '../note-share-dialog/note-share-dialog.component';

type ChangeNoteDialogStatus = { visible: false } | ({ visible: true } & { note: Note | null });
type ShareNoteDialogStatus = { visible: false } | ({ visible: true } & { note: Note });

@Component({
  selector: 'app-notes-list',
  standalone: true,
  imports: [
    CommonModule,
    NoteCardComponent,
    ReactiveFormsModule,
    ButtonModule,
    DialogModule,
    TextareaModule,
    InputTextModule,
    TranslatePipe,
    ProgressSpinnerModule,
    NoteChangeDialogComponent,
    NoteShareDialogComponent,
  ],
  styleUrls: ['./notes-list.component.scss'],
  templateUrl: './notes-list.component.html',
})
export class NotesListComponent implements OnInit, OnDestroy {
  private notes$: Observable<Note[]>;
  private notesSubscription: Subscription | null = null;
  private sharedStatusSubscription: Subscription | null = null;
  private sharedStatusLoadId = 0;
  pinnedNotes = signal<Note[]>([]);
  otherNotes = signal<Note[]>([]);
  sharedByNoteId = signal<Record<string, boolean>>({});
  clickNote = signal<ChangeNoteDialogStatus>({ visible: false });
  shareNote = signal<ShareNoteDialogStatus>({ visible: false });

  constructor(private noteService: NoteService) {
    this.notes$ = this.noteService.notes$;
  }

  onPinClickPropagation(note: Note) {
    if (!note || !note.id) return;
    const body: NoteUpdateRequest = { pinned: !note.pinned };
    this.noteService.updateNote(note.id, body).subscribe({
      next: () => console.log('updated pinned state'),
      error: (err: any) => console.error('Failed to update pinned state', err),
    });
  }

  ngOnInit(): void {
    this.notesSubscription = this.notes$.subscribe((list) => {
      this.pinnedNotes.set(list.filter((n) => n.pinned));
      this.otherNotes.set(list.filter((n) => !n.pinned));
      this.loadSharedStatus(list);
    });
  }
  ngOnDestroy(): void {
    this.notesSubscription?.unsubscribe();
    this.sharedStatusSubscription?.unsubscribe();
  }

  isShared(note: Note){
    return this.sharedByNoteId()[note.id] ?? false;
  }

  openCreate() {
    console.log('open create');
    this.clickNote.set({ visible: true, note: null });
  }

  noteCardClick(note: Note) {
    this.clickNote.set({ visible: true, note });
  }

  noteCardShareClick(note: Note) {
    this.shareNote.set({ visible: true, note });
  }

  noteDialogClose() {
    this.clickNote.set({ visible: false });
  }

  shareDialogClose() {
    this.shareNote.set({ visible: false });
  }

  onSharedStateChanged(event: { noteId: string; isShared: boolean }) {
    this.sharedByNoteId.update((current) => ({ ...current, [event.noteId]: event.isShared }));
  }

  private loadSharedStatus(notes: Note[]) {
    const noteIds = notes.map((note) => note.id).filter((id): id is string => !!id);
    if (noteIds.length === 0) {
      this.sharedByNoteId.set({});
      return;
    }

    this.sharedStatusSubscription?.unsubscribe();
    const requestId = ++this.sharedStatusLoadId;

    this.sharedStatusSubscription = forkJoin(
      noteIds.map((id) =>
        this.noteService.getPermissions(id).pipe(
          map((permissions) => ({ id, shared: (permissions ?? []).length > 0 })),
          catchError(() => of({ id, shared: false })),
        ),
      ),
    ).subscribe((statuses) => {
      if (requestId !== this.sharedStatusLoadId) {
        return;
      }

      const nextState: Record<string, boolean> = {};
      statuses.forEach((status) => {
        nextState[status.id] = status.shared;
      });
      this.sharedByNoteId.set(nextState);
    });
  }
}
