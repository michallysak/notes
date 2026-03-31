import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { AuthDialogComponent } from './auth-dialog.component';
import { AuthService } from '../../services/auth/auth.service';

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
});




