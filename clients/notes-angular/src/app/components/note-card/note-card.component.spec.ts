import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideTranslateService } from '@ngx-translate/core';
import { BehaviorSubject, EMPTY, of, throwError } from 'rxjs';
import { NoteCardComponent } from './note-card.component';
import { NoteEventsService } from '../../services/note/note-events.service';
import { NoteService } from '../../services/note/note.service';
import { AuthService } from '../../services/auth/auth.service';
import { Note } from '../../types/note';

describe('NoteCardComponent', () => {
  let component: NoteCardComponent;
  let fixture: ComponentFixture<NoteCardComponent>;
  const noteService = {
    deleteNote: vi.fn(),
  };
  const authService = {
    currentUser$: new BehaviorSubject<{ id: string } | null>({ id: 'auth-1' }),
  };

  const createNote = (overrides: Partial<Note> = {}): Note => ({
    id: '1',
    authorId: 'auth-1',
    title: 'Test Note',
    content: 'Content',
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
    noteService.deleteNote.mockReset();
    noteService.deleteNote.mockReturnValue(of(undefined));
    authService.currentUser$.next({ id: 'auth-1' });

    await TestBed.configureTestingModule({
      imports: [NoteCardComponent],
      providers: [
        provideTranslateService({ lang: 'en', fallbackLang: 'en' }),
        { provide: NoteEventsService, useValue: { noteEvents$: EMPTY } },
        { provide: NoteService, useValue: noteService },
        { provide: AuthService, useValue: authService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NoteCardComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('note', createNote({ title: 'My note', content: 'Some content', id: '5', authorId: 'auth-1' }));
    fixture.detectChanges();
    await fixture.whenStable();
  });

  const queryElement = (selector: string) => fixture.debugElement.query(By.css(selector));

  it('should render correctly', () => {
    expect(component).toBeTruthy();
    expect(queryElement('p-card')).toBeTruthy();
    expect(queryElement('h3').nativeElement.textContent).toContain('My note');
    expect(queryElement('div.text-trim').nativeElement.textContent).toContain('Some content');
    expect(queryElement('app-note-change-datetime')).toBeTruthy();
  });

  it('renders shared badge when shared is true', async () => {
    fixture.componentRef.setInput('note', createNote({ shared: true }));
    fixture.detectChanges();
    await fixture.whenStable();

    expect(queryElement('.shared-badge')).toBeTruthy();
  });

  it('does not render shared badge when shared is false', async () => {
    fixture.componentRef.setInput('note', createNote({ shared: false }));
    fixture.detectChanges();
    await fixture.whenStable();

    expect(queryElement('.shared-badge')).toBeFalsy();
  });

  it('renders controls and menu when isAuthor is true', async () => {
    const note = createNote({ authorId: 'auth-1' });
    fixture.componentRef.setInput('note', note);
    authService.currentUser$.next({ id: 'auth-1' });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(queryElement('.controls')).toBeTruthy();
    expect(queryElement('p-menu')).toBeTruthy();
  });

  it('does not render controls and menu when isAuthor is false', async () => {
    const note = createNote({ authorId: 'auth-1' });
    fixture.componentRef.setInput('note', note);
    authService.currentUser$.next({ id: 'other-user' });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(queryElement('.controls')).toBeFalsy();
    expect(queryElement('p-menu')).toBeFalsy();
  });

  it('should initialize menu items on init', () => {
    expect(component.items.length).toBe(2);
    expect(component.items[0].icon).toBe('pi pi-share-alt');
    expect(component.items[1].icon).toBe('pi pi-trash');
  });

  it('should emit shareClick from share menu item command', () => {
    const shareSpy = vi.spyOn(component.shareClick, 'emit');

    component.items[0].command?.({} as any);

    expect(shareSpy).toHaveBeenCalledWith(expect.objectContaining({ id: '5' }));
  });

  it('should call handleCardClick on card click', () => {
    const clickSpy = vi.spyOn(component, 'handleCardClick');

    const cardDe = queryElement('p-card');
    cardDe.triggerEventHandler('click', new MouseEvent('click'));

    expect(clickSpy).toHaveBeenCalled();
  });

  it('should call onPinClick from pin button click binding', () => {
    const pinClickSpy = vi.spyOn(component, 'onPinClick');
    const event = new MouseEvent('click');
    vi.spyOn(event, 'stopPropagation');

    component.onPinClick(event);

    expect(pinClickSpy).toHaveBeenCalledWith(event);
  });

  it('should call onMenuClick from menu button click binding', () => {
    const menuClickSpy = vi.spyOn(component, 'onMenuClick');
    const event = new MouseEvent('click');
    const menu = { toggle: vi.fn() };

    component.onMenuClick(event, menu);

    expect(menuClickSpy).toHaveBeenCalledWith(event, menu);
  });

  it('should stop event propagation on pin click', () => {
    const stopPropagation = vi.fn();
    component.onPinClick({ stopPropagation } as unknown as Event);

    expect(stopPropagation).toHaveBeenCalled();
  });

  it('should toggle menu and stop propagation on menu click', () => {
    const stopPropagation = vi.fn();
    const menu = { toggle: vi.fn() };
    const event = { stopPropagation } as unknown as Event;

    component.onMenuClick(event, menu);

    expect(stopPropagation).toHaveBeenCalled();
    expect(menu.toggle).toHaveBeenCalledWith(event);
  });

  it('calls handleCardClick when the p-card is clicked', () => {
    const handleSpy = vi.spyOn(component, 'handleCardClick');
    queryElement('p-card').triggerEventHandler('click', new MouseEvent('click'));
    expect(handleSpy).toHaveBeenCalled();
  });

  it('calls onPinClick when the pin button is clicked', async () => {
    const note = createNote({ authorId: 'auth-1' });
    fixture.componentRef.setInput('note', note);
    authService.currentUser$.next({ id: 'auth-1' });
    fixture.detectChanges();
    await fixture.whenStable();

    const onPinSpy = vi.spyOn(component, 'onPinClick');
    const pinBtn = fixture.debugElement.query(By.css('div.controls p-button:first-child'));
    expect(pinBtn).toBeTruthy();
    pinBtn.triggerEventHandler('onClick', new MouseEvent('click'));
    expect(onPinSpy).toHaveBeenCalled();
  });

  it('calls onMenuClick when the menu button is clicked', async () => {
    const note = createNote({ authorId: 'auth-1' });
    fixture.componentRef.setInput('note', note);
    authService.currentUser$.next({ id: 'auth-1' });
    fixture.detectChanges();
    await fixture.whenStable();

    const onMenuSpy = vi.spyOn(component, 'onMenuClick');
    const menuBtn = fixture.debugElement.query(By.css('div.controls p-button:last-child'));
    expect(menuBtn).toBeTruthy();
    menuBtn.triggerEventHandler('onClick', new MouseEvent('click'));
    expect(onMenuSpy).toHaveBeenCalled();
  });

  it('should call noteService.deleteNote from menu item command', () => {
    noteService.deleteNote.mockReturnValue({ subscribe: vi.fn() } as any);

    component.items[1].command?.({} as any);

    expect(noteService.deleteNote).toHaveBeenCalledWith('5');
  });

  it('logs successful note deletion from menu command', () => {
    const logSpy = vi.spyOn(console, 'log').mockImplementation(() => undefined);

    component.items[1].command?.({} as any);

    expect(logSpy).toHaveBeenCalledWith('deleted', '5');
    logSpy.mockRestore();
  });

  it('logs delete errors from menu command', () => {
    noteService.deleteNote.mockReturnValue(throwError(() => new Error('fail')));
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    component.items[1].command?.({} as any);

    expect(errorSpy).toHaveBeenCalledWith('delete failed', expect.any(Error));
    errorSpy.mockRestore();
  });


  it('should log click action in handleCardClick', () => {
    const logSpy = vi.spyOn(console, 'log').mockImplementation(() => undefined);

    component.handleCardClick();

    expect(logSpy).toHaveBeenCalledWith('click', '5');
  });
});
