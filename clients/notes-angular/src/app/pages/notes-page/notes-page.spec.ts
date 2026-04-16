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

describe('NotesPage', () => {
  let component: NotesPage;
  let fixture: ComponentFixture<NotesPage>;
  let loggedSubject: BehaviorSubject<boolean>;

  const authService = {
    logged$: new BehaviorSubject(false),
    login: vi.fn(),
  };

  const notesApiService = {
    getNotes: vi.fn().mockReturnValue(of([])),
  };

  beforeEach(async () => {
    loggedSubject = new BehaviorSubject(false);
    authService.logged$ = loggedSubject;

    await TestBed.configureTestingModule({
      imports: [NotesPage],
      providers: [
        provideRouter([]),
        provideTranslateService({
          lang: 'en',
          fallbackLang: 'en',
        }),
        { provide: AuthService, useValue: authService },
        { provide: NotesAPIService, useValue: notesApiService },
        { provide: NoteEventsService, useValue: { noteEvents$: EMPTY } },
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
    loggedSubject.next(true);
    fixture.detectChanges();

    expect(component.logged()).toBe(true);
    expect(queryElement('app-notes-list')).toBeTruthy();
    expect(queryElement('app-auth-dialog')).toBeFalsy();
  });
});

