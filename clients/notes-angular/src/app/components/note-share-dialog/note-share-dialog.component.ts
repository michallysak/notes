import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe } from '@ngx-translate/core';
import { finalize } from 'rxjs';
import { NotePermission, NoteShareResponse } from '@notes/notes_service';
import { NoteService } from '../../services/note/note.service';
import { Note } from '../../types/note';
import { Message } from 'primeng/message';
import { Select } from 'primeng/select';

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
export class NoteShareDialogComponent implements OnChanges {
  @Input({ required: true }) visible = false;
  @Input({ required: true }) note: Note | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() sharedStateChanged = new EventEmitter<{ noteId: string; isShared: boolean }>();

  shares = signal<ShareItem[]>([]);
  loading = signal(false);
  saving = signal(false);
  removingUserId = signal<string | null>(null);
  userNotFound = signal(false);
  permissionOptions: NotePermission[] = [NotePermission.READ, NotePermission.EDIT];

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

  constructor(private noteService: NoteService) {
    this.form.controls.email.valueChanges.subscribe(() => this.userNotFound.set(false));
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['visible']?.currentValue && this.note?.id) {
      this.loadPermissions(this.note.id);
    }

    if (changes['note']?.currentValue && this.visible && this.note?.id) {
      this.loadPermissions(this.note.id);
    }
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
          this.loadPermissions(noteId);
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
        next: () => this.loadPermissions(noteId),
        error: (err) => console.error('remove access failed', err),
      });
  }

  permissionsLabel(share: NoteShareResponse) {
    return (share.permissions ?? []).join(', ');
  }

  private loadPermissions(noteId: string) {
    this.loading.set(true);
    this.noteService
      .getPermissions(noteId)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (permissions) => {
          const mapped = (permissions ?? []).map((share) => ({
            ...share,
            selectedPermission: (share.permissions ?? []).includes(NotePermission.EDIT)
              ? NotePermission.EDIT
              : NotePermission.READ,
          }));
          this.shares.set(mapped);
          this.sharedStateChanged.emit({ noteId, isShared: mapped.length > 0 });
        },
        error: (err) => {
          console.error('load permissions failed', err);
          this.shares.set([]);
        },
      });
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
}

