import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BehaviorSubject, EMPTY } from 'rxjs';
import { NotesPage } from './notes-page';
import { AuthService } from '../../services/auth/auth.service';
import { NotesAPIService } from '@notes/notes_service';
import { of } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { NoteEventsService } from '../../services/note/note-events.service';
import { NoteService } from '../../services/note/note.service';

describe('NotesPage', () => {
  let component: NotesPage;
  let fixture: ComponentFixture<NotesPage>;

  const authService = {
    logged$: new BehaviorSubject(false),
    currentUser$: new BehaviorSubject(null),
    login: vi.fn(),
  };

  const noteService = {
    notes$: of([]),
    getCurrentUserValue: vi.fn(),
    refreshPermissions: vi.fn(),
    updateNote: vi.fn(),
    createNote: vi.fn(),
    deleteNote: vi.fn(),
  };

  const notesApiService = {
    getNotes: vi.fn().mockReturnValue(of([])),
  };

  let authServiceSpy: any;
  let routerSpy: any;
  let noteServiceSpy: any;

  beforeEach(async () => {
    noteServiceSpy = {
      pinnedSection: of({ data: [], page: 0, hasMore: false, loading: false }),
      otherSection: of({ data: [], page: 0, hasMore: false, loading: false }),
      sharedSection: of({ data: [], page: 0, hasMore: false, loading: false }),
    };

    authServiceSpy = {
      logged$: new BehaviorSubject(false),
      currentUser$: new BehaviorSubject(null),
      login: vi.fn(),
    };

    noteServiceSpy.getCurrentUserValue = vi.fn().mockReturnValue({ id: 'u1' });
    noteServiceSpy.notes$ = of([]);

    notesApiService.getNotes.mockClear();
    notesApiService.getNotes.mockReturnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [NotesPage],
      providers: [
        provideRouter([]),
        provideTranslateService({ lang: 'en', fallbackLang: 'en' }),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: NoteService, useValue: noteServiceSpy },
        { provide: NotesAPIService, useValue: notesApiService },
        { provide: NoteEventsService, useValue: { noteEvents$: EMPTY, noteUpdatedEvents$: EMPTY, noteDeletedEvents$: EMPTY } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NotesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  const queryElement = (selector: string) => fixture.debugElement.query(By.css(selector));

  it('should render correctly', () => {
    expect(component).toBeTruthy();
    expect(queryElement('div')).toBeTruthy();
  });

  it('should show auth dialog when user is not logged in', () => {
    expect(component.logged()).toBe(false);
    expect(queryElement('app-auth-dialog')).toBeTruthy();
    expect(queryElement('app-notes-list')).toBeFalsy();
  });

  it('should show notes list when user is logged in', () => {
    authServiceSpy.logged$.next(true);
    fixture.detectChanges();

    expect(component.logged()).toBe(true);
    expect(queryElement('app-notes-list')).toBeTruthy();
    expect(queryElement('app-auth-dialog')).toBeFalsy();
  });
});
