import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ForbiddenPage } from './forbidden-page';
import { provideTranslateService } from '@ngx-translate/core';

describe('ForbiddenPage', () => {
  let component: ForbiddenPage;
  let fixture: ComponentFixture<ForbiddenPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ForbiddenPage],
      providers: [provideTranslateService()]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ForbiddenPage);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
