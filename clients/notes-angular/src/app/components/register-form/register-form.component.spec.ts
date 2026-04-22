import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { RegisterFormComponent } from './register-form.component';
import * as AuthModule from '../../services/auth/auth.service';

describe('RegisterFormComponent', () => {
  let component: RegisterFormComponent;
  let fixture: ComponentFixture<RegisterFormComponent>;
  let router: Router;

  const authService = {
    register: vi.fn(),
  };

  beforeEach(async () => {
    authService.register.mockReset();

    await TestBed.configureTestingModule({
      imports: [RegisterFormComponent],
      providers: [
        provideRouter([]),
        provideTranslateService({
          lang: 'en',
          fallbackLang: 'en',
        }),
        { provide: AuthModule.AuthService, useValue: authService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterFormComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  const queryElement = (selector: string) => fixture.debugElement.query(By.css(selector));

  it('should render correctly', () => {
    expect(component).toBeTruthy();
    expect(queryElement('form.register')).toBeTruthy();
    expect(queryElement('input[formControlName="email"]')).toBeTruthy();
    expect(queryElement('input[formControlName="password"]')).toBeTruthy();
  });

  it('should not submit when form is invalid', () => {
    component.register();

    expect(authService.register).not.toHaveBeenCalled();
    expect(component.form.touched).toBe(true);
  });

  it('should navigate to root on successful register', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    authService.register.mockReturnValue(of({}));
    component.form.setValue({ email: 'user@example.com', password: 'password123' });

    component.register();

    expect(authService.register).toHaveBeenCalledWith({
      email: 'user@example.com',
      password: 'password123',
    });
    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });

  it('should call register from register button click binding', () => {
    const registerSpy = vi.spyOn(component, 'register');

    fixture.debugElement.queryAll(By.css('p-button'))[0].triggerEventHandler('onClick', {});

    expect(registerSpy).toHaveBeenCalled();
  });

  it('should emit toggleToLogin from back to login button click binding', () => {
    const toggleSpy = vi.spyOn(component.toggleToLogin, 'emit');

    fixture.debugElement.queryAll(By.css('p-button'))[1].triggerEventHandler('onClick', {});

    expect(toggleSpy).toHaveBeenCalled();
  });

  it('should set error on failed register and not navigate', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    authService.register.mockReturnValue(throwError(() => new Error('email already exists')));
    component.form.setValue({ email: 'user@example.com', password: 'password123' });

    component.register();
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

  it('should validate email format', () => {
    const emailControl = component.form.controls.email;
    emailControl.setValue('invalid-email');
    expect(emailControl.hasError('email')).toBe(true);

    emailControl.setValue('valid@example.com');
    expect(emailControl.hasError('email')).toBe(false);
  });

  it('should validate password minimum length', () => {
    const passwordControl = component.form.controls.password;
    passwordControl.setValue('short');
    expect(passwordControl.hasError('minlength')).toBe(true);

    passwordControl.setValue('longenough123');
    expect(passwordControl.hasError('minlength')).toBe(false);
  });
});

