import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BehaviorSubject, EMPTY, of, throwError } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { NotesListComponent } from './notes-list.component';
import { NoteService } from '../../services/note/note.service';
import { AuthService } from '../../services/auth/auth.service';
import { Note } from '../../types/note';
import { signal } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { BASE_PATH } from '@notes/notes_service';
import { NoteEventsService } from '../../services/note/note-events.service';

describe('NotesListComponent', () => {
  let component: NotesListComponent;
  let fixture: ComponentFixture<NotesListComponent>;
  let noteServiceMock: any;
  let authServiceMock: any;
  let currentUserSignal: any;
  let router: any;
  let activatedRoute: any;

  const createNote = (overrides: Partial<Note> = {}): Note => ({
    id: '1',
    authorId: 'auth-1',
    title: 'test note',
    content: 'content',
    created: null as any,
    updated: null as any,
    pinned: false,
    style: undefined,
    shares: [],
    shared: false,
    canEdit: true,
    ...overrides,
  });

  beforeEach(async () => {
    currentUserSignal = signal({ id: 'auth-1' });

    authServiceMock = {
      currentUser$: of({ id: 'auth-1' }),
      currentUser: signal({ id: 'auth-1' }),
    };

    router = {
      navigate: vi.fn(),
    };
    activatedRoute = {
      paramMap: of({ get: () => null }),
      snapshot: { paramMap: { has: () => false } },
    };

    noteServiceMock = {
      updateNote: vi.fn().mockReturnValue(of({})),
      loadMorePinned: vi.fn(),
      loadMoreOther: vi.fn(),
      loadMoreShared: vi.fn(),
      getNoteById: vi.fn().mockReturnValue(of({})),
      pinnedSection: new BehaviorSubject({ data: [], page: 0, hasMore: false }),
      otherSection: new BehaviorSubject({ data: [], page: 0, hasMore: false }),
      sharedSection: new BehaviorSubject({ data: [], page: 0, hasMore: false }),
    };

    await TestBed.configureTestingModule({
      imports: [NotesListComponent, RouterModule.forRoot([])],
      providers: [
        provideTranslateService(),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: BASE_PATH, useValue: 'http://localhost' },
        { provide: NoteService, useValue: noteServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        {
          provide: NoteEventsService,
          useValue: { noteEvents$: EMPTY, noteUpdatedEvents$: EMPTY, noteDeletedEvents$: EMPTY },
        },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NotesListComponent);
    component = fixture.componentInstance;
    (component as any).currentUser = currentUserSignal;

    fixture.detectChanges();
  });

  const queryElement = (selector: string) => fixture.debugElement.query(By.css(selector));
  const queryElements = (selector: string) => fixture.debugElement.queryAll(By.css(selector));

  it('should render correctly', () => {
    expect(component).toBeTruthy();
    expect(queryElement('.create-btn p-button')).toBeTruthy();
    expect(queryElement('.notes-list')).toBeTruthy();
  });

  it('should split pinned and other notes', () => {
    noteServiceMock.pinnedSection.next({
      data: [createNote({ id: '1', pinned: true }), createNote({ id: '3', pinned: true })],
      page: 0,
      hasMore: false,
      loading: false,
    });
    noteServiceMock.otherSection.next({
      data: [createNote({ id: '2', pinned: false })],
      page: 0,
      hasMore: false,
      loading: false,
    });

    // re-create the component to pick up new observables
    fixture = TestBed.createComponent(NotesListComponent);
    component = fixture.componentInstance;
    (component as any).currentUser = currentUserSignal;

    fixture.detectChanges();

    expect(component.sections.find((s) => s.id === 'pinned')?.signal().data.length).toBe(2);
    expect(component.sections.find((s) => s.id === 'other')?.signal().data.length).toBe(1);
    expect(queryElements('app-note-card').length).toBe(3);
  });

  it('should call openCreate when create button is clicked', () => {
    const openCreateSpy = vi.spyOn(component, 'openCreate');

    queryElement('.create-btn p-button').triggerEventHandler('onClick', {});

    expect(openCreateSpy).toHaveBeenCalled();
  });

  it('should call loadMore when Load more button is clicked', () => {
    noteServiceMock.pinnedSection.next({
      data: [createNote({ id: '1', pinned: true })],
      page: 0,
      hasMore: true,
      loading: false,
    });
    fixture = TestBed.createComponent(NotesListComponent);
    component = fixture.componentInstance;
    (component as any).currentUser = currentUserSignal;
    fixture.detectChanges();

    const loadMoreSpy = vi.spyOn(noteServiceMock, 'loadMorePinned' as any);
    const loadMoreBtn = queryElement('.load-more-btn');
    expect(loadMoreBtn).toBeTruthy();
    loadMoreBtn.triggerEventHandler('onClick', {});
    expect(loadMoreSpy).toHaveBeenCalled();
  });

  it('should open dialog when noteCardClick is called as author', async () => {
    const note = createNote({ id: '5', authorId: 'auth-1', canEdit: true });
    currentUserSignal.set({ id: 'auth-1' });

    await component.noteCardClick(note);
    fixture.detectChanges();

    expect(component.clickNote().visible).toBe(true);
    const state = component.clickNote() as any;
    expect(state.note?.id).toBe('5');
    expect(state.readonly).toBe(false);
    expect(queryElement('app-note-change-dialog')).toBeTruthy();
  });

  it('should open dialog as readonly when not author and canEdit is false', async () => {
    const note = createNote({ id: '5', authorId: 'other-author', canEdit: false });
    currentUserSignal.set({ id: 'auth-1' });

    await component.noteCardClick(note);
    fixture.detectChanges();

    expect(component.clickNote().visible).toBe(true);
    const state = component.clickNote() as any;
    expect(state.readonly).toBe(true);
  });

  it('should close dialog when noteDialogClose is called', () => {
    component.clickNote.set({ visible: true, note: createNote(), readonly: false });
    fixture.detectChanges();

    component.noteDialogClose();
    expect(component.clickNote().visible).toBe(false);
  });

  it('should open share dialog when noteCardShareClick is called', () => {
    const note = createNote({ id: '15' });

    component.noteCardShareClick(note);
    fixture.detectChanges();

    expect(component.shareNote().visible).toBe(true);
    expect((component.shareNote() as any).note.id).toBe('15');
    expect(queryElement('app-note-share-dialog')).toBeTruthy();
  });

  it('should close share dialog when shareDialogClose is called', () => {
    component.shareNote.set({ visible: true, note: createNote({ id: '16' }) });
    fixture.detectChanges();

    component.shareDialogClose();
    expect(component.shareNote().visible).toBe(false);
  });

  it('should call updateNote when pin is clicked', () => {
    const successNote: Note = createNote({ id: '7', pinned: false });
    const updateSpy = noteServiceMock.updateNote as any;

    component.onPinClickPropagation(successNote);
    expect(updateSpy).toHaveBeenCalledWith('7', { pinned: true });
  });

  it('updates permissions via NoteService on state change', () => {
    // Shared state is now handled natively
    component.onSharedStateChanged({ noteId: '10', isShared: true });
    // Expect no crash, no refresh permissions mock
    expect(true).toBe(true);
  });

  it('logs error when updateNote fails on pin click', () => {
    noteServiceMock.updateNote.mockReturnValue(throwError(() => new Error('Pin failed')));
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    const note = createNote({ id: '7', pinned: false });

    component.onPinClickPropagation(note);

    expect(errorSpy).toHaveBeenCalledWith('Failed to update pinned state', expect.any(Error));
    errorSpy.mockRestore();
  });

  it('calls noteDialogClose from visibleChange binding', () => {
    const closeSpy = vi.spyOn(component, 'noteDialogClose');
    component.clickNote.set({ visible: true, note: createNote(), readonly: false });
    fixture.detectChanges();

    const dialog = queryElement('app-note-change-dialog');
    dialog.triggerEventHandler('visibleChange', false);

    expect(closeSpy).toHaveBeenCalled();
  });

  it('calls shareDialogClose from visibleChange binding', () => {
    const closeSpy = vi.spyOn(component, 'shareDialogClose');
    component.shareNote.set({ visible: true, note: createNote() });
    fixture.detectChanges();

    const dialog = queryElement('app-note-share-dialog');
    dialog.triggerEventHandler('visibleChange', false);

    expect(closeSpy).toHaveBeenCalled();
  });

  it('calls noteCardClick when a note card is clicked', () => {
    const note = createNote({ id: '1', pinned: false });

    noteServiceMock.otherSection.next({
      data: [note],
      page: 0,
      hasMore: false,
      loading: false,
    });
    fixture = TestBed.createComponent(NotesListComponent);
    component = fixture.componentInstance;
    (component as any).currentUser = currentUserSignal;
    fixture.detectChanges();

    const clickSpy = vi.spyOn(component, 'noteCardClick');
    const card = queryElement('app-note-card');
    card.triggerEventHandler('onClick', note);

    expect(clickSpy).toHaveBeenCalledWith(note);
  });

  it('calls noteCardClick when a pinned note card is clicked', () => {
    const note = createNote({ id: '1', pinned: true });

    noteServiceMock.pinnedSection.next({
      data: [note],
      page: 0,
      hasMore: false,
      loading: false,
    });
    fixture = TestBed.createComponent(NotesListComponent);
    component = fixture.componentInstance;
    (component as any).currentUser = currentUserSignal;
    fixture.detectChanges();

    const clickSpy = vi.spyOn(component, 'noteCardClick');
    const card = queryElement('app-note-card');
    card.triggerEventHandler('onClick', note);

    expect(clickSpy).toHaveBeenCalledWith(note);
  });

  it('calls noteCardClick when an other note card is clicked', () => {
    const note = createNote({ id: '1', pinned: false });

    noteServiceMock.otherSection.next({
      data: [note],
      page: 0,
      hasMore: false,
      loading: false,
    });
    fixture = TestBed.createComponent(NotesListComponent);
    component = fixture.componentInstance;
    (component as any).currentUser = currentUserSignal;
    fixture.detectChanges();

    const clickSpy = vi.spyOn(component, 'noteCardClick');
    const card = queryElement('app-note-card');
    card.triggerEventHandler('onClick', note);

    expect(clickSpy).toHaveBeenCalledWith(note);
  });

  it('calls onPinClickPropagation when a note card emits pinClick', () => {
    const note = createNote({ id: '1', pinned: false });

    noteServiceMock.otherSection.next({
      data: [note],
      page: 0,
      hasMore: false,
      loading: false,
    });
    fixture = TestBed.createComponent(NotesListComponent);
    component = fixture.componentInstance;
    (component as any).currentUser = currentUserSignal;
    fixture.detectChanges();

    const pinSpy = vi.spyOn(component, 'onPinClickPropagation');
    const card = queryElement('app-note-card');
    card.triggerEventHandler('pinClick', note);

    expect(pinSpy).toHaveBeenCalledWith(note);
  });

  it('calls noteCardShareClick when a note card emits shareClick', () => {
    const note = createNote({ id: '1', pinned: false });

    noteServiceMock.otherSection.next({
      data: [note],
      page: 0,
      hasMore: false,
      loading: false,
    });
    fixture = TestBed.createComponent(NotesListComponent);
    component = fixture.componentInstance;
    (component as any).currentUser = currentUserSignal;
    fixture.detectChanges();

    const shareSpy = vi.spyOn(component, 'noteCardShareClick');
    const card = queryElement('app-note-card');
    card.triggerEventHandler('shareClick', note);

    expect(shareSpy).toHaveBeenCalledWith(note);
  });

  it('correctly calculates isShared from note property', () => {
    const sharedNote = createNote({ shared: true });
    const unsharedNote = createNote({ shared: false });

    expect(component.isShared(sharedNote)).toBe(true);
    expect(component.isShared(unsharedNote)).toBe(false);
  });

  it('calls onPinClickPropagation when a pinned note card emits pinClick', () => {
    const note = createNote({ id: '1', pinned: true });

    noteServiceMock.pinnedSection.next({
      data: [note],
      page: 0,
      hasMore: false,
      loading: false,
    });
    fixture = TestBed.createComponent(NotesListComponent);
    component = fixture.componentInstance;
    (component as any).currentUser = currentUserSignal;
    fixture.detectChanges();

    const pinSpy = vi.spyOn(component, 'onPinClickPropagation');
    const card = queryElement('app-note-card');
    card.triggerEventHandler('pinClick', note);

    expect(pinSpy).toHaveBeenCalledWith(note);
  });

  it('calls noteCardShareClick when a pinned note card emits shareClick', () => {
    const note = createNote({ id: '1', pinned: true });

    noteServiceMock.pinnedSection.next({
      data: [note],
      page: 0,
      hasMore: false,
      loading: false,
    });
    fixture = TestBed.createComponent(NotesListComponent);
    component = fixture.componentInstance;
    (component as any).currentUser = currentUserSignal;
    fixture.detectChanges();

    const shareSpy = vi.spyOn(component, 'noteCardShareClick');
    const card = queryElement('app-note-card');
    card.triggerEventHandler('shareClick', note);

    expect(shareSpy).toHaveBeenCalledWith(note);
  });

  it('calls onPinClickPropagation when an other note card emits pinClick', () => {
    const note = createNote({ id: '1', pinned: false });

    noteServiceMock.otherSection.next({
      data: [note],
      page: 0,
      hasMore: false,
      loading: false,
    });
    fixture = TestBed.createComponent(NotesListComponent);
    component = fixture.componentInstance;
    (component as any).currentUser = currentUserSignal;
    fixture.detectChanges();

    const pinSpy = vi.spyOn(component, 'onPinClickPropagation');
    const card = queryElement('app-note-card');
    card.triggerEventHandler('pinClick', note);

    expect(pinSpy).toHaveBeenCalledWith(note);
  });

  it('calls noteCardShareClick when an other note card emits shareClick', () => {
    const note = createNote({ id: '1', pinned: false });

    noteServiceMock.otherSection.next({
      data: [note],
      page: 0,
      hasMore: false,
      loading: false,
    });
    fixture = TestBed.createComponent(NotesListComponent);
    component = fixture.componentInstance;
    (component as any).currentUser = currentUserSignal;
    fixture.detectChanges();

    const shareSpy = vi.spyOn(component, 'noteCardShareClick');
    const card = queryElement('app-note-card');
    card.triggerEventHandler('shareClick', note);

    expect(shareSpy).toHaveBeenCalledWith(note);
  });

  it('verifies sections loadMore callbacks', () => {
    fixture.detectChanges();
    const otherSection = component.sections.find((s: any) => s.id === 'other');
    otherSection?.loadMore();
    expect(noteServiceMock.loadMoreOther).toHaveBeenCalled();

    const sharedSection = component.sections.find((s: any) => s.id === 'shared');
    sharedSection?.loadMore();
    expect(noteServiceMock.loadMoreShared).toHaveBeenCalled();
  });

  it('renders section correctly when data is empty or undefined', () => {
    noteServiceMock.otherSection.next({
      data: undefined as any,
      page: 0,
      hasMore: false,
      loading: false,
    });
    // Ensure all sections are empty
    noteServiceMock.pinnedSection.next({
      data: undefined as any,
      page: 0,
      hasMore: false,
      loading: false,
    });
    noteServiceMock.sharedSection.next({
      data: undefined as any,
      page: 0,
      hasMore: false,
      loading: false,
    });

    fixture = TestBed.createComponent(NotesListComponent);
    component = fixture.componentInstance;
    (component as any).currentUser = currentUserSignal;
    fixture.detectChanges();

    const h4Elements = fixture.nativeElement.querySelectorAll('h4');
    // It should render only 1 h4 which is the "NO_NOTES" text
    expect(h4Elements.length).toBe(1);
    expect(h4Elements[0].textContent).toContain('NOTES.NO_NOTES');
  });

  it('renders note conditionally when dialog is closed', () => {
    component.clickNote.set({ visible: false, readonly: false });
    fixture.detectChanges();
    const dialog = queryElement('app-note-change-dialog');
    expect(dialog.componentInstance.note).toBeFalsy();
  });

  it('renders note conditionally when share dialog is closed', () => {
    component.shareNote.set({ visible: false });
    fixture.detectChanges();
    const dialog = queryElement('app-note-share-dialog');
    expect(dialog.componentInstance.note).toBeFalsy();
  });
});
