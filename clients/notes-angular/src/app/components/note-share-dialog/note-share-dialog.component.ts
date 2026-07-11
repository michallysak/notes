import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe } from '@ngx-translate/core';
import { finalize, Subject, debounceTime } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NotePermission, NoteShareResponse } from '@notes/notes_service';
import { NoteService } from '../../services/note/note.service';
import { Note } from '../../types/note';
import { Message } from 'primeng/message';
import { Select } from 'primeng/select';
import { Router } from '@angular/router';
import { NotificationService } from '../../services/notification/notification.service';

type ShareForm = {
  email: FormControl<string>;
  permission: FormControl<NotePermission>;
};

type ShareItem = NoteShareResponse & { selectedPermission: NotePermission };

@Component({
  selector: 'app-note-share-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DialogModule,
    InputTextModule,
    ButtonModule,
    ProgressSpinnerModule,
    TranslatePipe,
    Message,
    Select,
  ],
  templateUrl: './note-share-dialog.component.html',
  styleUrls: ['./note-share-dialog.component.scss'],
})
export class NoteShareDialogComponent implements OnChanges, OnDestroy {
  @Input({ required: true }) visible = false;
  @Input({ required: true }) note: Note | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() sharedStateChanged = new EventEmitter<{ noteId: string; isShared: boolean }>();

  shares = signal<ShareItem[]>([]);
  loading = signal(false);
  saving = signal(false);
  removingUserId = signal<string | null>(null);
  userNotFound = signal(false);
  publicEnabled = signal(false);
  publicPermission = signal(NotePermission.READ);
  publicShareId = signal<string | null>(null);
  publicUpdating = signal(false);
  permissionOptions: NotePermission[] = [NotePermission.READ, NotePermission.EDIT];

  private destroy$ = new Subject<void>();

  form = new FormGroup<ShareForm>({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    permission: new FormControl(NotePermission.READ, {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  constructor(
    private noteService: NoteService,
    private router: Router,
    private notificationService: NotificationService,
  ) {
    this.form.controls.email.valueChanges
      .pipe(debounceTime(500), takeUntil(this.destroy$))
      .subscribe(() => this.userNotFound.set(false));

    this.noteService.notes$
      .pipe(takeUntil(this.destroy$))
      .subscribe((notes) => {
        if (this.note?.id && this.visible) {
          const updatedNote = notes.find(n => n.id === this.note!.id);
          if (updatedNote) {
            this.syncSharesFromNote(updatedNote);
          }
        }
      });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['note']?.currentValue?.id !== changes['note']?.previousValue?.id) {
      this.publicEnabled.set(false);
      this.publicPermission.set(NotePermission.READ);
      this.publicShareId.set(null);
    }

    if (changes['visible']?.currentValue && this.note?.id) {
      this.syncSharesFromNote(this.note);
    }

    if (changes['note']?.currentValue && this.visible && this.note?.id) {
      this.syncSharesFromNote(this.note);
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onHide() {
    this.visible = false;
    this.visibleChange.emit(this.visible);
    this.form.reset({ email: '', permission: NotePermission.READ });
    this.shares.set([]);
    this.loading.set(false);
    this.saving.set(false);
    this.removingUserId.set(null);
    this.userNotFound.set(false);
    this.publicShareId.set(null);
  }

  onMakePublic(permissionRaw?: NotePermission | null | undefined) {
    const noteId = this.note?.id;
    if (!noteId || this.publicUpdating()) {
      return;
    }

    const permission = this.toPermission(permissionRaw) ?? this.publicPermission();
    if (!permission) {
      return;
    }

    this.publicPermission.set(permission);
    this.publicUpdating.set(true);
    this.noteService
      .makeNotePublic(noteId, permission)
      .pipe(finalize(() => this.publicUpdating.set(false)))
      .subscribe({
        next: (publicShareId) => {
          this.publicEnabled.set(true);
          this.publicShareId.set(publicShareId);
          this.emitSharedState(noteId);
        },
        error: (err) => console.error('make public failed', err),
      });
  }

  onUndoPublic() {
    const noteId = this.note?.id;
    if (!noteId || this.publicUpdating()) {
      return;
    }

    this.publicUpdating.set(true);
    this.noteService
      .undoNotePublic(noteId)
      .pipe(finalize(() => this.publicUpdating.set(false)))
      .subscribe({
        next: () => {
          this.publicEnabled.set(false);
          this.publicShareId.set(null);
          this.emitSharedState(noteId);
        },
        error: (err) => console.error('undo public failed', err),
      });
  }

  onPublicPermissionChange(permissionRaw: NotePermission | null | undefined) {
    const permission = this.toPermission(permissionRaw);
    if (!permission) {
      return;
    }

    this.publicPermission.set(permission);
    if (this.publicEnabled()) {
      this.onMakePublic(permission);
    }
  }

  async onCopyPublicLink() {
    const publicShareId = this.publicShareId();
    if (!publicShareId) {
      return;
    }

    const link = this.buildPublicLink(publicShareId);
    try {
      await navigator.clipboard.writeText(link);
      this.notificationService.show('NOTES.SHARE_DIALOG.PUBLIC.LINK_COPIED', 'success', 3000);
    } catch (err) {
      console.error('copy public link failed', err);
      this.notificationService.show('NOTES.SHARE_DIALOG.PUBLIC.LINK_COPY_FAILED', 'error', 5000);
    }
  }

  onShare() {
    const noteId = this.note?.id;
    if (!noteId || this.form.invalid || this.saving()) {
      return;
    }

    const email = this.form.controls.email.value.trim();
    if (!email) {
      return;
    }

    const permission = this.form.controls.permission.value;
    this.userNotFound.set(false);

    this.saving.set(true);
    this.noteService
      .setNotePermissions(noteId, email, [permission])
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.form.reset({ email: '', permission: NotePermission.READ });
          this.userNotFound.set(false);
          // Wait for SSE noteUpdateEvent to sync shares
        },
        error: (err) => {
          if ([400, 404].includes(err?.status)) {
            this.userNotFound.set(true);
            return;
          }
          console.error('share failed', err);
        },
      });
  }

  onPermissionChange(share: ShareItem, permissionRaw: NotePermission | null | undefined) {
    const permission = this.toPermission(permissionRaw);
    const noteId = this.note?.id;
    const email = share.email;
    if (!noteId || !email || !permission || share.selectedPermission === permission) {
      return;
    }

    const previous = share.selectedPermission;
    this.updateSharePermission(share, permission);

    this.noteService.setNotePermissions(noteId, email, [permission]).subscribe({
      next: () => undefined,
      error: (err) => {
        this.updateSharePermission(share, previous);
        console.error('permission update failed', err);
      },
    });
  }

  onRemove(share: NoteShareResponse) {
    const noteId = this.note?.id;
    const targetUserId = share.userId;
    if (!noteId || !targetUserId || this.removingUserId()) {
      return;
    }

    this.removingUserId.set(targetUserId);
    this.noteService
      .removeNoteAccess(noteId, targetUserId)
      .pipe(finalize(() => this.removingUserId.set(null)))
      .subscribe({
        next: () => {
          // Wait for SSE noteUpdateEvent to sync shares
        },
        error: (err) => console.error('remove access failed', err),
      });
  }

  permissionsLabel(share: NoteShareResponse) {
    return (share.permissions ?? []).join(', ');
  }

  publicLink() {
    const publicShareId = this.publicShareId();
    return publicShareId ? this.buildPublicLink(publicShareId) : '';
  }

  private syncSharesFromNote(note: Note) {
    const mapped = (note.shares ?? []).map((share) => ({
      ...share,
      selectedPermission: (share.permissions ?? []).includes(NotePermission.EDIT)
        ? NotePermission.EDIT
        : NotePermission.READ,
    }));
    const publicShare = (note as Note & {
      publicShare?: { publicShareId?: string; permissions?: NotePermission[] } | null;
    }).publicShare;
    this.shares.set(mapped);
    this.publicEnabled.set(!!publicShare?.publicShareId);
    this.publicShareId.set(publicShare?.publicShareId ?? null);
    this.publicPermission.set(
      (publicShare?.permissions ?? []).includes(NotePermission.EDIT) ? NotePermission.EDIT : NotePermission.READ,
    );
    this.emitSharedState(note.id);
  }

  private emitSharedState(noteId: string) {
    this.sharedStateChanged.emit({ noteId, isShared: this.publicEnabled() || this.shares().length > 0 });
  }

  private updateSharePermission(share: ShareItem, permission: NotePermission) {
    this.shares.update((items) =>
      items.map((item) =>
        item.userId === share.userId || item.email === share.email
          ? { ...item, selectedPermission: permission, permissions: [permission] }
          : item,
      ),
    );
  }

  private toPermission(value: NotePermission | null | undefined): NotePermission | null {
    return value === NotePermission.READ || value === NotePermission.EDIT ? value : null;
  }

  private buildPublicLink(publicShareId: string) {
    const path = this.router.serializeUrl(this.router.createUrlTree(['/public', publicShareId]));
    return `${window.location.origin}${path}`;
  }
}
