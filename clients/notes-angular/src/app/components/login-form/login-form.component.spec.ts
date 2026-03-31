import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginFormComponent } from './login-form.component';
import * as AuthModule from '../../services/auth/auth.service';

describe('LoginFormComponent', () => {
  let component: LoginFormComponent;
  let fixture: ComponentFixture<LoginFormComponent>;
  let router: Router;

  const authService = {
    login: vi.fn(),
  };

  beforeEach(async () => {
    authService.login.mockReset();

    await TestBed.configureTestingModule({
      imports: [LoginFormComponent],
      providers: [
        provideRouter([]),
        provideTranslateService({
          lang: 'en',
          fallbackLang: 'en',
        }),
        { provide: AuthModule.AuthService, useValue: authService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginFormComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  const queryElement = (selector: string) => fixture.debugElement.query(By.css(selector));

  it('should render correctly', () => {
    expect(component).toBeTruthy();
    expect(queryElement('form.login')).toBeTruthy();
    expect(queryElement('input[formControlName="email"]')).toBeTruthy();
    expect(queryElement('input[formControlName="password"]')).toBeTruthy();
  });

  it('should not submit when form is invalid', () => {
    component.login();

    expect(authService.login).not.toHaveBeenCalled();
    expect(component.form.touched).toBe(true);
  });

  it('should navigate to root on successful login', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    authService.login.mockReturnValue(of({}));
    component.form.setValue({ email: 'user@example.com', password: 'secret' });

    component.login();

    expect(authService.login).toHaveBeenCalledWith({
      email: 'user@example.com',
      password: 'secret',
    });
    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });

  it('should call login from login button click binding', () => {
    const loginSpy = vi.spyOn(component, 'login');

    fixture.debugElement.queryAll(By.css('p-button'))[0].triggerEventHandler('onClick', {});

    expect(loginSpy).toHaveBeenCalled();
  });

  it('should call toggleRegister from register button click binding', () => {
    const toggleSpy = vi.spyOn(component as any, 'toggleRegister');

    fixture.debugElement.queryAll(By.css('p-button'))[1].triggerEventHandler('onClick', {});

    expect(toggleSpy).toHaveBeenCalled();
  });

  it('should set error on failed login and not navigate', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    authService.login.mockReturnValue(throwError(() => new Error('bad credentials')));
    component.form.setValue({ email: 'user@example.com', password: 'wrong' });

    component.login();
    fixture.detectChanges();

    expect(component.error()).toBe(true);
    expect(queryElement('.error')).toBeTruthy();
    expect(navigateSpy).not.toHaveBeenCalled();
  });

  it('should clear error when form value changes', () => {
    component.error.set(true);

    component.form.controls.email.setValue('next@example.com');

    expect(component.error()).toBe(false);
  });

  it('should render error message when error state is set', () => {
    component.error.set(true);
    fixture.detectChanges();

    expect(component.error()).toBe(true);
    expect(queryElement('.error')).toBeTruthy();
  });

  it('should log toggle register action', () => {
    const logSpy = vi.spyOn(console, 'log').mockImplementation(() => undefined);

    (component as any).toggleRegister();

    expect(logSpy).toHaveBeenCalledWith('Toggle register form');
  });
});



