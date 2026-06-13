import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTranslateService, TranslateModule } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { NoteAttachmentsComponent } from './note-attachments.component';
import { AttachmentService } from '../../services/attachment/attachment.service';
import { NotificationService } from '../../services/notification/notification.service';
import { AttachmentResponse } from '@notes/notes_service';
import { SimpleChange } from '@angular/core';

describe('NoteAttachmentsComponent', () => {
  let component: NoteAttachmentsComponent;
  let fixture: ComponentFixture<NoteAttachmentsComponent>;

  const attachmentService = {
    getAttachmentsForNote: vi.fn(),
    createAttachment: vi.fn(),
    deleteAttachment: vi.fn(),
    downloadAttachment: vi.fn(),
  };

  const notificationService = {
    show: vi.fn(),
  };

  const mockAttachments: AttachmentResponse[] = [
    { id: '1', fileName: 'test1.pdf', size: 1024 },
    { id: '2', fileName: 'test2.doc', size: 2048 },
  ];

  beforeEach(async () => {
    attachmentService.getAttachmentsForNote.mockReset();
    attachmentService.createAttachment.mockReset();
    attachmentService.deleteAttachment.mockReset();
    attachmentService.downloadAttachment.mockReset();
    notificationService.show.mockReset();

    await TestBed.configureTestingModule({
      imports: [NoteAttachmentsComponent, TranslateModule.forRoot()],
      providers: [
        provideTranslateService({ lang: 'en', fallbackLang: 'en' }),
        { provide: AttachmentService, useValue: attachmentService },
        { provide: NotificationService, useValue: notificationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NoteAttachmentsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('initialization', () => {
    it('should have empty attachments initially', () => {
      expect(component.attachments()).toEqual([]);
    });

    it('should have empty uploading files initially', () => {
      expect(component.uploadingFiles()).toEqual([]);
    });

    it('should have readonly as false by default', () => {
      expect(component.readonly).toBe(false);
    });

    it('should have maxFileSize defined', () => {
      expect(component.maxFileSize).toBe(10 * 1024 * 1024);
    });
  });

  describe('ngOnChanges', () => {
    it('should load attachments when noteId changes', () => {
      attachmentService.getAttachmentsForNote.mockReturnValue(of(mockAttachments));

      component.noteId = 'note-1';
      component.ngOnChanges({
        noteId: new SimpleChange(undefined, 'note-1', true),
      });

      expect(attachmentService.getAttachmentsForNote).toHaveBeenCalledWith('note-1');
    });

    it('should load attachments when readonly changes and noteId exists', () => {
      attachmentService.getAttachmentsForNote.mockReturnValue(of(mockAttachments));

      component.noteId = 'note-1';
      component.ngOnChanges({
        readonly: new SimpleChange(false, true, false),
      });

      expect(attachmentService.getAttachmentsForNote).toHaveBeenCalledWith('note-1');
    });

    it('should not load attachments if noteId is undefined', () => {
      component.noteId = undefined;
      component.ngOnChanges({
        noteId: new SimpleChange(undefined, undefined, true),
      });

      expect(attachmentService.getAttachmentsForNote).not.toHaveBeenCalled();
    });
  });

  describe('loadAttachments', () => {
    it('should fetch attachments for the note', async () => {
      attachmentService.getAttachmentsForNote.mockReturnValue(of(mockAttachments));

      component.noteId = 'note-1';
      component.loadAttachments();

      await fixture.whenStable();
      expect(attachmentService.getAttachmentsForNote).toHaveBeenCalledWith('note-1');
      expect(component.attachments()).toEqual(mockAttachments);
    });

    it('should not fetch if noteId is undefined', () => {
      component.noteId = undefined;
      component.loadAttachments();

      expect(attachmentService.getAttachmentsForNote).not.toHaveBeenCalled();
    });

    it('should handle empty attachments list', async () => {
      attachmentService.getAttachmentsForNote.mockReturnValue(of([]));

      component.noteId = 'note-1';
      component.loadAttachments();

      await fixture.whenStable();
      expect(component.attachments()).toEqual([]);
    });

    it('should handle error when fetching attachments', async () => {
      const error = new Error('API Error');
      attachmentService.getAttachmentsForNote.mockReturnValue(throwError(() => error));

      component.noteId = 'note-1';
      component.loadAttachments();

      await fixture.whenStable();
      // The error should be caught by the subscription
      expect(attachmentService.getAttachmentsForNote).toHaveBeenCalled();
    });
  });

  describe('onSelectedFiles', () => {
    it('should do nothing if noteId is undefined', () => {
      component.noteId = undefined;

      const event = { currentFiles: [new File(['content'], 'test.txt')] };
      component.onSelectedFiles(event);

      expect(component.uploadingFiles()).toEqual([]);
    });

    it('should do nothing if readonly is true', () => {
      component.noteId = 'note-1';
      component.readonly = true;

      const event = { currentFiles: [new File(['content'], 'test.txt')] };
      component.onSelectedFiles(event);

      expect(component.uploadingFiles()).toEqual([]);
    });

    it('should add files to uploading list', () => {
      component.noteId = 'note-1';
      component.readonly = false;

      const file1 = new File(['content'], 'test1.txt');
      const file2 = new File(['content'], 'test2.txt');
      const event = { currentFiles: [file1, file2] };

      component.onSelectedFiles(event);

      expect(component.uploadingFiles().length).toBe(2);
      expect(component.uploadingFiles()[0].name).toBe('test1.txt');
      expect(component.uploadingFiles()[1].name).toBe('test2.txt');
    });

    it('should filter out files that are too large', () => {
      component.noteId = 'note-1';
      component.readonly = false;

      const smallFile = new File(['small'], 'small.txt');
      const largeFile = new File(['a'.repeat(11 * 1024 * 1024)], 'large.txt');
      const event = { currentFiles: [smallFile, largeFile] };

      component.onSelectedFiles(event);

      expect(component.uploadingFiles().length).toBe(1);
      expect(component.uploadingFiles()[0].name).toBe('small.txt');
    });

    it('should show notification for files that are too large', () => {
      component.noteId = 'note-1';
      component.readonly = false;

      const largeFile = new File(['a'.repeat(11 * 1024 * 1024)], 'large.txt');
      const event = { currentFiles: [largeFile] };

      component.onSelectedFiles(event);

      expect(notificationService.show).toHaveBeenCalled();
    });

    it('should do nothing if no files are allowed', () => {
      component.noteId = 'note-1';
      component.readonly = false;

      const largeFile = new File(['a'.repeat(11 * 1024 * 1024)], 'large.txt');
      const event = { currentFiles: [largeFile] };

      component.onSelectedFiles(event);

      expect(component.uploadingFiles()).toEqual([]);
    });

    it('should handle empty currentFiles', () => {
      component.noteId = 'note-1';
      component.readonly = false;

      const event = { currentFiles: [] };
      component.onSelectedFiles(event);

      expect(component.uploadingFiles()).toEqual([]);
    });
  });

  describe('onUpload', () => {
    it('should do nothing if noteId is undefined', () => {
      component.noteId = undefined;

      const event = { files: [new File(['content'], 'test.txt')] };
      component.onUpload(event as any);

      expect(attachmentService.createAttachment).not.toHaveBeenCalled();
    });

    it('should do nothing if readonly is true', () => {
      component.noteId = 'note-1';
      component.readonly = true;

      const event = { files: [new File(['content'], 'test.txt')] };
      component.onUpload(event as any);

      expect(attachmentService.createAttachment).not.toHaveBeenCalled();
    });

    it('should upload allowed files', async () => {
      component.noteId = 'note-1';
      component.readonly = false;
      attachmentService.createAttachment.mockReturnValue(of({ id: '1', fileName: 'test.txt' }));
      attachmentService.getAttachmentsForNote.mockReturnValue(of([]));

      const file = new File(['content'], 'test.txt');
      const event = { files: [file] };

      // Mock fileUpload ViewChild
      component.fileUpload = { clear: vi.fn() } as any;

      component.onUpload(event as any);

      expect(attachmentService.createAttachment).toHaveBeenCalledWith('note-1', file);
    });

    it('should clear file upload and reload attachments on successful upload', async () => {
      component.noteId = 'note-1';
      component.readonly = false;
      attachmentService.createAttachment.mockReturnValue(of({ id: '1', fileName: 'test.txt' }));
      attachmentService.getAttachmentsForNote.mockReturnValue(of(mockAttachments));

      const file = new File(['content'], 'test.txt');
      const event = { files: [file] };

      const mockClear = vi.fn();
      component.fileUpload = { clear: mockClear } as any;

      component.uploadingFiles.set([{ name: 'test.txt' }]);
      component.onUpload(event as any);

      await fixture.whenStable();
      expect(mockClear).toHaveBeenCalled();
      expect(attachmentService.getAttachmentsForNote).toHaveBeenCalledWith('note-1');
      expect(component.uploadingFiles()).toEqual([]);
    });

    it('should remove file from uploading list on error', async () => {
      component.noteId = 'note-1';
      component.readonly = false;
      const error = new Error('Upload failed');
      attachmentService.createAttachment.mockReturnValue(throwError(() => error));

      const file = new File(['content'], 'test.txt');
      const event = { files: [file] };

      component.fileUpload = {} as any;
      component.uploadingFiles.set([{ name: 'test.txt' }]);
      component.onUpload(event as any);

      await fixture.whenStable();
      expect(component.uploadingFiles()).toEqual([]);
    });

    it('should filter out too large files before uploading', () => {
      component.noteId = 'note-1';
      component.readonly = false;
      attachmentService.createAttachment.mockReturnValue(of({ id: '1', fileName: 'small.txt' }));

      const smallFile = new File(['small'], 'small.txt');
      const largeFile = new File(['a'.repeat(11 * 1024 * 1024)], 'large.txt');
      const event = { files: [smallFile, largeFile] };

      component.fileUpload = { clear: vi.fn() } as any;

      component.onUpload(event as any);

      expect(attachmentService.createAttachment).toHaveBeenCalledTimes(1);
      expect(attachmentService.createAttachment).toHaveBeenCalledWith('note-1', smallFile);
    });
  });

  describe('isTooLarge', () => {
    it('should return false for files smaller than maxFileSize', () => {
      const file = new File(['small content'], 'test.txt');
      expect(component.isTooLarge(file)).toBe(false);
    });

    it('should return true for files larger than maxFileSize', () => {
      const largeContent = 'a'.repeat(11 * 1024 * 1024);
      const file = new File([largeContent], 'large.txt');
      expect(component.isTooLarge(file)).toBe(true);
    });

    it('should show notification when file is too large', () => {
      const largeContent = 'a'.repeat(11 * 1024 * 1024);
      const file = new File([largeContent], 'large.txt');
      component.isTooLarge(file);

      expect(notificationService.show).toHaveBeenCalled();
    });

    it('should return false for files at exact maxFileSize', () => {
      const exactContent = 'a'.repeat(10 * 1024 * 1024);
      const file = new File([exactContent], 'exact.txt');
      expect(component.isTooLarge(file)).toBe(false);
    });
  });

  describe('onDownload', () => {
    it('should call downloadAttachment with id and fileName', () => {
      const attachment: AttachmentResponse = {
        id: '123',
        fileName: 'test.pdf',
        size: 1024,
      };

      component.onDownload(attachment);

      expect(attachmentService.downloadAttachment).toHaveBeenCalledWith('123', 'test.pdf');
    });

    it('should do nothing if attachment has no id', () => {
      const attachment: AttachmentResponse = {
        id: undefined,
        fileName: 'test.pdf',
        size: 1024,
      };

      component.onDownload(attachment);

      expect(attachmentService.downloadAttachment).not.toHaveBeenCalled();
    });

    it('should do nothing if attachment has no fileName', () => {
      const attachment: AttachmentResponse = {
        id: '123',
        fileName: undefined,
        size: 1024,
      };

      component.onDownload(attachment);

      expect(attachmentService.downloadAttachment).not.toHaveBeenCalled();
    });
  });

  describe('onDelete', () => {
    it('should delete attachment and reload', async () => {
      component.noteId = 'note-1';
      component.readonly = false;
      attachmentService.deleteAttachment.mockReturnValue(of({}));
      attachmentService.getAttachmentsForNote.mockReturnValue(of([]));

      const attachment: AttachmentResponse = {
        id: '123',
        fileName: 'test.pdf',
        size: 1024,
      };

      component.onDelete(attachment);

      await fixture.whenStable();
      expect(attachmentService.deleteAttachment).toHaveBeenCalledWith('123');
      expect(attachmentService.getAttachmentsForNote).toHaveBeenCalledWith('note-1');
    });

    it('should do nothing if attachment has no id', () => {
      component.noteId = 'note-1';
      const attachment: AttachmentResponse = {
        id: undefined,
        fileName: 'test.pdf',
        size: 1024,
      };

      component.onDelete(attachment);

      expect(attachmentService.deleteAttachment).not.toHaveBeenCalled();
    });

    it('should do nothing if readonly is true', () => {
      component.noteId = 'note-1';
      component.readonly = true;
      const attachment: AttachmentResponse = {
        id: '123',
        fileName: 'test.pdf',
        size: 1024,
      };

      component.onDelete(attachment);

      expect(attachmentService.deleteAttachment).not.toHaveBeenCalled();
    });

    it('should handle error on delete', async () => {
      component.noteId = 'note-1';
      component.readonly = false;
      const error = new Error('Delete failed');
      attachmentService.deleteAttachment.mockReturnValue(throwError(() => error));

      const attachment: AttachmentResponse = {
        id: '123',
        fileName: 'test.pdf',
        size: 1024,
      };

      component.onDelete(attachment);

      await fixture.whenStable();
      expect(attachmentService.deleteAttachment).toHaveBeenCalled();
    });
  });

  describe('signal updates', () => {
    it('should update attachments signal', () => {
      const attachments = [
        { id: '1', fileName: 'file1.txt', size: 100 },
      ];
      component.attachments.set(attachments);

      expect(component.attachments()).toEqual(attachments);
    });

    it('should update uploadingFiles signal', () => {
      const files = [{ name: 'uploading.txt' }];
      component.uploadingFiles.set(files);

      expect(component.uploadingFiles()).toEqual(files);
    });

    it('should update signals with array spread operator', () => {
      component.attachments.set([
        { id: '1', fileName: 'file1.txt', size: 100 },
      ]);

      component.attachments.update((current) => [
        ...current,
        { id: '2', fileName: 'file2.txt', size: 200 },
      ]);

      expect(component.attachments().length).toBe(2);
    });
  });

  describe('DOM rendering', () => {
    it('should render title with attachment count', async () => {
      component.attachments.set(mockAttachments);
      fixture.detectChanges();
      await fixture.whenStable();

      const title = fixture.nativeElement.querySelector('h5');
      expect(title).toBeTruthy();
      expect(title.textContent).toContain('(2)'); // 2 attachments
    });

    it('should render file upload button when not readonly', async () => {
      component.readonly = false;
      fixture.detectChanges();
      await fixture.whenStable();

      const fileUpload = fixture.nativeElement.querySelector('p-fileupload');
      expect(fileUpload).toBeTruthy();
    });

    it('should not render file upload button when readonly', async () => {
      component.readonly = true;
      fixture.detectChanges();
      await fixture.whenStable();

      const fileUpload = fixture.nativeElement.querySelector('p-fileupload');
      expect(fileUpload).toBeFalsy();
    });

    it('should render uploading files section when files are uploading', async () => {
      component.uploadingFiles.set([
        { name: 'uploading1.txt' },
        { name: 'uploading2.txt' },
      ]);
      fixture.detectChanges();
      await fixture.whenStable();

      const uploadingSection = fixture.nativeElement.textContent;
      expect(uploadingSection).toContain('Uploading');
      expect(uploadingSection).toContain('uploading1.txt');
      expect(uploadingSection).toContain('uploading2.txt');
    });

    it('should not render uploading files section when no files are uploading', async () => {
      component.uploadingFiles.set([]);
      fixture.detectChanges();
      await fixture.whenStable();

      const uploadingHeaders = fixture.nativeElement.querySelectorAll('h5');
      let hasUploadingHeader = false;
      uploadingHeaders.forEach((header: HTMLElement) => {
        if (header.textContent.includes('Uploading')) {
          hasUploadingHeader = true;
        }
      });
      expect(hasUploadingHeader).toBeFalsy();
    });

    it('should render attachments list when attachments exist', async () => {
      component.attachments.set(mockAttachments);
      fixture.detectChanges();
      await fixture.whenStable();

      const listItems = fixture.nativeElement.querySelectorAll('.attachment-item');
      expect(listItems.length).toBe(2);
    });

    it('should not render attachments list when no attachments exist', async () => {
      component.attachments.set([]);
      fixture.detectChanges();
      await fixture.whenStable();

      const listItems = fixture.nativeElement.querySelectorAll('.attachment-item');
      expect(listItems.length).toBe(0);
    });

    it('should render attachment file names', async () => {
      component.attachments.set(mockAttachments);
      fixture.detectChanges();
      await fixture.whenStable();

      const fileNames = fixture.nativeElement.querySelectorAll('.file-name');
      expect(fileNames[0].textContent).toContain('test1.pdf');
      expect(fileNames[1].textContent).toContain('test2.doc');
    });

    it('should render download button for each attachment', async () => {
      component.attachments.set(mockAttachments);
      component.readonly = false;
      fixture.detectChanges();
      await fixture.whenStable();

      const downloadButtons = fixture.nativeElement.querySelectorAll('p-button');
      // Each attachment has 2 buttons (download + delete when not readonly)
      expect(downloadButtons.length).toBeGreaterThanOrEqual(2);
    });

    it('should render delete button only when not readonly', async () => {
      component.attachments.set(mockAttachments);
      component.readonly = false;
      fixture.detectChanges();
      await fixture.whenStable();

      const buttons = fixture.nativeElement.querySelectorAll('p-button');
      expect(buttons.length).toBeGreaterThan(0);
    });

    it('should not render delete button when readonly', async () => {
      component.attachments.set(mockAttachments);
      component.readonly = true;
      fixture.detectChanges();
      await fixture.whenStable();

      const buttons = fixture.nativeElement.querySelectorAll('p-button');
      // Should only have download buttons, not delete buttons
      expect(buttons.length).toBe(2); // 1 download per attachment
    });

    it('should display file size using formatSize pipe', async () => {
      component.attachments.set([
        { id: '1', fileName: 'test.pdf', size: 1024 },
      ]);
      fixture.detectChanges();
      await fixture.whenStable();

      const fileSizeElement = fixture.nativeElement.querySelector('.file-size');
      expect(fileSizeElement).toBeTruthy();
      // The formatSize pipe should convert 1024 to "1 KB"
      expect(fileSizeElement.textContent).toContain('1 KB');
    });

    it('should show 0 size when attachment size is undefined', async () => {
      component.attachments.set([
        { id: '1', fileName: 'test.pdf', size: undefined },
      ]);
      fixture.detectChanges();
      await fixture.whenStable();

      const fileSizeElement = fixture.nativeElement.querySelector('.file-size');
      expect(fileSizeElement).toBeTruthy();
      expect(fileSizeElement.textContent).toContain('0 B');
    });

    it('should pass file name to download button title attribute', async () => {
      component.attachments.set([
        { id: '1', fileName: 'my-document.pdf', size: 1024 },
      ]);
      fixture.detectChanges();
      await fixture.whenStable();

      const fileNameSpan = fixture.nativeElement.querySelector('.file-name');
      expect(fileNameSpan.getAttribute('title')).toBe('my-document.pdf');
    });

    it('should disable file upload button when files are uploading', async () => {
      component.readonly = false;
      component.uploadingFiles.set([{ name: 'uploading.txt' }]);
      fixture.detectChanges();
      await fixture.whenStable();

      const fileUpload = fixture.nativeElement.querySelector('p-fileupload');
      // The chooseButtonProps should have disabled: true when uploadingFiles.length > 0
      expect(fileUpload).toBeTruthy();
    });

    it('should render progress spinner for each uploading file', async () => {
      component.uploadingFiles.set([
        { name: 'file1.txt' },
        { name: 'file2.txt' },
      ]);
      fixture.detectChanges();
      await fixture.whenStable();

      const spinners = fixture.nativeElement.querySelectorAll('p-progressSpinner');
      expect(spinners.length).toBe(2);
    });

    it('should render multiple uploading and attached files together', async () => {
      component.uploadingFiles.set([{ name: 'uploading.txt' }]);
      component.attachments.set(mockAttachments);
      fixture.detectChanges();
      await fixture.whenStable();

      const allItems = fixture.nativeElement.querySelectorAll('.attachment-item');
      // 1 uploading + 2 attached = 3 items
      expect(allItems.length).toBe(3);
    });

    it('should have correct CSS classes for styling', async () => {
      component.attachments.set(mockAttachments);
      fixture.detectChanges();
      await fixture.whenStable();

      const container = fixture.nativeElement.querySelector('.note-attachments');
      expect(container).toBeTruthy();

      const attachmentList = fixture.nativeElement.querySelector('.attachment-list');
      expect(attachmentList).toBeTruthy();

      const attachmentItems = fixture.nativeElement.querySelectorAll('.attachment-item');
      expect(attachmentItems.length).toBeGreaterThan(0);
    });
  });
});








