import { of, throwError } from 'rxjs';
import { TestBed } from '@angular/core/testing';
import { AttachmentService } from './attachment.service';
import { AttachmentResponse } from '@notes/notes_service';
import { AttachmentsAPIService, BASE_PATH } from '@notes/notes_service';
import { HttpClient } from '@angular/common/http';

describe('AttachmentService', () => {
  const attachmentsApiMock = {
    getAttachmentsForNote: vi.fn(),
    createAttachment: vi.fn(),
    deleteAttachment: vi.fn(),
  };

  const httpClientMock = {
    get: vi.fn(),
  };

  beforeEach(async () => {
    attachmentsApiMock.getAttachmentsForNote.mockReset();
    attachmentsApiMock.createAttachment.mockReset();
    attachmentsApiMock.deleteAttachment.mockReset();
    httpClientMock.get.mockReset();

    await TestBed.configureTestingModule({
      providers: [
        AttachmentService,
        { provide: AttachmentsAPIService, useValue: attachmentsApiMock },
        { provide: HttpClient, useValue: httpClientMock },
        { provide: BASE_PATH, useValue: 'http://localhost:8080' },
      ],
    }).compileComponents();
  });

  const createService = () => TestBed.inject(AttachmentService);

  describe('getAttachmentsForNote', () => {
    it('should call attachmentsApi.getAttachmentsForNote with noteId', () => {
      const service = createService();
      const noteId = 'test-note-id';

      attachmentsApiMock.getAttachmentsForNote.mockReturnValue(of([]));

      service.getAttachmentsForNote(noteId);

      expect(attachmentsApiMock.getAttachmentsForNote).toHaveBeenCalledWith(noteId);
    });

    it('should return observable from attachmentsApi', async () => {
      const service = createService();
      const noteId = 'test-note-id';
      const mockAttachments: AttachmentResponse[] = [
        { id: '1', fileName: 'test.pdf', size: 1024 },
      ];

      attachmentsApiMock.getAttachmentsForNote.mockReturnValue(of(mockAttachments));

      const result = await service.getAttachmentsForNote(noteId).toPromise();
      expect(result).toEqual(mockAttachments);
    });

    it('should handle errors from attachmentsApi', async () => {
      const service = createService();
      const noteId = 'test-note-id';
      const error = new Error('API Error');

      attachmentsApiMock.getAttachmentsForNote.mockReturnValue(throwError(() => error));

      await expect(service.getAttachmentsForNote(noteId).toPromise()).rejects.toThrow('API Error');
    });
  });

  describe('createAttachment', () => {
    it('should call attachmentsApi.createAttachment with correct parameters', () => {
      const service = createService();
      const noteId = 'test-note-id';
      const file = new File(['content'], 'test.txt', { type: 'text/plain' });

      attachmentsApiMock.createAttachment.mockReturnValue(of({}));

      service.createAttachment(noteId, file);

      expect(attachmentsApiMock.createAttachment).toHaveBeenCalledWith(
        noteId,
        file.name,
        file.type,
        file
      );
    });

    it('should return observable from attachmentsApi', async () => {
      const service = createService();
      const noteId = 'test-note-id';
      const file = new File(['content'], 'test.txt', { type: 'text/plain' });
      const mockResponse: AttachmentResponse = {
        id: '1',
        fileName: 'test.txt',
        size: 7,
      };

      attachmentsApiMock.createAttachment.mockReturnValue(of(mockResponse));

      const result = await service.createAttachment(noteId, file).toPromise();
      expect(result).toEqual(mockResponse);
    });

    it('should handle file with different types', () => {
      const service = createService();
      const noteId = 'test-note-id';
      const files = [
        new File(['content'], 'test.pdf', { type: 'application/pdf' }),
        new File(['content'], 'test.doc', { type: 'application/msword' }),
        new File(['content'], 'test.jpg', { type: 'image/jpeg' }),
      ];

      attachmentsApiMock.createAttachment.mockReturnValue(of({}));

      files.forEach((file) => {
        service.createAttachment(noteId, file);
      });

      expect(attachmentsApiMock.createAttachment).toHaveBeenCalledTimes(3);
    });
  });

  describe('deleteAttachment', () => {
    it('should call attachmentsApi.deleteAttachment with attachment id', () => {
      const service = createService();
      const attachmentId = 'test-attachment-id';

      attachmentsApiMock.deleteAttachment.mockReturnValue(of({}));

      service.deleteAttachment(attachmentId);

      expect(attachmentsApiMock.deleteAttachment).toHaveBeenCalledWith(attachmentId);
    });

    it('should return observable from attachmentsApi', async () => {
      const service = createService();
      const attachmentId = 'test-attachment-id';

      attachmentsApiMock.deleteAttachment.mockReturnValue(of({ success: true }));

      const result = await service.deleteAttachment(attachmentId).toPromise();
      expect(result).toEqual({ success: true });
    });

    it('should handle errors when deleting', async () => {
      const service = createService();
      const attachmentId = 'test-attachment-id';
      const error = new Error('Delete failed');

      attachmentsApiMock.deleteAttachment.mockReturnValue(throwError(() => error));

      await expect(service.deleteAttachment(attachmentId).toPromise()).rejects.toThrow('Delete failed');
    });
  });

  describe('downloadAttachment', () => {
    it('should make http.get request with correct parameters', () => {
      const service = createService();
      const attachmentId = '123';
      const fileName = 'test.pdf';

      httpClientMock.get.mockReturnValue(of(new Blob()));

      service.downloadAttachment(attachmentId, fileName);

      expect(httpClientMock.get).toHaveBeenCalledWith(
        'http://localhost:8080/attachments/123',
        {
          responseType: 'blob',
          headers: { 'Accept': 'application/octet-stream' }
        }
      );
    });

    it('should encode the attachment id in URL', () => {
      const service = createService();
      const attachmentId = 'id with spaces/special';
      const fileName = 'test.pdf';

      httpClientMock.get.mockReturnValue(of(new Blob()));

      service.downloadAttachment(attachmentId, fileName);

      expect(httpClientMock.get).toHaveBeenCalledWith(
        expect.stringContaining(encodeURIComponent(String(attachmentId))),
        expect.any(Object)
      );
    });

    it('should create href and trigger download', () => {
      const service = createService();
      const attachmentId = '123';
      const fileName = 'test.pdf';
      const blob = new Blob(['test content']);

      httpClientMock.get.mockReturnValue(of(blob));

      // Mock DOM methods
      const mockLink = {
        href: '',
        download: '',
        click: vi.fn(),
      };
      const createElementSpy = vi.spyOn(document, 'createElement').mockReturnValue(mockLink as any);
      const createObjectUrlSpy = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:url');
      const revokeObjectUrlSpy = vi.spyOn(URL, 'revokeObjectURL');

      service.downloadAttachment(attachmentId, fileName);

      expect(createElementSpy).toHaveBeenCalledWith('a');
      expect(mockLink.download).toBe(fileName);
      expect(mockLink.click).toHaveBeenCalled();
      expect(revokeObjectUrlSpy).toHaveBeenCalledWith('blob:url');

      createElementSpy.mockRestore();
      createObjectUrlSpy.mockRestore();
      revokeObjectUrlSpy.mockRestore();
    });

    it('should handle special characters in file name', () => {
      const service = createService();
      const attachmentId = '123';
      const fileName = 'test file (copy) #2.pdf';
      const blob = new Blob(['test content']);

      httpClientMock.get.mockReturnValue(of(blob));

      const mockLink = {
        href: '',
        download: '',
        click: vi.fn(),
      };
      vi.spyOn(document, 'createElement').mockReturnValue(mockLink as any);
      vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:url');
      const revokeObjectUrlSpy = vi.spyOn(URL, 'revokeObjectURL');

      service.downloadAttachment(attachmentId, fileName);

      expect(mockLink.download).toBe(fileName);
      revokeObjectUrlSpy.mockRestore();
    });
  });

  describe('all methods', () => {
    it('should be called correctly in sequence', () => {
      const service = createService();
      const noteId = 'note-1';
      const attachmentId = 'attach-1';
      const file = new File(['content'], 'test.txt', { type: 'text/plain' });

      attachmentsApiMock.getAttachmentsForNote.mockReturnValue(of([]));
      attachmentsApiMock.createAttachment.mockReturnValue(of({}));
      attachmentsApiMock.deleteAttachment.mockReturnValue(of({}));
      httpClientMock.get.mockReturnValue(of(new Blob()));

      service.getAttachmentsForNote(noteId);
      service.createAttachment(noteId, file);
      service.downloadAttachment(attachmentId, 'test.txt');
      service.deleteAttachment(attachmentId);

      expect(attachmentsApiMock.getAttachmentsForNote).toHaveBeenCalledWith(noteId);
      expect(attachmentsApiMock.createAttachment).toHaveBeenCalled();
      expect(httpClientMock.get).toHaveBeenCalled();
      expect(attachmentsApiMock.deleteAttachment).toHaveBeenCalledWith(attachmentId);
    });
  });
});





