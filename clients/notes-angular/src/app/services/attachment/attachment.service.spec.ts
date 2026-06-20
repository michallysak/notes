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
    getAttachmentContent: vi.fn(),
  };

  const httpClientMock = {
    get: vi.fn(),
  };

  beforeEach(async () => {
    vi.restoreAllMocks();

    attachmentsApiMock.getAttachmentsForNote.mockReset();
    attachmentsApiMock.createAttachment.mockReset();
    attachmentsApiMock.deleteAttachment.mockReset();
    attachmentsApiMock.getAttachmentContent.mockReset();
    httpClientMock.get.mockReset();

    attachmentsApiMock.getAttachmentContent.mockReturnValue(of(new Blob(['default'])));

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
    it('should call API with noteId', () => {
      const service = createService();

      attachmentsApiMock.getAttachmentsForNote.mockReturnValue(of([]));

      service.getAttachmentsForNote('note-1');

      expect(attachmentsApiMock.getAttachmentsForNote).toHaveBeenCalledWith('note-1');
    });

    it('should return attachments', async () => {
      const service = createService();

      const mock: AttachmentResponse[] = [{ id: '1', fileName: 'a.pdf', size: 10 }];

      attachmentsApiMock.getAttachmentsForNote.mockReturnValue(of(mock));

      const result = await service.getAttachmentsForNote('note-1').toPromise();

      expect(result).toEqual(mock);
    });

    it('should propagate errors', async () => {
      const service = createService();

      attachmentsApiMock.getAttachmentsForNote.mockReturnValue(
        throwError(() => new Error('API Error')),
      );

      await expect(service.getAttachmentsForNote('note-1').toPromise()).rejects.toThrow(
        'API Error',
      );
    });
  });

  describe('createAttachment', () => {
    it('should call API correctly', () => {
      const service = createService();

      const file = new File(['x'], 'test.txt', { type: 'text/plain' });

      attachmentsApiMock.createAttachment.mockReturnValue(of({}));

      service.createAttachment('note-1', file);

      expect(attachmentsApiMock.createAttachment).toHaveBeenCalledWith(
        'note-1',
        file.name,
        file.type,
        file,
      );
    });

    it('should return response', async () => {
      const service = createService();

      const file = new File(['x'], 'test.txt', { type: 'text/plain' });

      const mock: AttachmentResponse = {
        id: '1',
        fileName: 'test.txt',
        size: 123,
      };

      attachmentsApiMock.createAttachment.mockReturnValue(of(mock));

      const result = await service.createAttachment('note-1', file).toPromise();

      expect(result).toEqual(mock);
    });
  });

  describe('deleteAttachment', () => {
    it('should call delete API', () => {
      const service = createService();

      attachmentsApiMock.deleteAttachment.mockReturnValue(of({}));

      service.deleteAttachment('id-1');

      expect(attachmentsApiMock.deleteAttachment).toHaveBeenCalledWith('id-1');
    });

    it('should handle errors', async () => {
      const service = createService();

      attachmentsApiMock.deleteAttachment.mockReturnValue(
        throwError(() => new Error('Delete failed')),
      );

      await expect(service.deleteAttachment('id-1').toPromise()).rejects.toThrow('Delete failed');
    });
  });

  describe('downloadAttachment', () => {
    it('should trigger download flow', () => {
      const service = createService();

      const blob = new Blob(['data']);

      attachmentsApiMock.getAttachmentContent.mockReturnValue(of(blob));

      const mockLink = {
        href: '',
        download: '',
        click: vi.fn(),
      };

      vi.spyOn(document, 'createElement').mockReturnValue(mockLink as any);
      vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:url');
      const revokeSpy = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {});

      service.downloadAttachment('123', 'file.pdf');

      expect(attachmentsApiMock.getAttachmentContent).toHaveBeenCalledWith('123');
      expect(document.createElement).toHaveBeenCalledWith('a');

      expect(mockLink.href).toBe('blob:url');
      expect(mockLink.download).toBe('file.pdf');
      expect(mockLink.click).toHaveBeenCalled();

      expect(revokeSpy).toHaveBeenCalledWith('blob:url');
    });

    it('should NOT create link on error', () => {
      const service = createService();

      attachmentsApiMock.getAttachmentContent.mockReturnValue(
        throwError(() => new Error('Download failed')),
      );

      const createSpy = vi.spyOn(document, 'createElement');

      expect(() => {
        service.downloadAttachment('123', 'file.pdf');
      }).not.toThrow();

      expect(createSpy).not.toHaveBeenCalled();
    });
  });

  describe('all methods', () => {
    it('should call all APIs in sequence', () => {
      const service = createService();

      const file = new File(['x'], 'test.txt', { type: 'text/plain' });

      attachmentsApiMock.getAttachmentsForNote.mockReturnValue(of([]));
      attachmentsApiMock.createAttachment.mockReturnValue(of({}));
      attachmentsApiMock.deleteAttachment.mockReturnValue(of({}));
      attachmentsApiMock.getAttachmentContent.mockReturnValue(of(new Blob()));
      httpClientMock.get.mockReturnValue(of(new Blob()));

      service.getAttachmentsForNote('n');
      service.createAttachment('n', file);
      service.downloadAttachment('a', 'x.txt');
      service.deleteAttachment('a');

      expect(attachmentsApiMock.getAttachmentsForNote).toHaveBeenCalled();
      expect(attachmentsApiMock.createAttachment).toHaveBeenCalled();
      expect(attachmentsApiMock.getAttachmentContent).toHaveBeenCalled();
      expect(attachmentsApiMock.deleteAttachment).toHaveBeenCalled();
    });
  });
});
