import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { TextRangeComponent } from './text-range.component';

describe('TextRangeComponent', () => {
  let component: TextRangeComponent;
  let fixture: ComponentFixture<TextRangeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TextRangeComponent] }).compileComponents();
    fixture = TestBed.createComponent(TextRangeComponent);
    component = fixture.componentInstance;
  });

  it('should calculate percent and render text', () => {
    component.min = 3;
    component.max = 100;
    component.current = 50;
    fixture.detectChanges();

    expect(component.percent).toBeCloseTo(50);
    const span = fixture.debugElement.query(By.css('span'));
    expect(span.nativeElement.textContent.trim()).toBe('50 / 100');
  });

  it('should apply red class when current < min', () => {
    component.min = 10;
    component.max = 100;
    component.current = 5;
    fixture.detectChanges();

    expect(component.isRed).toBe(true);
    const span = fixture.debugElement.query(By.css('span'));
    expect(span.classes['red']).toBe(true);
  });

  it('should apply yellow class when percent >= 75 and not red', () => {
    component.min = 0;
    component.max = 100;
    component.current = 80;
    fixture.detectChanges();

    expect(component.isYellow).toBe(true);
    const span = fixture.debugElement.query(By.css('span'));
    expect(span.classes['yellow']).toBe(true);
  });

  it('should apply green class otherwise', () => {
    component.min = 0;
    component.max = 100;
    component.current = 50;
    fixture.detectChanges();

    expect(component.isGreen).toBe(true);
    const span = fixture.debugElement.query(By.css('span'));
    expect(span.classes['green']).toBe(true);
  });

  it('should return 0 percent when max is zero', () => {
    component.min = 0;
    component.max = 0;
    component.current = 10;
    fixture.detectChanges();

    expect(component.percent).toBe(0);
  });
});

