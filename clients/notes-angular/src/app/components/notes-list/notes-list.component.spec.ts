import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BehaviorSubject, of, Subject, throwError } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { NotesListComponent } from './notes-list.component';
import { NoteService } from '../../services/note/note.service';
import { Note } from '../../types/note';
import { NoteCardComponent } from '../note-card/note-card.component';
import { AuthService } from '../../services/auth/auth.service';
import { signal } from '@angular/core';

describe('NotesListComponent', () => {
  let component: NotesListComponent;
  let fixture: ComponentFixture<NotesListComponent>;
  let notesSubject: BehaviorSubject<Note[]>;
  let currentUserSignal: any;

  const createNote = (overrides: Partial<Note> = {}): Note => ({
    id: '1',
    authorId: 'auth-1',
    title: 'Title',
    content: 'Content',
    created: new Date().toISOString() as any,
    updated: new Date().toISOString() as any,
    pinned: false,
    shared: false,
    canEdit: true,
    ...overrides,
  });

  const noteService = {
    notes$: new BehaviorSubject<Note[]>([]),
    getPermissions: vi.fn().mockReturnValue(of([])),
    setNotePermissions: vi.fn().mockReturnValue(of({})),
    removeNoteAccess: vi.fn().mockReturnValue(of({})),
    refreshPermissions: vi.fn(),
    updateNote: vi.fn().mockReturnValue(of({})),
  };

  beforeEach(() => {
    noteService.updateNote.mockReset();
    noteService.updateNote.mockReturnValue(of({}));
    noteService.getPermissions.mockReset();
    noteService.getPermissions.mockReturnValue(of([]));
    noteService.setNotePermissions.mockReset();
    noteService.setNotePermissions.mockReturnValue(of({}));
    noteService.removeNoteAccess.mockReset();
    noteService.removeNoteAccess.mockReturnValue(of({}));
    noteService.refreshPermissions.mockReset();
  });

  beforeEach(async () => {
    notesSubject = new BehaviorSubject<Note[]>([]);
    noteService.notes$ = notesSubject;
    currentUserSignal = signal({ id: 'auth-1' });

    await TestBed.configureTestingModule({
      imports: [NotesListComponent],
      providers: [
        provideTranslateService({
          lang: 'en',
          fallbackLang: 'en',
        }),
        { provide: NoteService, useValue: noteService },
        { provide: AuthService, useValue: { currentUser$: of({ id: 'auth-1' }) } },
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
    notesSubject.next([
      createNote({ id: '1', pinned: true }),
      createNote({ id: '2', pinned: false }),
      createNote({ id: '3', pinned: true }),
    ]);
    fixture.detectChanges();

    expect(component.pinnedNotes().length).toBe(2);
    expect(component.otherNotes().length).toBe(1);
    expect(queryElements('app-note-card').length).toBe(3);
  });

  it('should unsubscribe from notes stream on destroy', () => {
    notesSubject.next([createNote({ id: '1', pinned: true })]);
    fixture.detectChanges();
    const pinnedBeforeDestroy = component.pinnedNotes().length;

    component.ngOnDestroy();
    notesSubject.next([createNote({ id: '2', pinned: false })]);

    expect(component.pinnedNotes().length).toBe(pinnedBeforeDestroy);
  });

  it('should call openCreate when create button is clicked', () => {
    const openCreateSpy = vi.spyOn(component, 'openCreate');

    queryElement('.create-btn p-button').triggerEventHandler('onClick', {});

    expect(openCreateSpy).toHaveBeenCalled();
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
    const updateSpy = (noteService.updateNote as any);

    component.onPinClickPropagation(successNote);
    expect(updateSpy).toHaveBeenCalledWith('7', { pinned: true });
  });

  it('updates permissions via NoteService on state change', () => {
    const refreshSpy = vi.spyOn(noteService, 'refreshPermissions');
    component.onSharedStateChanged({ noteId: '10', isShared: true });
    expect(refreshSpy).toHaveBeenCalledWith('10');
  });

  it('logs error when updateNote fails on pin click', () => {
    noteService.updateNote.mockReturnValue(throwError(() => new Error('Pin failed')));
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
    const clickSpy = vi.spyOn(component, 'noteCardClick');
    notesSubject.next([createNote({ id: '1' })]);
    fixture.detectChanges();

    const card = queryElement('app-note-card');
    const note = component.otherNotes()[0];
    card.triggerEventHandler('onClick', note);

    expect(clickSpy).toHaveBeenCalledWith(note);
  });

  it('calls noteCardClick when a pinned note card is clicked', () => {
    const clickSpy = vi.spyOn(component, 'noteCardClick');
    notesSubject.next([createNote({ id: '1', pinned: true })]);
    fixture.detectChanges();

    const card = queryElement('app-note-card');
    const note = component.pinnedNotes()[0];
    card.triggerEventHandler('onClick', note);

    expect(clickSpy).toHaveBeenCalledWith(note);
  });

  it('calls noteCardClick when an other note card is clicked', () => {
    const clickSpy = vi.spyOn(component, 'noteCardClick');
    notesSubject.next([createNote({ id: '1', pinned: false })]);
    fixture.detectChanges();

    const card = queryElement('app-note-card');
    const note = component.otherNotes()[0];
    card.triggerEventHandler('onClick', note);

    expect(clickSpy).toHaveBeenCalledWith(note);
  });

  it('calls onPinClickPropagation when a note card emits pinClick', () => {
    const pinSpy = vi.spyOn(component, 'onPinClickPropagation');
    notesSubject.next([createNote({ id: '1', pinned: false })]);
    fixture.detectChanges();

    const card = queryElement('app-note-card');
    const note = component.otherNotes()[0];
    card.triggerEventHandler('pinClick', note);

    expect(pinSpy).toHaveBeenCalledWith(note);
  });

  it('calls noteCardShareClick when a note card emits shareClick', () => {
    const shareSpy = vi.spyOn(component, 'noteCardShareClick');
    notesSubject.next([createNote({ id: '1' })]);
    fixture.detectChanges();

    const card = queryElement('app-note-card');
    const note = component.otherNotes()[0];
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
    const pinSpy = vi.spyOn(component, 'onPinClickPropagation');
    notesSubject.next([createNote({ id: '1', pinned: true })]);
    fixture.detectChanges();

    const card = queryElement('app-note-card');
    const note = component.pinnedNotes()[0];
    card.triggerEventHandler('pinClick', note);

    expect(pinSpy).toHaveBeenCalledWith(note);
  });

  it('calls noteCardShareClick when a pinned note card emits shareClick', () => {
    const shareSpy = vi.spyOn(component, 'noteCardShareClick');
    notesSubject.next([createNote({ id: '1', pinned: true })]);
    fixture.detectChanges();

    const card = queryElement('app-note-card');
    const note = component.pinnedNotes()[0];
    card.triggerEventHandler('shareClick', note);

    expect(shareSpy).toHaveBeenCalledWith(note);
  });

  it('calls onPinClickPropagation when an other note card emits pinClick', () => {
    const pinSpy = vi.spyOn(component, 'onPinClickPropagation');
    notesSubject.next([createNote({ id: '1', pinned: false })]);
    fixture.detectChanges();

    const card = queryElement('app-note-card');
    const note = component.otherNotes()[0];
    card.triggerEventHandler('pinClick', note);

    expect(pinSpy).toHaveBeenCalledWith(note);
  });

  it('calls noteCardShareClick when an other note card emits shareClick', () => {
    const shareSpy = vi.spyOn(component, 'noteCardShareClick');
    notesSubject.next([createNote({ id: '1', pinned: false })]);
    fixture.detectChanges();

    const card = queryElement('app-note-card');
    const note = component.otherNotes()[0];
    card.triggerEventHandler('shareClick', note);

    expect(shareSpy).toHaveBeenCalledWith(note);
  });
});
