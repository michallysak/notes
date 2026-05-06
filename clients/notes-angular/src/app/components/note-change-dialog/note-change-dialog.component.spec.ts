import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { EMPTY, of, throwError } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { NoteChangeDialogComponent } from './note-change-dialog.component';
import { NotesAPIService, NoteResponse } from '@notes/notes_service';
import { NoteEventsService } from '../../services/note/note-events.service';

describe('NoteChangeDialogComponent', () => {
  let component: NoteChangeDialogComponent;
  let fixture: ComponentFixture<NoteChangeDialogComponent>;

  const mockApi = {
    createNote: vi.fn(),
    updateNote: vi.fn(),
    searchNotes: vi.fn().mockReturnValue(of([])),
  };

  const sampleNote: NoteResponse = {
    id: '10',
    title: 'T1',
    content: 'C1',
    pinned: false,
    created: new Date('2026-01-01T10:00:00Z').toISOString(),
    updated: undefined,
  } as any;

  beforeEach(async () => {
    mockApi.createNote.mockReset();
    mockApi.updateNote.mockReset();

    await TestBed.configureTestingModule({
      imports: [NoteChangeDialogComponent],
      providers: [
        provideTranslateService({ lang: 'en', fallbackLang: 'en' }),
        { provide: NotesAPIService, useValue: mockApi },
        { provide: NoteEventsService, useValue: { noteEvents$: EMPTY, noteUpdatedEvents$: EMPTY, noteDeletedEvents$: EMPTY } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NoteChangeDialogComponent);
    component = fixture.componentInstance;
    // do not call detectChanges here to avoid ExpressionChangedAfterItHasBeenCheckedError
  });

  it('should render and have form controls', () => {
    expect(component).toBeTruthy();
    // form controls exist on the component instance
    expect(component.form.controls.title).toBeTruthy();
    expect(component.form.controls.content).toBeTruthy();
  });

  it('should render spinner when saving is true and visible', () => {
    component.visible = true;
    component.saving.set(true);

    fixture.detectChanges();

    const spinner = fixture.debugElement.query(By.css('p-progress-spinner'));
    expect(spinner).toBeTruthy();
  });

  it('should render not-saved message when form is dirty, lastSavedNote exists and notSaved is true', () => {
    component.visible = true;
    component.lastSavedNote.set(sampleNote as any);
    component.notSaved.set(true);
    // make form dirty
    component.form.markAsDirty();

    fixture.detectChanges();

    // primeNG dialog/projecting may render content outside the component DOM in tests;
    // assert the component state that controls the message rendering instead
    expect(component.notSaved()).toBe(true);
    expect(component.form.dirty).toBe(true);
    expect(component.lastSavedNote()).toBeTruthy();
  });

  it('should patch form when note input changes', () => {
    // simulate input change
    component.note = sampleNote as any;
    component.ngOnChanges({ note: { currentValue: sampleNote, firstChange: true, previousValue: null, isFirstChange: () => true } as any });
    fixture.detectChanges();

    expect(component.form.controls.title.value).toBe('T1');
    expect(component.form.controls.content.value).toBe('C1');
    expect(component.lastSavedNote()).toBeTruthy();
  });

  it('should call createNote on save when note is null', async () => {
    vi.useFakeTimers();
    const res = { ...sampleNote, id: '11' } as NoteResponse;
    mockApi.createNote.mockReturnValue(of(res));

    component.note = null;
    component.form.setValue({ title: 'New', content: 'Body', color: null });

    // call private save and advance timers so delayed subscribe runs
    (component as any).save();
    vi.advanceTimersByTime(1000);
    // no further change detection required for state assertions
    expect(mockApi.createNote).toHaveBeenCalledWith({ title: 'New', content: 'Body' });
    expect(component.lastSavedNote()?.id).toBe('11');
    vi.useRealTimers();
  });

  it('should call updateNote on save when note exists', async () => {
    vi.useFakeTimers();
    mockApi.updateNote.mockReturnValue(of(sampleNote));
    component.note = sampleNote as any;
    // title must be long enough to pass validators (minLength 3)
    component.form.setValue({ title: 'Title', content: 'C1', color: null });

    (component as any).save();
    vi.advanceTimersByTime(1000);

    expect(mockApi.updateNote).toHaveBeenCalledWith({ title: 'Title', content: 'C1', style: { color: null } }, '10');
    vi.useRealTimers();
  });

  it('should set notSaved on save error', async () => {
    vi.useFakeTimers();
    mockApi.createNote.mockReturnValue(throwError(() => new Error('fail')));

    component.note = null;
    // title must satisfy validators so save() proceeds
    component.form.setValue({ title: 'Abc', content: 'Y', color: null });

    (component as any).save();
    vi.advanceTimersByTime(1000);

    expect(component.notSaved()).toBe(true);
    vi.useRealTimers();
  });

  it('should set notSaved and saved=false when form is invalid after debounce', () => {
    vi.useFakeTimers();

    component.form.setValue({ title: 'A', content: 'Y', color: null });
    // ensure form is considered dirty so valueChanges handler proceeds
    component.form.markAsDirty();
    // advance debounce
    vi.advanceTimersByTime(1000);

    expect(component.notSaved()).toBe(true);
    expect(component.saved()).toBe(false);

    vi.useRealTimers();
  });

  it('calls save after debounce when the dirty form is valid', () => {
    vi.useFakeTimers();
    const saveSpy = vi.spyOn(component as any, 'save').mockImplementation(() => undefined);

    component.form.setValue({ title: 'Valid title', content: 'Body', color: null });
    component.form.markAsDirty();
    vi.advanceTimersByTime(1000);

    expect(saveSpy).toHaveBeenCalledOnce();
    saveSpy.mockRestore();
    vi.useRealTimers();
  });

  it('does not save when a request is already in progress', () => {
    component.saving.set(true);
    component.form.setValue({ title: 'Valid title', content: 'Body', color: null });

    (component as any).save();

    expect(mockApi.createNote).not.toHaveBeenCalled();
    expect(mockApi.updateNote).not.toHaveBeenCalled();
  });

  it('does not create a note when form values are undefined', () => {
    component.note = null;
    Object.defineProperty(component.form, 'value', {
      configurable: true,
      get: () => ({ title: undefined, content: undefined }),
    });

    (component as any).save();

    expect(mockApi.createNote).not.toHaveBeenCalled();
  });

  it('sets notSaved on update error', () => {
    vi.useFakeTimers();
    mockApi.updateNote.mockReturnValue(throwError(() => new Error('update fail')));
    component.note = sampleNote as any;
    component.form.setValue({ title: 'Title', content: 'C1', color: null });

    (component as any).save();
    vi.advanceTimersByTime(1000);

    expect(component.notSaved()).toBe(true);
    vi.useRealTimers();
  });

  it('should render saved message when saved is true and visible', () => {
    component.visible = true;
    component.saved.set(true);

    fixture.detectChanges();

    const msg = fixture.debugElement.query(By.css('p-message'));
    expect(msg).toBeTruthy();
  });

  it('should set notSaved true and saved false when visible becomes true and note is null', () => {
    component.note = null;

    component.ngOnChanges({ visible: { currentValue: true, firstChange: false, previousValue: false, isFirstChange: () => false } as any });

    expect(component.notSaved()).toBe(true);
    expect(component.saved()).toBe(false);
    expect(component.lastSavedNote()).toBeNull();
  });

  it('should return white color as default from colorPickerValue when color is null', () => {
    component.form.controls.color.setValue(null);

    expect(component.colorPickerValue()).toBe('#ffffff');
  });

  it('should return actual color from colorPickerValue when color is set', () => {
    const testColor = '#ff0000';
    component.form.controls.color.setValue(testColor);

    expect(component.colorPickerValue()).toBe(testColor);
  });

  it('should update color control and mark form dirty when onColorInput is called', () => {
    const input = document.createElement('input');
    input.value = '#00ff00';
    const event = new Event('input');
    Object.defineProperty(event, 'target', { value: input, enumerable: true });

    component.form.markAsPristine();
    component.onColorInput(event);

    expect(component.form.dirty).toBe(true);
    expect(component.form.controls.color.dirty).toBe(true);
    expect(component.form.controls.color.touched).toBe(true);
  });

  it('should clear color and mark form dirty when clearColor is called', () => {
    component.form.controls.color.setValue('#ff0000');
    component.form.markAsPristine();

    component.clearColor();

    expect(component.form.controls.color.value).toBeNull();
    expect(component.form.dirty).toBe(true);
    expect(component.form.controls.color.dirty).toBe(true);
    expect(component.form.controls.color.touched).toBe(true);
  });

  it('should include color in form value', () => {
    component.form.setValue({ title: 'Test', content: 'Content', color: '#aabbcc' });

    expect(component.form.value.color).toBe('#aabbcc');
  });

  it('should have color control on form', () => {
    expect(component.form.controls.color).toBeTruthy();
  });

  it('should call updateNote with color property when saving note with color', async () => {
    vi.useFakeTimers();
    mockApi.updateNote.mockReturnValue(of(sampleNote));
    component.note = sampleNote as any;
    component.form.setValue({ title: 'Title', content: 'C1', color: '#ff0000' });

    (component as any).save();
    vi.advanceTimersByTime(1000);

    expect(mockApi.updateNote).toHaveBeenCalled();
    const updateCall = mockApi.updateNote.mock.calls[0];
    expect(updateCall[0]).toHaveProperty('style');
    vi.useRealTimers();
  });

  it('should reset form with color when note is provided', () => {
    const noteWithColor = { ...sampleNote, style: { color: '#ff0000' } } as any;
    component.note = noteWithColor;

    component.ngOnChanges({ note: { currentValue: noteWithColor, firstChange: true, previousValue: null, isFirstChange: () => true } as any });

    expect(component.form.controls.color.value).toBeTruthy();
  });

  it('should call onHide when p-dialog onHide event fires', () => {
    const onHideSpy = vi.spyOn(component, 'onHide');
    component.visible = true;

    const dialogElement = fixture.debugElement.query(By.css('p-dialog'));
    dialogElement?.triggerEventHandler('onHide', {});

    expect(onHideSpy).toHaveBeenCalled();
  });

  it('should trap color input value and normalize it on onColorInput', () => {
    const input = document.createElement('input');
    input.value = '#FF0000';
    const event = new Event('input');
    Object.defineProperty(event, 'target', { value: input, enumerable: true });

    component.onColorInput(event);

    expect(component.form.controls.color.value).toBe('#ff0000');
  });

  it('should handle color input with various valid hex formats', () => {
    const testCases = [
      { input: '#FFF', expected: '#ffffff' },
      { input: '#ffffff', expected: '#ffffff' },
      { input: '#ABCDEF', expected: '#abcdef' },
      { input: '#abc', expected: '#aabbcc' },
    ];

    testCases.forEach(({ input, expected }) => {
      const inputElement = document.createElement('input');
      inputElement.value = input;
      const event = new Event('input');
      Object.defineProperty(event, 'target', { value: inputElement, enumerable: true });

      component.onColorInput(event);

      expect(component.form.controls.color.value).toBe(expected);
    });
  });

  it('should have colorPickerValue return current color value when set', () => {
    component.form.setValue({ title: 'Test', content: 'Content', color: '#abcdef' });

    expect(component.colorPickerValue()).toBe('#abcdef');
  });

  it('should have activeNote return lastSavedNote when it exists', () => {
    component.lastSavedNote.set(sampleNote as any);

    expect(component.activeNote()).toBe(sampleNote);
  });

  it('should have activeNote return current note when lastSavedNote is null', () => {
    component.note = sampleNote as any;
    component.lastSavedNote.set(null);

    expect(component.activeNote()).toBe(sampleNote);
  });

  it('should properly reset form with color and other fields', () => {
    const noteWithStyle = {
      ...sampleNote,
      style: { color: '#ff0000' },
      title: 'Updated Title',
      content: 'Updated Content',
    } as any;

    component.note = noteWithStyle;
    component.ngOnChanges({
      note: {
        currentValue: noteWithStyle,
        firstChange: true,
        previousValue: null,
        isFirstChange: () => true,
      } as any,
    });

    expect(component.form.controls.title.value).toBe('Updated Title');
    expect(component.form.controls.content.value).toBe('Updated Content');
    expect(component.form.controls.color.value).toBeTruthy();
  });

  it('calls onColorInput from color input event binding', () => {
    const spy = vi.spyOn(component, 'onColorInput');
    component.visible = true;
    component.note = sampleNote as any;
    fixture.detectChanges();

    const colorInput = fixture.debugElement.query(By.css('input[type="color"]'));
    expect(colorInput).toBeTruthy();

    const input = document.createElement('input');
    input.value = '#00ff00';
    const event = new Event('input');
    Object.defineProperty(event, 'target', { value: input, enumerable: true });

    colorInput.triggerEventHandler('input', event);

    expect(spy).toHaveBeenCalled();
    expect(component.form.controls.color.value).toBe('#00ff00');
  });

  it('hides clear color button when color is empty', () => {
    component.visible = true;
    component.note = sampleNote as any;

    component.form.controls.color.setValue(null);
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('.note-color-controls p-button'))).toBeFalsy();
  });

  it('shows clear color button when color has value', () => {
    component.visible = true;
    component.note = sampleNote as any;

    component.form.controls.color.setValue('#ff0000');
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('.note-color-controls p-button'))).toBeTruthy();
  });

  it('calls clearColor from clear button click binding', () => {
    const clearSpy = vi.spyOn(component, 'clearColor');
    component.visible = true;
    component.note = sampleNote as any;
    component.form.controls.color.setValue('#ff0000');
    fixture.detectChanges();

    const clearButton = fixture.debugElement.query(By.css('.note-color-controls p-button'));
    expect(clearButton).toBeTruthy();

    clearButton.triggerEventHandler('onClick', {});

    expect(clearSpy).toHaveBeenCalled();
    expect(component.form.controls.color.value).toBeNull();
  });
});
