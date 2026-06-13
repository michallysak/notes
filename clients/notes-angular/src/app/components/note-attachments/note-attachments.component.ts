import {
  Component,
  inject,
  Input,
  OnChanges,
  signal,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { AttachmentService } from '../../services/attachment/attachment.service';
import { AttachmentResponse } from '@notes/notes_service';
import { ButtonModule } from 'primeng/button';
import { FileUpload, FileUploadHandlerEvent, FileUploadModule } from 'primeng/fileupload';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TooltipModule } from 'primeng/tooltip';
import { TranslatePipe } from '@ngx-translate/core';
import { FormatSizePipe } from '../../pipes/format-size.pipe';
import { NotificationService } from '../../services/notification/notification.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-note-attachments',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    FileUploadModule,
    ProgressSpinnerModule,
    TooltipModule,
    TranslatePipe,
    FormatSizePipe,
  ],
  templateUrl: './note-attachments.component.html',
  styleUrls: ['./note-attachments.component.scss'],
})
export class NoteAttachmentsComponent implements OnChanges {
  @Input({ required: true }) noteId!: string | undefined;
  @Input() readonly = false;

  @ViewChild('fileUpload') fileUpload!: FileUpload;

  private attachmentService = inject(AttachmentService);
  private notificationService = inject(NotificationService);
  private translate = inject(TranslateService);
  attachments = signal<AttachmentResponse[]>([]);
  uploadingFiles = signal<Array<{ name: string }>>([]);
  readonly maxFileSize = 10 * 1024 * 1024;

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['noteId'] || changes['readonly']) && this.noteId) {
      this.loadAttachments();
    }
  }

  loadAttachments() {
    if (!this.noteId) return;
    this.attachmentService
      .getAttachmentsForNote(this.noteId)
      .subscribe((data) => this.attachments.set(data));
  }

  onSelectedFiles(event: any) {
    if (!this.noteId || this.readonly) return;
    const files: File[] = event.currentFiles || [];
    const allowedFiles: File[] = [];
    files.forEach((file: File) => {
      if (this.isTooLarge(file)) {
        return;
      }
      allowedFiles.push(file);
    });
    if (allowedFiles.length === 0) return;
    const uploadingList = allowedFiles.map((file: File) => ({ name: file.name }));
    this.uploadingFiles.update((current) => [...current, ...uploadingList]);
  }

  onUpload(event: FileUploadHandlerEvent) {
    if (!this.noteId || this.readonly) return;
    const files = Array.from(event.files) as File[];
    const allowedFiles = files.filter((f) => !this.isTooLarge(f));
    allowedFiles.forEach((file: File) => {
      this.attachmentService.createAttachment(this.noteId!, file).subscribe({
        next: () => {
          this.uploadingFiles.update((current) => current.filter((f) => f.name !== file.name));
          this.loadAttachments();
          this.fileUpload.clear();
        },
        error: () => {
          this.uploadingFiles.update((current) => current.filter((f) => f.name !== file.name));
        },
      });
    });
  }

  isTooLarge(file: File): boolean {
    if (file.size > this.maxFileSize) {
      const msg = this.translate.instant('NOTES.ATTACHMENTS.ERROR_TOO_LARGE', { name: file.name });
      this.notificationService.show(msg, 'warn');
      return true;
    }
    return false;
  }

  onDownload(attachment: AttachmentResponse) {
    if (!attachment.id || !attachment.fileName) return;
    this.attachmentService.downloadAttachment(attachment.id, attachment.fileName);
  }

  onDelete(attachment: AttachmentResponse) {
    if (!attachment.id || this.readonly) return;
    this.attachmentService.deleteAttachment(attachment.id).subscribe(() => {
      this.loadAttachments();
    });
  }
}
