import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { AuthDialogComponent } from './auth-dialog.component';
import { AuthService } from '../../services/auth/auth.service';
import { LoginFormComponent } from '../login-form/login-form.component';
import { RegisterFormComponent } from '../register-form/register-form.component';

describe('AuthDialogComponent', () => {
  let component: AuthDialogComponent;
  let fixture: ComponentFixture<AuthDialogComponent>;

  const authService = {
    login: vi.fn(),
    logged$: { subscribe: vi.fn() },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthDialogComponent],
      providers: [
        provideRouter([]),
        provideTranslateService({
          lang: 'en',
          fallbackLang: 'en',
        }),
        { provide: AuthService, useValue: authService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AuthDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  const queryElement = (selector: string) => fixture.debugElement.query(By.css(selector));

  it('should render correctly', () => {
    expect(component).toBeTruthy();
    expect(queryElement('p-dialog')).toBeTruthy();
  });

  it('should update visible from p-dialog visibleChange binding', () => {
    expect(component.visible).toBe(false);

    queryElement('p-dialog').triggerEventHandler('visibleChange', true);

    expect(component.visible).toBe(true);
  });

  it('should call onHide from p-dialog onHide binding', () => {
    const onHideSpy = vi.spyOn(component, 'onHide');

    queryElement('p-dialog').triggerEventHandler('onHide', {});

    expect(onHideSpy).toHaveBeenCalled();
  });

  it('should hide dialog and emit visible change on hide', () => {
    component.visible = true;
    const visibleChangeSpy = vi.spyOn(component.visibleChange, 'emit');

    component.onHide();

    expect(component.visible).toBe(false);
    expect(visibleChangeSpy).toHaveBeenCalledWith(false);
  });

  it('should start with login form displayed', () => {
    expect(component.isLoginForm()).toBe(true);
  });

  it('should toggle multiple times correctly', () => {
    expect(component.isLoginForm()).toBe(true);

    component.toggleForm();
    expect(component.isLoginForm()).toBe(false);

    component.toggleForm();
    expect(component.isLoginForm()).toBe(true);
  });

  it('should have opposite state after each toggle', () => {
    const initialState = component.isLoginForm();

    component.toggleForm();
    expect(component.isLoginForm()).not.toBe(initialState);

    component.toggleForm();
    expect(component.isLoginForm()).toBe(initialState);

    component.toggleForm();
    expect(component.isLoginForm()).not.toBe(initialState);
  });

  it('renders login form branch by default', () => {
    fixture.componentRef.setInput('visible', true);
    fixture.detectChanges();

    expect(fixture.debugElement.query(By.directive(LoginFormComponent))).toBeTruthy();
    expect(fixture.debugElement.query(By.directive(RegisterFormComponent))).toBeFalsy();
  });

  it('renders register form branch after toggle', () => {
    fixture.componentRef.setInput('visible', true);
    component.toggleForm();
    fixture.detectChanges();

    expect(fixture.debugElement.query(By.directive(LoginFormComponent))).toBeFalsy();
    expect(fixture.debugElement.query(By.directive(RegisterFormComponent))).toBeTruthy();
  });

  it('toggles form from child output events', () => {
    fixture.componentRef.setInput('visible', true);
    fixture.detectChanges();
    expect(component.isLoginForm()).toBe(true);

    const loginForm = fixture.debugElement.query(By.directive(LoginFormComponent));
    (loginForm.componentInstance as { toggleRegister: { emit: () => void } }).toggleRegister.emit();
    fixture.detectChanges();
    expect(component.isLoginForm()).toBe(false);

    const registerForm = fixture.debugElement.query(By.directive(RegisterFormComponent));
    (registerForm.componentInstance as { toggleToLogin: { emit: () => void } }).toggleToLogin.emit();
    fixture.detectChanges();
    expect(component.isLoginForm()).toBe(true);
  });
});
