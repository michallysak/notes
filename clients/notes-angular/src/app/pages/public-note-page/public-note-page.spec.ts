import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { PublicNotePage } from './public-note-page';
import { ActivatedRoute, Router } from '@angular/router';
import { NoteService } from '../../services/note/note.service';
import { Note } from '../../types/note';
import { AuthService } from '../../services/auth/auth.service';
import { AttachmentService } from '../../services/attachment/attachment.service';
import { NotePermission,
  NotePublicShareRemovedEventDTO,
  NotePublicShareUpsertedEventDTO,
  NoteUpdatedEventDTO,
} from '@notes/notes_service';
import { NotificationService } from '../../services/notification/notification.service';
import { PublicNoteEventsService } from '../../services/note/public-note-events.service';

describe('PublicNotePage', () => {
  let component: PublicNotePage;
  let fixture: ComponentFixture<PublicNotePage>;
  let noteService: any;
  let authService: any;
  let publicNoteEventsService: any;
  let notificationService: any;
  let attachmentService: any;
  let router: Router;
  let currentUser$: BehaviorSubject<{ id: string } | null>;
  let noteEvents$: Subject<any>;
  let notes$: BehaviorSubject<Note[]>;

  const createNote = (overrides: Partial<Note> = {}): Note => ({
    id: 'public-1',
    authorId: 'auth-1',
    title: 'Public note',
    content: 'content',
    created: null as any,
    updated: null as any,
    pinned: false,
    style: undefined,
    shares: [],
    shared: true,
    canEdit: false,
    publicShare: { publicShareId: 'public-1', permissions: [NotePermission.READ] } as any,
    ...overrides,
  });

  beforeEach(async () => {
    noteEvents$ = new Subject<any>();
    notes$ = new BehaviorSubject<Note[]>([]);
    noteService = {
      getPublicNote: vi.fn().mockReturnValue(of(createNote({ canEdit: true }))),
      notes$: notes$.asObservable(),
    };
    currentUser$ = new BehaviorSubject<{ id: string } | null>({ id: 'other-user' });
    authService = {
      currentUser$,
      getCurrentUserValue: vi.fn().mockReturnValue({ id: 'other-user' }),
    };
    publicNoteEventsService = {
      connect: vi.fn().mockReturnValue(noteEvents$.asObservable()),
    };
    notificationService = {
      show: vi.fn(),
    };
    attachmentService = {
      getAttachmentsForNote: vi.fn().mockReturnValue(of([])),
      createAttachment: vi.fn().mockReturnValue(of({})),
      deleteAttachment: vi.fn().mockReturnValue(of({})),
      downloadAttachment: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [PublicNotePage],
      providers: [
        provideRouter([]),
        provideTranslateService({ lang: 'en', fallbackLang: 'en' }),
        { provide: ActivatedRoute, useValue: { paramMap: of(new Map([['publicShareId', 'public-1']])) } },
        { provide: NoteService, useValue: noteService },
        { provide: AuthService, useValue: authService },
        { provide: PublicNoteEventsService, useValue: publicNoteEventsService },
        { provide: NotificationService, useValue: notificationService },
        { provide: AttachmentService, useValue: attachmentService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PublicNotePage);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('loads a public note from the route id', () => {
    expect(noteService.getPublicNote).toHaveBeenCalledWith('public-1');
    expect(component.note()?.id).toBe('public-1');
    expect(component.loading()).toBe(false);
  });

  it('renders the note dialog once loaded', () => {
    expect(fixture.debugElement.query(By.css('app-note-change-dialog'))).toBeTruthy();
  });

  it('allows editing when the note is editable', () => {
    noteService.getPublicNote.mockReturnValue(
      of(createNote({ publicShare: { publicShareId: 'public-1', permissions: [NotePermission.EDIT] } as any })),
    );
    fixture = TestBed.createComponent(PublicNotePage);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const dialog = fixture.debugElement.query(By.css('app-note-change-dialog'));
    expect(dialog.componentInstance.readonly).toBe(false);
  });

  it('keeps the dialog read-only when the note is not editable', () => {
    noteService.getPublicNote.mockReturnValue(
      of(createNote({ canEdit: true, publicShare: { publicShareId: 'public-1', permissions: [NotePermission.READ] } as any })),
    );
    fixture = TestBed.createComponent(PublicNotePage);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const dialog = fixture.debugElement.query(By.css('app-note-change-dialog'));
    expect(dialog.componentInstance.readonly).toBe(true);
  });

  it('toggles readonly and shows notification when public share permission changes', () => {
    noteService.getPublicNote.mockReturnValue(
      of(createNote({ publicShare: { publicShareId: 'public-1', permissions: [NotePermission.READ] } as any })),
    );
    fixture = TestBed.createComponent(PublicNotePage);
    component = fixture.componentInstance;
    fixture.detectChanges();

    noteEvents$.next({
      type: NotePublicShareUpsertedEventDTO.TypeEnum.NOTEPUBLICSHAREUPSERTEDEVENT,
      payload: {
        ...createNote({ id: 'public-1' }),
        shares: [],
        publicShare: { publicShareId: 'public-1', permissions: [NotePermission.EDIT] },
      },
    });
    fixture.detectChanges();

    expect(component.readonly()).toBe(false);
    expect(notificationService.show).toHaveBeenCalledWith('NOTES.PERMISSION_CHANGED', 'info', 5000);
  });

  it('becomes read-only and shows notification when public share permission changes from edit to read', () => {
    noteService.getPublicNote.mockReturnValue(
      of(createNote({ publicShare: { publicShareId: 'public-1', permissions: [NotePermission.EDIT] } as any })),
    );
    fixture = TestBed.createComponent(PublicNotePage);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.readonly()).toBe(false);

    noteEvents$.next({
      type: NotePublicShareUpsertedEventDTO.TypeEnum.NOTEPUBLICSHAREUPSERTEDEVENT,
      payload: {
        ...createNote({ id: 'public-1' }),
        shares: [],
        publicShare: { publicShareId: 'public-1', permissions: [NotePermission.READ] },
      },
    });
    fixture.detectChanges();

    expect(component.readonly()).toBe(true);
    expect(notificationService.show).toHaveBeenCalledWith('NOTES.PERMISSION_CHANGED', 'info', 5000);
  });

  it('updates the dialog when the author changes the public note', () => {
    noteEvents$.next({
      type: NoteUpdatedEventDTO.TypeEnum.NOTEUPDATEDEVENT,
      payload: {
        ...createNote({ id: 'public-1', title: 'Updated title', content: 'Updated content' }),
        publicShare: { publicShareId: 'public-1', permissions: [NotePermission.READ] },
      },
    });
    fixture.detectChanges();

    expect(component.note()?.title).toBe('Updated title');
    expect(component.note()?.content).toBe('Updated content');
    expect(notificationService.show).not.toHaveBeenCalled();
  });

  it('shows revoke notification and navigates home when current public share is removed', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    noteEvents$.next({
      type: NotePublicShareRemovedEventDTO.TypeEnum.NOTEPUBLICSHAREREMOVEDEVENT,
      payload: { noteId: 'public-1', publicShareId: 'public-1' },
    });

    expect(notificationService.show).toHaveBeenCalledWith('NOTES.PERMISSION_REVOKED', 'warn', 5000);
    expect(navigateSpy).toHaveBeenCalledWith(['/'], { replaceUrl: true });
  });

  it('navigates home when the dialog is closed', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.onDialogVisibleChange(false);

    expect(navigateSpy).toHaveBeenCalledWith(['/'], { replaceUrl: true });
  });

  it('redirects author to regular note route', () => {
    noteService.getPublicNote.mockReturnValue(of(createNote({ id: 'note-42', authorId: 'auth-1' })));
    authService.getCurrentUserValue.mockReturnValue(null);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture = TestBed.createComponent(PublicNotePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
    currentUser$.next({ id: 'auth-1' });

    expect(navigateSpy).toHaveBeenCalledWith(['/', 'note-42'], { replaceUrl: true });
  });
});
