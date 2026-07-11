import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { NoteChangeDialogComponent } from '../../components/note-change-dialog/note-change-dialog.component';
import { NoteService } from '../../services/note/note.service';
import { Note } from '../../types/note';
import { AuthService } from '../../services/auth/auth.service';
import { NotePermission } from '@notes/notes_service';

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

  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private noteService: NoteService,
    private authService: AuthService,
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

      this.loading.set(true);
      this.note.set(null);
      this.readonly.set(true);

      this.noteService.getPublicNote(publicShareId).pipe(takeUntil(this.destroy$)).subscribe({
        next: (note) => {
          if (this.currentUserId() && note.authorId === this.currentUserId()) {
            this.router.navigate(['/', note.id], { replaceUrl: true });
            return;
          }
          this.note.set(note);
          this.readonly.set(!this.canEditFromPublicShare(note));
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
  }

  private canEditFromPublicShare(note: Note): boolean {
    const publicPermissions = (note as Note & { publicShare?: { permissions?: NotePermission[] } | null }).publicShare?.permissions ?? [];
    return publicPermissions.includes(NotePermission.EDIT);
  }

  ngOnDestroy() {
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
