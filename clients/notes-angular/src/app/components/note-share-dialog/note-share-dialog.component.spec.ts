import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { NotePermission, NoteShareResponse } from '@notes/notes_service';
import { Select } from 'primeng/select';
import { MessageService } from 'primeng/api';
import { NoteShareDialogComponent } from './note-share-dialog.component';
import { NoteService } from '../../services/note/note.service';
import { NoteEventsService } from '../../services/note/note-events.service';
import { Note } from '../../types/note';

describe('NoteShareDialogComponent', () => {
  let component: NoteShareDialogComponent;
  let fixture: ComponentFixture<NoteShareDialogComponent>;
  const noteService = {
    notes$: of([]),
    setNotePermissions: vi.fn(),
    removeNoteAccess: vi.fn(),
    makeNotePublic: vi.fn(),
    undoNotePublic: vi.fn(),
  };

  const createNote = (overrides: Partial<Note> = {}): Note => ({
    id: '12',
    authorId: 'auth-1',
    title: 'test note',
    content: 'content',
    created: null as any,
    updated: null as any,
    pinned: false,
    style: undefined,
    shares: [],
    shared: false,
    canEdit: true,
    ...overrides,
  });

  const createShare = (overrides: Partial<NoteShareResponse & { selectedPermission: NotePermission }> = {}) => ({
    userId: 'u-1',
    email: 'u1@example.com',
    permissions: [NotePermission.READ],
    selectedPermission: NotePermission.READ,
    ...overrides,
  });

  beforeEach(async () => {
    noteService.setNotePermissions.mockReset();
    noteService.setNotePermissions.mockReturnValue(of({}));
    noteService.removeNoteAccess.mockReset();
    noteService.removeNoteAccess.mockReturnValue(of({}));
    noteService.makeNotePublic.mockReset();
    noteService.makeNotePublic.mockReturnValue(of({}));
    noteService.undoNotePublic.mockReset();
    noteService.undoNotePublic.mockReturnValue(of({}));

    await TestBed.configureTestingModule({
      imports: [NoteShareDialogComponent],
      providers: [
        provideTranslateService({ lang: 'en', fallbackLang: 'en' }),
        { provide: NoteService, useValue: noteService },
        MessageService,
        { provide: NoteEventsService, useValue: { domainEvents$: of(null) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NoteShareDialogComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('note', createNote({ id: 'note-1', shares: [] }));
    fixture.componentRef.setInput('visible', true);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('emits sharedStateChanged with isShared=false when shares are empty', () => {
    const emitSpy = vi.spyOn(component.sharedStateChanged, 'emit');
    component.note = createNote({ id: 'note-1', shares: [] });
    component.visible = true;
    component.ngOnChanges({
      visible: { currentValue: true } as any,
    });
    expect(emitSpy).toHaveBeenCalledWith({ noteId: 'note-1', isShared: false });
  });

  it('emits sharedStateChanged with isShared=true when shares are non-empty', () => {
    const emitSpy = vi.spyOn(component.sharedStateChanged, 'emit');
    component.note = createNote({ id: 'note-1', shares: [{ userId: 'u-1', email: 'u1@example.com', permissions: [NotePermission.READ] }] });
    component.visible = true;
    component.ngOnChanges({
      visible: { currentValue: true } as any,
    });
    expect(emitSpy).toHaveBeenCalledWith({ noteId: 'note-1', isShared: true });
  });

  it('resets state onHide', () => {
    component.userNotFound.set(true);
    component.saving.set(true);
    component.onHide();
    expect(component.userNotFound()).toBe(false);
    expect(component.saving()).toBe(false);
    expect(component.shares()).toEqual([]);
  });

  it('renders input and add button', () => {
    expect(fixture.debugElement.query(By.css('input[type="email"]'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('p-select[formControlName="permission"]'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('p-button[type="submit"]'))).toBeTruthy();
  });

  it('makes note public with the selected permission', () => {
    component.onMakePublic(NotePermission.EDIT);

    expect(noteService.makeNotePublic).toHaveBeenCalledWith('note-1', NotePermission.EDIT);
    expect(component.publicEnabled()).toBe(true);
  });

  it('undoes note public access', () => {
    component.publicEnabled.set(true);
    component.onUndoPublic();

    expect(noteService.undoNotePublic).toHaveBeenCalledWith('note-1');
    expect(component.publicEnabled()).toBe(false);
  });

  it('renders loading spinner when loading', () => {
    component.loading.set(true);
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('p-progress-spinner'))).toBeTruthy();
  });

  it('renders empty message when no shares', () => {
    component.loading.set(false);
    component.shares.set([]);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('');
    expect(fixture.debugElement.query(By.css('.share-empty'))).toBeTruthy();
  });

  it('renders share list rows when shares exist', () => {
    component.loading.set(false);
    component.shares.set([
      createShare({ userId: 'u-1', email: 'u1@example.com', permissions: [NotePermission.READ], selectedPermission: NotePermission.READ }),
      createShare({ userId: 'u-2', email: 'u2@example.com', permissions: [NotePermission.EDIT], selectedPermission: NotePermission.EDIT }),
    ]);
    fixture.detectChanges();

    const rows = fixture.debugElement.queryAll(By.css('.share-row'));
    expect(rows.length).toBe(2);
    expect(fixture.nativeElement.textContent).toContain('u1@example.com');
    expect(fixture.nativeElement.textContent).toContain('u2@example.com');
  });

  it('permissionsLabel returns joined permissions', () => {
    const share = createShare({ permissions: [NotePermission.READ, NotePermission.EDIT] });
    expect(component.permissionsLabel(share)).toBe('READ, EDIT');
  });

  it('permissionsLabel returns empty string for no permissions', () => {
    expect(component.permissionsLabel({ permissions: undefined } as any)).toBe('');
  });

  it('updates visible from p-dialog visibleChange binding', () => {
    fixture.componentRef.setInput('visible', false);
    fixture.detectChanges();

    const dialog = fixture.debugElement.query(By.css('p-dialog'));
    dialog.triggerEventHandler('visibleChange', true);

    expect(component.visible).toBe(true);
  });

  it('calls onShare from form ngSubmit binding', () => {
    const onShareSpy = vi.spyOn(component, 'onShare');

    const form = fixture.debugElement.query(By.css('form.share-dialog-form'));
    form.triggerEventHandler('ngSubmit', {});

    expect(onShareSpy).toHaveBeenCalledTimes(1);
  });

  it('renders permission translation labels in selects', () => {
    component.loading.set(false);
    component.form.controls.permission.setValue(NotePermission.READ);
    component.shares.set([
      createShare({
        userId: 'u-1',
        email: 'u1@example.com',
        permissions: [NotePermission.EDIT],
        selectedPermission: NotePermission.EDIT,
      }),
    ]);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toMatch(/(NOTES\.SHARE_DIALOG\.PERMISSION\.|Read|Edit)/i);
  });

  it('renders permission templates for both selectedItem and item slots', () => {
    component.loading.set(false);
    component.shares.set([
      createShare({
        userId: 'u-1',
        email: 'u1@example.com',
        permissions: [NotePermission.EDIT],
        selectedPermission: NotePermission.EDIT,
      }),
    ]);
    fixture.detectChanges();

    const selectEls = fixture.debugElement.queryAll(By.directive(Select));
    expect(selectEls.length).toBeGreaterThanOrEqual(2);

    selectEls.forEach((el) => {
      const selectInstance = el.componentInstance as {
        selectedItemTemplate?: { createEmbeddedView: (ctx: any) => any };
        itemTemplate?: { createEmbeddedView: (ctx: any) => any };
      };

      const selectedView = selectInstance.selectedItemTemplate?.createEmbeddedView({ $implicit: NotePermission.READ });
      selectedView?.detectChanges();

      const itemView = selectInstance.itemTemplate?.createEmbeddedView({ $implicit: NotePermission.EDIT });
      itemView?.detectChanges();

      const selectedText = (selectedView?.rootNodes ?? []).map((n: Node) => n.textContent ?? '').join('');
      const itemText = (itemView?.rootNodes ?? []).map((n: Node) => n.textContent ?? '').join('');

      expect(selectedText).toMatch(/(NOTES\.SHARE_DIALOG\.PERMISSION\.|Read|Edit)/i);
      expect(itemText).toMatch(/(NOTES\.SHARE_DIALOG\.PERMISSION\.|Read|Edit)/i);

      selectedView?.destroy();
      itemView?.destroy();
    });
  });

  it('renders userId when share email is missing', () => {
    component.loading.set(false);
    component.shares.set([
      createShare({ userId: 'u-fallback', email: '', permissions: [NotePermission.READ], selectedPermission: NotePermission.READ }),
    ]);
    fixture.detectChanges();

    const emailText = fixture.debugElement.query(By.css('.share-email'))?.nativeElement.textContent as string;
    expect(emailText).toContain('u-fallback');
  });

  it('calls onPermissionChange from row p-select onChange binding', () => {
    const share = createShare({
      userId: 'u-1',
      email: 'u1@example.com',
      permissions: [NotePermission.READ],
      selectedPermission: NotePermission.READ,
    });
    const onPermissionChangeSpy = vi.spyOn(component, 'onPermissionChange');

    component.loading.set(false);
    component.shares.set([share]);
    fixture.detectChanges();

    const rowSelect = fixture.debugElement.query(By.css('.share-row p-select'));
    rowSelect.triggerEventHandler('onChange', { value: NotePermission.EDIT });

    expect(onPermissionChangeSpy).toHaveBeenCalledWith(expect.objectContaining({ userId: 'u-1' }), NotePermission.EDIT);
  });

  it('calls onRemove from row remove button onClick binding', () => {
    const share = createShare({
      userId: 'u-remove',
      email: 'remove@example.com',
      permissions: [NotePermission.READ],
      selectedPermission: NotePermission.READ,
    });
    const onRemoveSpy = vi.spyOn(component, 'onRemove');

    component.loading.set(false);
    component.shares.set([share]);
    fixture.detectChanges();

    const removeButton = fixture.debugElement.query(By.css('.share-row p-button[icon="pi pi-times"]'));
    removeButton.triggerEventHandler('onClick', {});

    expect(onRemoveSpy).toHaveBeenCalledWith(expect.objectContaining({ userId: 'u-remove' }));
  });
});
