import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { By } from '@angular/platform-browser';
import { App } from './app';
import { provideTranslateService } from '@ngx-translate/core';
import { HeaderComponent } from './components/header/header.component';
import { MessageService } from 'primeng/api';

describe('App Component', () => {
  let component: App;
  let fixture: ComponentFixture<App>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App, HeaderComponent],
      providers: [
        provideRouter([]),
        provideHttpClientTesting(),
        MessageService,
        provideTranslateService({
          lang: 'en',
          fallbackLang: 'en',
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(App);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  const queryElement = (selector: string) => fixture.debugElement.query(By.css(selector));

  it('should render correctly', () => {
    expect(component).toBeTruthy();
    expect(queryElement('header')).toBeTruthy();
    expect(queryElement('header app-header')).toBeTruthy();
    expect(queryElement('main')).toBeTruthy();
    expect(queryElement('main router-outlet')).toBeTruthy();
  });
});
