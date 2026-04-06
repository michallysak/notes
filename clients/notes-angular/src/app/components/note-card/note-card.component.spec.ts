import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideTranslateService } from '@ngx-translate/core';
import { NoteCardComponent } from './note-card.component';
import { Note } from '../../types/note';

describe('NoteCardComponent', () => {
  let component: NoteCardComponent;
  let fixture: ComponentFixture<NoteCardComponent>;

  const createNote = (overrides: Partial<Note> = {}): Note =>
    ({
      id: '5',
      title: 'My note',
      content: 'Some content',
      pinned: false,
      created: new Date('2026-01-01T10:00:00Z'),
      updated: undefined,
      ...overrides,
    });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NoteCardComponent],
      providers: [
        provideTranslateService({
          lang: 'en',
          fallbackLang: 'en',
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NoteCardComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('note', createNote());
    fixture.detectChanges();
  });

  const queryElement = (selector: string) => fixture.debugElement.query(By.css(selector));

  it('should render correctly', () => {
    expect(component).toBeTruthy();
    expect(queryElement('p-card')).toBeTruthy();
    expect(queryElement('h3').nativeElement.textContent).toContain('My note');
    expect(queryElement('div.text-trim').nativeElement.textContent).toContain('Some content');
    expect(queryElement('app-note-change-datetime')).toBeTruthy();
  });

  it('should initialize menu items on init', () => {
    expect(component.items.length).toBe(1);
    expect(component.items[0].icon).toBe('pi pi-trash');
  });

  it('should call handleCardClick on card click', () => {
    const clickSpy = vi.spyOn(component, 'handleCardClick');

    queryElement('p-card').triggerEventHandler('click', {});

    expect(clickSpy).toHaveBeenCalled();
  });

  it('should call onPinClick from pin button click binding', () => {
    const pinClickSpy = vi.spyOn(component, 'onPinClick');
    const event = { stopPropagation: vi.fn() } as unknown as Event;

    fixture.debugElement.queryAll(By.css('p-button'))[0].triggerEventHandler('onClick', event);

    expect(pinClickSpy).toHaveBeenCalledWith(event);
  });

  it('should call onMenuClick from menu button click binding', () => {
    const menuClickSpy = vi.spyOn(component, 'onMenuClick');
    const event = { stopPropagation: vi.fn() } as unknown as Event;

    fixture.debugElement.queryAll(By.css('p-button'))[1].triggerEventHandler('onClick', event);

    expect(menuClickSpy).toHaveBeenCalled();
    expect(menuClickSpy.mock.calls[0][0]).toBe(event);
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

  it('should call noteService.deleteNote from menu item command', () => {
    const mockNoteService = { deleteNote: vi.fn().mockReturnValue({ subscribe: vi.fn() }) } as any;
    (component as any).noteService = mockNoteService;

    component.items[0].command?.({} as any);

    expect(mockNoteService.deleteNote).toHaveBeenCalledWith('5');
  });

  it('should log click action in handleCardClick', () => {
    const logSpy = vi.spyOn(console, 'log').mockImplementation(() => undefined);

    component.handleCardClick();

    expect(logSpy).toHaveBeenCalledWith('click', '5');
  });
});
