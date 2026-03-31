import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import * as AuthModule from '../../services/auth/auth.service';
import { HeaderComponent } from './header.component';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let currentUserSubject: BehaviorSubject<any | null>;

  const authService = {
    currentUser$: new BehaviorSubject<any | null>(null),
    logout: vi.fn(),
  };

  const router = {
    navigate: vi.fn(),
  };

  beforeEach(async () => {
    currentUserSubject = new BehaviorSubject<any | null>(null);
    authService.currentUser$ = currentUserSubject;
    authService.logout.mockClear();
    router.navigate.mockClear();

    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [
        provideTranslateService({
          lang: 'en',
          fallbackLang: 'en',
        }),
        { provide: AuthModule.AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  const queryElement = (selector: string) => fixture.debugElement.query(By.css(selector));

  it('should render correctly', () => {
    expect(component).toBeTruthy();
    expect(queryElement('header.app-header')).toBeTruthy();
    expect(queryElement('div:nth-child(1) h1')).toBeTruthy();
    expect(queryElement('div:nth-child(2)')).toBeTruthy();
  });

  it('should render user info when current user exists', () => {
    currentUserSubject.next({ email: 'user@example.com' });
    fixture.detectChanges();

    expect(queryElement('.user-info')).toBeTruthy();
    expect(queryElement('.user-info span').nativeElement.textContent).toContain('user@example.com');
    expect(queryElement('.user-info p-button')).toBeTruthy();
  });

  it('should hide user info when current user is null', () => {
    currentUserSubject.next(null);
    fixture.detectChanges();

    expect(queryElement('.user-info')).toBeFalsy();
  });

  it('should logout and navigate to login on logout button click', () => {
    currentUserSubject.next({ email: 'user@example.com' });
    fixture.detectChanges();

    queryElement('.user-info p-button').triggerEventHandler('onClick', {});

    expect(authService.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});

