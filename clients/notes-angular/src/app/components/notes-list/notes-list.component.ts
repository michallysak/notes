import { Component, Signal, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NoteService } from '../../services/note/note.service';
import { NoteCardComponent } from '../note-card/note-card.component';
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
export class NotesListComponent {
  pinnedNotesSection: Signal<any>;
  otherNotesSection: Signal<any>;
  sharedNotesSection: Signal<any>;

  clickNote = signal<ChangeNoteDialogStatus>({ visible: false, readonly: false });
  shareNote = signal<ShareNoteDialogStatus>({ visible: false });

  constructor(private noteService: NoteService) {
    this.pinnedNotesSection = toSignal(this.noteService.pinnedSection);
    this.otherNotesSection = toSignal(this.noteService.otherSection);
    this.sharedNotesSection = toSignal(this.noteService.sharedSection);
  }

  onPinClickPropagation(note: Note) {
    if (!note || !note.id) return;
    const body: NoteUpdateRequest = { pinned: !note.pinned };
    this.noteService.updateNote(note.id, body).subscribe({
      next: () => console.log('updated pinned state'),
      error: (err: any) => console.error('Failed to update pinned state', err),
    });
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

  get sections() {
    return [
      {
        id: 'pinned',
        titleKey: 'NOTES.SECTION_PINNED',
        signal: this.pinnedNotesSection,
        loadMore: () => this.noteService.loadMorePinned(),
      },
      {
        id: 'other',
        titleKey: 'NOTES.SECTION_OTHER',
        signal: this.otherNotesSection,
        loadMore: () => this.noteService.loadMoreOther(),
      },
      {
        id: 'shared',
        titleKey: 'NOTES.SECTION_SHARED',
        signal: this.sharedNotesSection,
        loadMore: () => this.noteService.loadMoreShared(),
      },
    ];
  }
}
