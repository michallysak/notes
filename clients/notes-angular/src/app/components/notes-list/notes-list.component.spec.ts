import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BehaviorSubject, of, Subject, throwError } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { NotesListComponent } from './notes-list.component';
import { NoteService } from '../../services/note/note.service';
import { Note } from '../../types/note';
import { NoteCardComponent } from '../note-card/note-card.component';

describe('NotesListComponent', () => {
  let component: NotesListComponent;
  let fixture: ComponentFixture<NotesListComponent>;
  let notesSubject: BehaviorSubject<Note[]>;

  const createNote = (overrides: Partial<Note> = {}): Note => ({
    id: '1',
    title: 'Title',
    content: 'Content',
    pinned: false,
    created: new Date('2026-01-01T10:00:00Z'),
    updated: undefined,
    ...overrides,
  });

  const noteService = {
    notes$: new BehaviorSubject<Note[]>([]),
    getPermissions: vi.fn().mockReturnValue(of([])),
    setNotePermissions: vi.fn().mockReturnValue(of({})),
    removeNoteAccess: vi.fn().mockReturnValue(of({})),
  };

  beforeEach(() => {
    // ensure updateNote mock exists for tests that call onPinClickPropagation
    (noteService as any).updateNote = vi.fn();
    noteService.getPermissions.mockReset();
    noteService.getPermissions.mockReturnValue(of([]));
    noteService.setNotePermissions.mockReset();
    noteService.setNotePermissions.mockReturnValue(of({}));
    noteService.removeNoteAccess.mockReset();
    noteService.removeNoteAccess.mockReturnValue(of({}));
  });

  beforeEach(async () => {
    notesSubject = new BehaviorSubject<Note[]>([]);
    noteService.notes$ = notesSubject;

    await TestBed.configureTestingModule({
      imports: [NotesListComponent],
      providers: [
        provideTranslateService({
          lang: 'en',
          fallbackLang: 'en',
        }),
        { provide: NoteService, useValue: noteService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NotesListComponent);
    component = fixture.componentInstance;
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
    expect(noteService.getPermissions).toHaveBeenCalledTimes(3);
  });

  it('marks cards as shared when permissions list is not empty', () => {
    noteService.getPermissions.mockImplementation((id: string) => of(id === '1' ? [{ userId: 'u-1' }] : []));

    notesSubject.next([createNote({ id: '1', pinned: true }), createNote({ id: '2', pinned: false })]);
    fixture.detectChanges();

    const cards = fixture.debugElement.queryAll(By.directive(NoteCardComponent));
    const firstCard = cards[0].componentInstance as NoteCardComponent;
    const secondCard = cards[1].componentInstance as NoteCardComponent;

    expect(firstCard.isShared).toBe(true);
    expect(secondCard.isShared).toBe(false);
  });

  it('falls back to shared=false when getPermissions fails for a note', () => {
    noteService.getPermissions.mockImplementation((id: string) =>
      id === 'failed-id' ? throwError(() => new Error('permissions fail')) : of([{ userId: 'u-1' }]),
    );

    notesSubject.next([
      createNote({ id: 'ok-id', pinned: true }),
      createNote({ id: 'failed-id', pinned: false }),
    ]);
    fixture.detectChanges();

    expect(component.sharedByNoteId()['ok-id']).toBe(true);
    expect(component.sharedByNoteId()['failed-id']).toBe(false);
  });

  it('ignores stale shared-status responses when requestId changed', () => {
    const permissions$ = new Subject<any[]>();
    noteService.getPermissions.mockReturnValue(permissions$.asObservable());
    component.sharedByNoteId.set({ keep: true });

    (component as any).loadSharedStatus([createNote({ id: 'stale-id' })]);
    (component as any).sharedStatusLoadId += 1; // simulate a newer request id before stale response arrives

    permissions$.next([{ userId: 'u-1' }]);
    permissions$.complete();

    expect(component.sharedByNoteId()).toEqual({ keep: true });
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

  it('should open dialog when noteCardClick is called', () => {
    const note = createNote({ id: '5' });

    component.noteCardClick(note);
    fixture.detectChanges();

    expect(component.clickNote().visible).toBe(true);
    expect((component.clickNote() as any).note?.id).toBe('5');
    // dialog should be present in template
    expect(queryElement('app-note-change-dialog')).toBeTruthy();
  });

  it('should close dialog when visibleChange event is triggered on dialog', () => {
    // open dialog first
    component.clickNote.set({ visible: true, note: createNote() });
    fixture.detectChanges();

    const dialogDe = queryElement('app-note-change-dialog');
    expect(dialogDe).toBeTruthy();

    // trigger the visibleChange output
    dialogDe.triggerEventHandler('visibleChange', false);
    fixture.detectChanges();

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

  it('should close share dialog when visibleChange event is triggered', () => {
    component.shareNote.set({ visible: true, note: createNote({ id: '16' }) });
    fixture.detectChanges();

    const dialogDe = queryElement('app-note-share-dialog');
    expect(dialogDe).toBeTruthy();

    dialogDe.triggerEventHandler('visibleChange', false);
    fixture.detectChanges();

    expect(component.shareNote().visible).toBe(false);
  });

  it('template @if blocks render when signals are set before first change detection', () => {
    // set signals on the existing component and trigger change detection
    component.clickNote.set({ visible: true, note: createNote({ id: '10' }) });
    component.pinnedNotes.set([createNote({ id: '1', pinned: true })]);
    component.otherNotes.set([createNote({ id: '2', pinned: false })]);

    fixture.detectChanges();

    // dialog should be rendered by the @if (clickNote()) block
    expect(fixture.debugElement.query(By.css('app-note-change-dialog'))).toBeTruthy();

    // both pinned and other notes should render app-note-card items
    const cards = fixture.debugElement.queryAll(By.css('app-note-card'));
    expect(cards.length).toBe(2);
  });

  it('renders click/share dialogs and binds note/isShared/onClick for pinned and other cards', () => {
    const pinned = createNote({ id: 'p-1', pinned: true });
    const other = createNote({ id: 'o-1', pinned: false });
    component.clickNote.set({ visible: true, note: pinned });
    component.shareNote.set({ visible: true, note: other });
    component.pinnedNotes.set([pinned]);
    component.otherNotes.set([other]);
    component.sharedByNoteId.set({ 'p-1': true, 'o-1': false });

    const noteCardClickSpy = vi.spyOn(component, 'noteCardClick');
    fixture.detectChanges();

    expect(fixture.debugElement.query(By.css('app-note-change-dialog'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('app-note-share-dialog'))).toBeTruthy();

    const cards = fixture.debugElement.queryAll(By.directive(NoteCardComponent));
    expect(cards.length).toBe(2);

    const pinnedCard = cards[0].componentInstance as NoteCardComponent;
    const otherCard = cards[1].componentInstance as NoteCardComponent;
    expect(pinnedCard.note.id).toBe('p-1');
    expect(otherCard.note.id).toBe('o-1');
    expect(pinnedCard.isShared).toBe(true);
    expect(otherCard.isShared).toBe(false);

    pinnedCard.onClick.emit(pinned);
    otherCard.onClick.emit(other);
    expect(noteCardClickSpy).toHaveBeenCalledWith(pinned);
    expect(noteCardClickSpy).toHaveBeenCalledWith(other);
  });

  it('should not call updateNote when note is falsy or missing id', () => {
    const updateSpy = vi.spyOn(noteService as any, 'updateNote');

    // call with null and with note missing id
    (component as any).onPinClickPropagation(null as any);
    (component as any).onPinClickPropagation({} as any);

    expect(updateSpy).not.toHaveBeenCalled();
  });

  it('should call updateNote and log on success and log error on failure', () => {
    const successNote: Note = createNote({ id: '7', pinned: false });
    // success case
    (noteService as any).updateNote = vi.fn().mockReturnValue(of({}));
    const logSpy = vi.spyOn(console, 'log').mockImplementation(() => undefined);

    component.onPinClickPropagation(successNote);
    expect((noteService as any).updateNote).toHaveBeenCalledWith('7', { pinned: true });
    expect(logSpy).toHaveBeenCalledWith('updated pinned state');

    // error case
    (noteService as any).updateNote = vi.fn().mockReturnValue(throwError(() => new Error('fail')));
    const errSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    component.onPinClickPropagation(successNote);

    return new Promise<void>((resolve) => {
      setTimeout(() => {
        expect(errSpy).toHaveBeenCalled();
        // restore spies
        logSpy.mockRestore();
        errSpy.mockRestore();
        resolve();
      }, 0);
    });
  });
});


