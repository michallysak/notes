import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, Subscription, takeUntil } from 'rxjs';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { NoteChangeDialogComponent } from '../../components/note-change-dialog/note-change-dialog.component';
import { NoteService } from '../../services/note/note.service';
import { Note } from '../../types/note';
import { AuthService } from '../../services/auth/auth.service';
import {
  DomainEventDTO,
  NotePermission,
  NotePublicShareRemovedEventDTO,
  NotePublicShareUpsertedEventDTO,
  NoteResponse,
  NoteUpdatedEventDTO,
} from '@notes/notes_service';
import { PublicNoteEventsService } from '../../services/note/public-note-events.service';
import { NotificationService } from '../../services/notification/notification.service';

@Component({
  selector: 'app-public-note-page',
  standalone: true,
  imports: [CommonModule, ProgressSpinnerModule, NoteChangeDialogComponent],
  templateUrl: './public-note-page.html',
  styleUrls: ['./public-note-page.scss'],
})
export class PublicNotePage implements OnInit, OnDestroy {
  note = signal<Note | null>(null);
  loading = signal(true);
  dialogVisible = signal(true);
  readonly = signal(true);
  private currentUserId = signal<string | null>(null);
  private currentPublicShareId = signal<string | null>(null);
  private previousReadonlyState: boolean | null = null;

  private destroy$ = new Subject<void>();
  private eventsSub?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private noteService: NoteService,
    private authService: AuthService,
    private publicNoteEventsService: PublicNoteEventsService,
    private notificationService: NotificationService,
  ) {}

  ngOnInit() {
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe((user) => {
      const userId = user?.id ?? null;
      this.currentUserId.set(userId);
      const currentNote = this.note();
      if (currentNote && userId && currentNote.authorId === userId) {
        this.router.navigate(['/', currentNote.id], { replaceUrl: true });
      }
    });

    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      const publicShareId = params.get('publicShareId');
      if (!publicShareId) {
        this.router.navigate(['/'], { replaceUrl: true });
        return;
      }
      this.currentPublicShareId.set(publicShareId);

      this.eventsSub?.unsubscribe();
      this.eventsSub = this.publicNoteEventsService
        .connect(publicShareId)
        .pipe(takeUntil(this.destroy$))
        .subscribe((event) => this.handlePublicEvent(event, publicShareId));

      this.loading.set(true);
      this.note.set(null);
      this.readonly.set(true);
      this.previousReadonlyState = null;

      this.noteService.getPublicNote(publicShareId).pipe(takeUntil(this.destroy$)).subscribe({
        next: (note) => {
          this.applyPublicNote(note, false);
          this.loading.set(false);
        },
        error: (err) => {
          if (err?.status === 403) {
            this.router.navigate(['/403'], { replaceUrl: true });
          } else {
            console.error('Failed to load public note', err);
            this.router.navigate(['/'], { replaceUrl: true });
          }
        },
      });
    });

    this.noteService.notes$.pipe(takeUntil(this.destroy$)).subscribe((notes) => {
      const currentNote = this.note();
      if (!currentNote) {
        return;
      }
      const updatedNote = notes.find((n) => n.id === currentNote.id);
      if (!updatedNote) {
        return;
      }
      this.applyPublicNote(updatedNote, true);
    });
  }

  private handlePublicEvent(event: DomainEventDTO, publicShareId: string) {
    if (this.currentPublicShareId() !== publicShareId) {
      return;
    }

    if (event.type === NotePublicShareUpsertedEventDTO.TypeEnum.NOTEPUBLICSHAREUPSERTEDEVENT) {
      if (event.payload?.publicShare?.publicShareId === publicShareId) {
        this.applyPublicNote(this.mapEventPayloadToNote(event.payload as NoteResponse), true);
      }
      return;
    }

    if (event.type === NoteUpdatedEventDTO.TypeEnum.NOTEUPDATEDEVENT) {
      if (event.payload?.publicShare?.publicShareId === publicShareId) {
        this.applyPublicNote(this.mapEventPayloadToNote(event.payload as NoteResponse), false);
      }
      return;
    }

    if (event.type === NotePublicShareRemovedEventDTO.TypeEnum.NOTEPUBLICSHAREREMOVEDEVENT) {
      if (event.payload?.publicShareId === publicShareId) {
        this.notificationService.show('NOTES.PERMISSION_REVOKED', 'warn', 5000);
        this.router.navigate(['/'], { replaceUrl: true });
      }
    }
  }

  private applyPublicNote(note: Note, notifyOnReadonlyChange: boolean) {
    if (this.currentUserId() && note.authorId === this.currentUserId()) {
      this.router.navigate(['/', note.id], { replaceUrl: true });
      return;
    }

    this.note.set(note);
    const nextReadonly = !this.canEditFromPublicShare(note);
    const previousReadonly = this.previousReadonlyState;
    this.readonly.set(nextReadonly);
    if (
      notifyOnReadonlyChange
      && previousReadonly !== null
      && previousReadonly !== nextReadonly
    ) {
      this.notificationService.show('NOTES.PERMISSION_CHANGED', 'info', 5000);
    }
    this.previousReadonlyState = nextReadonly;
  }

  private mapEventPayloadToNote(payload: NoteResponse): Note {
    const publicPermissions = payload.publicShare?.permissions ?? [];
    return {
      ...payload,
      shared: (payload.shares?.length ?? 0) > 0 || !!payload.publicShare?.publicShareId,
      canEdit: publicPermissions.includes(NotePermission.EDIT),
    };
  }

  private canEditFromPublicShare(note: Note): boolean {
    const publicPermissions = (note as Note & { publicShare?: { permissions?: NotePermission[] } | null }).publicShare?.permissions ?? [];
    return publicPermissions.includes(NotePermission.EDIT);
  }

  ngOnDestroy() {
    this.eventsSub?.unsubscribe();
    this.destroy$.next();
    this.destroy$.complete();
  }

  onDialogVisibleChange(visible: boolean) {
    this.dialogVisible.set(visible);
    if (!visible) {
      this.router.navigate(['/'], { replaceUrl: true });
    }
  }
}
