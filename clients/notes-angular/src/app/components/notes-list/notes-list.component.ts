import { Component, OnDestroy, OnInit, Signal, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NoteService, NotesSection } from '../../services/note/note.service';
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
import { NoteAccessRemovedEventDTO, NoteUpdateRequest } from '@notes/notes_service';
import { NoteShareDialogComponent } from '../note-share-dialog/note-share-dialog.component';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { NoteEventsService } from '../../services/note/note-events.service';
import { AuthService } from '../../services/auth/auth.service';
import { NotificationService } from '../../services/notification/notification.service';
import { skip, Subject, takeUntil } from 'rxjs';

type ChangeNoteDialogStatus ={ visible: boolean; note: Note | null; readonly: boolean };
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
  clickNote = signal<ChangeNoteDialogStatus>({ visible: false, readonly: false, note: null });
  shareNote = signal<ShareNoteDialogStatus>({ visible: false });
  pinnedNotesSection: Signal<NotesSection | undefined>;
  otherNotesSection: Signal<NotesSection | undefined>;
  sharedNotesSection: Signal<NotesSection | undefined>;

  private destroy$ = new Subject<void>();
  private previousReadonlyState: boolean | null = null;

  constructor(
    private noteService: NoteService,
    private route: ActivatedRoute,
    private router: Router,
    private noteEventsService: NoteEventsService,
    private auth: AuthService,
    private notificationService: NotificationService,
  ) {
    this.pinnedNotesSection = toSignal(this.noteService.pinnedSection);
    this.otherNotesSection = toSignal(this.noteService.otherSection);
    this.sharedNotesSection = toSignal(this.noteService.sharedSection);

    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.noteService.getNoteById(id).subscribe({
          next: (note) => {
            if (note) {
              const readonly = !note.canEdit;
              this.clickNote.set({ visible: true, note, readonly });
              this.previousReadonlyState = readonly;
            }
          },
          error: (err) => {
            if (err.status === 403) {
              this.router.navigate(['/403'], { replaceUrl: true });
            } else {
              console.error('Failed to load note from URL mapping', err);
              this.router.navigate(['/'], { replaceUrl: true });
            }
          }
        });
      }
    });
  }

  ngOnInit() {
    this.setupPermissionChangeListener();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupPermissionChangeListener() {
    this.noteEventsService.domainEvents$
      .pipe(takeUntil(this.destroy$))
      .subscribe((event) => {
        if (event.type !== NoteAccessRemovedEventDTO.TypeEnum.NOTEACCESSREMOVEDEVENT) return;

        const currentUserId = this.auth.getCurrentUserValue()?.id;
        const currentNoteState = this.clickNote();
        if (!currentNoteState.visible || !currentNoteState.note || !currentUserId) return;

        const noteId = currentNoteState.note.id;
        if (event.payload?.noteId === noteId && event.payload?.userId === currentUserId) {
          setTimeout(() => {
            this.noteDialogClose();
            this.notificationService.show('NOTES.PERMISSION_REVOKED', 'warn', 5000);
          });
        }
      });

    this.noteService.notes$
      .pipe(
        takeUntil(this.destroy$),
        skip(1),
      )
      .subscribe((notes) => {
        const currentNoteState = this.clickNote();
        if (!currentNoteState.visible || !currentNoteState.note) return;

        const noteId = currentNoteState.note.id;
        const updatedNote = notes.find(n => n.id === noteId);
        if (!updatedNote) return;

        const newReadonly = !updatedNote.canEdit;
        const currentReadonly = this.previousReadonlyState;

        if (currentReadonly !== null && currentReadonly !== newReadonly) {
          setTimeout(() => {
            this.clickNote.set({
              visible: true,
              note: updatedNote,
              readonly: newReadonly
            });
            this.previousReadonlyState = newReadonly;
            this.notificationService.show('NOTES.PERMISSION_CHANGED', 'info', 5000);
          });
        }
      });
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
    this.previousReadonlyState = false;
  }

  async noteCardClick(note: Note) {
    const readonly = !note.canEdit;
    this.clickNote.set({ visible: true, note, readonly });
    this.previousReadonlyState = readonly;
    this.router.navigate(['/', note.id], { replaceUrl: true });
  }

  noteCardShareClick(note: Note) {
    this.shareNote.set({ visible: true, note });
  }

  noteDialogClose() {
    this.clickNote.set({ visible: false, readonly: false, note: null });
    this.previousReadonlyState = null;
    if (this.route.snapshot.paramMap.has('id')) {
      this.router.navigate(['/']);
    }
  }

  shareDialogClose() {
    this.shareNote.set({ visible: false });
  }

  getShareDialogNote(): Note | null {
    const state = this.shareNote();
    return state.visible && 'note' in state ? state.note : null;
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
