import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { BehaviorSubject, EMPTY, of } from 'rxjs';
import { NotesPage } from './notes-page';
import { AuthService } from '../../services/auth/auth.service';
import { NotesAPIService } from '@notes/notes_service';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { NoteEventsService } from '../../services/note/note-events.service';
import { NoteService } from '../../services/note/note.service';
import { vi } from 'vitest';

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
  let noteServiceSpy: any;
  let router: any;

  beforeEach(async () => {
    noteServiceSpy = {
      pinnedSection: new BehaviorSubject({ data: [], page: 0, hasMore: false, loading: false }),
      otherSection: new BehaviorSubject({ data: [], page: 0, hasMore: false, loading: false }),
      sharedSection: new BehaviorSubject({ data: [], page: 0, hasMore: false, loading: false }),
      notes$: of([]),
    };

    authServiceSpy = {
      logged$: new BehaviorSubject(false),
      currentUser$: new BehaviorSubject(null),
      login: vi.fn(),
      logout: vi.fn(),
    };

    router = {
      navigate: vi.fn().mockResolvedValue(true),
    };

    noteServiceSpy.getCurrentUserValue = vi.fn().mockReturnValue({ id: 'u1' });
    noteServiceSpy.notes$ = of([]);

    notesApiService.getNotes.mockClear();
    notesApiService.getNotes.mockReturnValue(of([]));

    let domainEventsSubject = new BehaviorSubject<any>({});
    await TestBed.configureTestingModule({
      imports: [NotesPage],
      providers: [
        provideRouter([{ path: 'login', component: NotesPage }]),
        provideTranslateService({ lang: 'en', fallbackLang: 'en' }),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: NoteService, useValue: noteServiceSpy },
        { provide: NotesAPIService, useValue: notesApiService },
        { provide: NoteEventsService, useValue: { domainEvents$: domainEventsSubject } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NotesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  const queryElement = (selector: string) => fixture.debugElement.query(By.css(selector));

  it('should render correctly', () => {
    expect(component).toBeTruthy();
    expect(queryElement('.notes-page-container')).toBeTruthy();
  });

  it('should show auth dialog when user is not logged in', () => {
    expect(component.logged()).toBe(false);
    expect(queryElement('app-auth-dialog')).toBeTruthy();
    expect(queryElement('app-notes-list')).toBeFalsy();
  });

  it('should show notes list and header when user is logged in', () => {
    authServiceSpy.logged$.next(true);
    fixture.detectChanges();

    expect(component.logged()).toBe(true);
    expect(queryElement('app-notes-list')).toBeTruthy();
    expect(queryElement('app-auth-dialog')).toBeFalsy();
    expect(queryElement('app-header')).toBeTruthy();
  });

  it('should render the notes list when user is logged in', () => {
    authServiceSpy.logged$.next(true);
    fixture.detectChanges();

    expect(component.logged()).toBe(true);
    expect(queryElement('app-notes-list')).toBeTruthy();
  });


  it('should call logout when logout is called', () => {
    const logoutSpy = vi.spyOn(component, 'logout');
    component.logout();

    expect(logoutSpy).toHaveBeenCalled();
  });
});

