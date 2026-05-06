import { Component, OnDestroy, OnInit, Signal, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NoteService } from '../../services/note/note.service';
import { NoteCardComponent } from '../note-card/note-card.component';
import { Observable, Subscription } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TextareaModule } from 'primeng/textarea';
import { InputTextModule } from 'primeng/inputtext';
import { TranslatePipe } from '@ngx-translate/core';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { Note } from '../../types/note';
import { NoteChangeDialogComponent } from '../note-change-dialog/note-change-dialog.component';
import { NoteUpdateRequest, UserResponse } from '@notes/notes_service';
import { NoteShareDialogComponent } from '../note-share-dialog/note-share-dialog.component';
import { AuthService } from '../../services/auth/auth.service';
import { toSignal } from '@angular/core/rxjs-interop';

type ChangeNoteDialogStatus =
  | { visible: false; readonly: false }
  | ({ visible: true } & { note: Note | null; readonly: boolean });
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
  pinnedNotes = signal<Note[]>([]);
  otherNotes = signal<Note[]>([]);
  clickNote = signal<ChangeNoteDialogStatus>({ visible: false, readonly: false });
  shareNote = signal<ShareNoteDialogStatus>({ visible: false });
  currentUser: Signal<UserResponse | null | undefined>;

  constructor(
    private noteService: NoteService,
    private auth: AuthService,
  ) {
    this.notes$ = this.noteService.notes$;
    this.currentUser = toSignal(this.auth.currentUser$);
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
    });
  }
  ngOnDestroy(): void {
    this.notesSubscription?.unsubscribe();
  }

  openCreate() {
    console.log('open create');
    this.clickNote.set({ visible: true, note: null, readonly: false });
  }

  async noteCardClick(note: Note) {
    const readonly = !note.canEdit;
    this.clickNote.set({ visible: true, note, readonly });
  }

  noteCardShareClick(note: Note) {
    this.shareNote.set({ visible: true, note });
  }

  noteDialogClose() {
    this.clickNote.set({ visible: false, readonly: false });
  }

  shareDialogClose() {
    this.shareNote.set({ visible: false });
  }

  onSharedStateChanged(event: { noteId: string; isShared: boolean }) {
    this.noteService.refreshPermissions(event.noteId);
  }

  isShared(note: Note): boolean {
    return note.shared;
  }
}
