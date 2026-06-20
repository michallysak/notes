import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AttachmentsAPIService, AttachmentResponse, BASE_PATH } from '@notes/notes_service';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AttachmentService {
  private attachmentsApi = inject(AttachmentsAPIService);
  private http = inject(HttpClient);
  private basePath = inject(BASE_PATH, { optional: true });

  getAttachmentsForNote(noteId: string): Observable<AttachmentResponse[]> {
    return this.attachmentsApi.getAttachmentsForNote(noteId);
  }

  createAttachment(noteId: string, file: File): Observable<AttachmentResponse> {
    return this.attachmentsApi.createAttachment(noteId, file.name, file.type, file);
  }

  deleteAttachment(id: string): Observable<any> {
    return this.attachmentsApi.deleteAttachment(id);
  }

  downloadAttachment(id: string, fileName: string): void {
    this.attachmentsApi.getAttachmentContent(id).subscribe((blob) => {
      const a = document.createElement('a');
      const objectUrl = URL.createObjectURL(blob);
      a.href = objectUrl;
      a.download = fileName;
      a.click();
      URL.revokeObjectURL(objectUrl);
    });
  }
}

