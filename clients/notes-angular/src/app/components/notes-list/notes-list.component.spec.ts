import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BehaviorSubject } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { NotesListComponent } from './notes-list.component';
import { NoteService } from '../../services/note/note.service';
import { Note } from '../../types/note';

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
  };

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
});


