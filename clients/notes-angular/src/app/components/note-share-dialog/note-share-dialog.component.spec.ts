import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { NotePermission } from '@notes/notes_service';
import { Select } from 'primeng/select';
import { NoteShareDialogComponent } from './note-share-dialog.component';
import { NoteService } from '../../services/note/note.service';
import { Note } from '../../types/note';

describe('NoteShareDialogComponent', () => {
  let component: NoteShareDialogComponent;
  let fixture: ComponentFixture<NoteShareDialogComponent>;

  const noteService = {
    getPermissions: vi.fn(),
    setNotePermissions: vi.fn(),
    removeNoteAccess: vi.fn(),
  };

  const createNote = (overrides: Partial<Note> = {}): Note => ({
    id: '1',
    authorId: 'auth-1',
    title: 'Title',
    content: 'Content',
    created: new Date().toISOString() as any,
    updated: new Date().toISOString() as any,
    pinned: false,
    shared: false,
    canEdit: true,
    ...overrides,
  });

  const createShare = (overrides: any = {}) => ({
    userId: 'u-1',
    email: 'u1@example.com',
    permissions: [NotePermission.READ],
    selectedPermission: NotePermission.READ,
    ...overrides,
  });

  beforeEach(async () => {
    noteService.getPermissions.mockReset();
    noteService.setNotePermissions.mockReset();
    noteService.removeNoteAccess.mockReset();
    noteService.getPermissions.mockReturnValue(of([]));
    noteService.setNotePermissions.mockReturnValue(of({}));
    noteService.removeNoteAccess.mockReturnValue(of({}));

    await TestBed.configureTestingModule({
      imports: [NoteShareDialogComponent],
      providers: [
        provideTranslateService({ lang: 'en', fallbackLang: 'en' }),
        { provide: NoteService, useValue: noteService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NoteShareDialogComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('note', createNote({ id: '12' }));
    fixture.componentRef.setInput('visible', true);
    fixture.detectChanges();
  });

  it('loads permissions when opened', () => {
    expect(noteService.getPermissions).toHaveBeenCalledWith('12');
  });

  it('emits sharedStateChanged with isShared=false when permissions are empty', () => {
    const emitSpy = vi.spyOn(component.sharedStateChanged, 'emit');
    noteService.getPermissions.mockReturnValue(of([]));
    (component as any).loadPermissions('12');
    expect(emitSpy).toHaveBeenCalledWith({ noteId: '12', isShared: false });
  });

  it('emits sharedStateChanged with isShared=true when permissions are non-empty', () => {
    const emitSpy = vi.spyOn(component.sharedStateChanged, 'emit');
    noteService.getPermissions.mockReturnValue(of([{ userId: 'u-1', email: 'u1@example.com', permissions: [NotePermission.READ] }]));
    (component as any).loadPermissions('12');
    expect(emitSpy).toHaveBeenCalledWith({ noteId: '12', isShared: true });
  });

  it('handles loadPermissions error gracefully and clears shares', () => {
    noteService.getPermissions.mockReturnValue(throwError(() => new Error('fail')));
    (component as any).loadPermissions('12');
    expect(component.shares()).toEqual([]);
    expect(component.loading()).toBe(false);
  });

  it('shares note with EDIT permission', () => {
    component.form.controls.permission.setValue(NotePermission.EDIT);
    component.form.controls.email.setValue('user@example.com');

    component.onShare();

    expect(noteService.setNotePermissions).toHaveBeenCalledWith('12', 'user@example.com', [NotePermission.EDIT]);
  });

  it('onShare does nothing when form is invalid', () => {
    component.form.controls.email.setValue('not-an-email');
    component.onShare();
    expect(noteService.setNotePermissions).not.toHaveBeenCalled();
  });

  it('onShare does nothing when already saving', () => {
    component.saving.set(true);
    component.form.controls.email.setValue('user@example.com');
    component.onShare();
    expect(noteService.setNotePermissions).not.toHaveBeenCalled();
  });

  it('onShare does nothing when email is blank after trim', () => {
    component.form.controls.email.setValue('   ');
    // mark as valid for this test by bypassing validator via direct patch
    vi.spyOn(component.form, 'invalid', 'get').mockReturnValue(false);
    component.onShare();
    expect(noteService.setNotePermissions).not.toHaveBeenCalled();
  });

  it('onShare resets form and reloads on success', () => {
    noteService.setNotePermissions.mockReturnValue(of({}));
    noteService.getPermissions.mockReturnValue(of([]));
    component.form.controls.email.setValue('user@example.com');

    noteService.getPermissions.mockClear(); // reset count after setup calls
    component.onShare();

    expect(component.form.controls.email.value).toBe('');
    expect(component.userNotFound()).toBe(false);
    expect(noteService.getPermissions).toHaveBeenCalledTimes(1); // reloads once after share
  });

  it('shows user-not-found message on 404 when adding share', () => {
    noteService.setNotePermissions.mockReturnValue(throwError(() => ({ status: 404 })));
    component.form.controls.email.setValue('missing@example.com');

    component.onShare();
    fixture.detectChanges();

    expect(component.userNotFound()).toBe(true);
    expect(fixture.debugElement.query(By.css('p-message'))).toBeTruthy();
  });

  it('shows user-not-found message on 400 when adding share', () => {
    noteService.setNotePermissions.mockReturnValue(throwError(() => ({ status: 400 })));
    component.form.controls.email.setValue('missing@example.com');

    component.onShare();

    expect(component.userNotFound()).toBe(true);
  });

  it('logs error on unexpected share failure', () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    noteService.setNotePermissions.mockReturnValue(throwError(() => ({ status: 500 })));
    component.form.controls.email.setValue('user@example.com');

    component.onShare();

    expect(consoleSpy).toHaveBeenCalledWith('share failed', expect.objectContaining({ status: 500 }));
    consoleSpy.mockRestore();
  });

  it('clears userNotFound when email changes', () => {
    component.userNotFound.set(true);
    component.form.controls.email.setValue('new@example.com');
    expect(component.userNotFound()).toBe(false);
  });

  it('updates selected permission for an existing shared user', () => {
    const share = createShare();
    component.shares.set([share]);

    component.onPermissionChange(share, NotePermission.EDIT);

    expect(noteService.setNotePermissions).toHaveBeenCalledWith('12', 'u1@example.com', [NotePermission.EDIT]);
  });

  it('onPermissionChange does nothing when same permission', () => {
    const share = createShare({ selectedPermission: NotePermission.READ });
    component.shares.set([share]);

    component.onPermissionChange(share, NotePermission.READ);

    expect(noteService.setNotePermissions).not.toHaveBeenCalled();
  });

  it('onPermissionChange does nothing when permission value is invalid', () => {
    const share = createShare();
    component.shares.set([share]);

    component.onPermissionChange(share, null as any);

    expect(noteService.setNotePermissions).not.toHaveBeenCalled();
  });

  it('onPermissionChange rolls back on API error', () => {
    noteService.setNotePermissions.mockReturnValue(throwError(() => new Error('fail')));
    const share = createShare({ selectedPermission: NotePermission.READ });
    component.shares.set([share]);

    component.onPermissionChange(share, NotePermission.EDIT);

    // After error, should roll back to READ
    expect(component.shares()[0].selectedPermission).toBe(NotePermission.READ);
  });

  it('removes user access', () => {
    component.onRemove({ userId: 'u-1' } as any);
    expect(noteService.removeNoteAccess).toHaveBeenCalledWith('12', 'u-1');
  });

  it('onRemove does nothing when no userId', () => {
    component.onRemove({ userId: undefined } as any);
    expect(noteService.removeNoteAccess).not.toHaveBeenCalled();
  });

  it('onRemove does nothing when already removing', () => {
    component.removingUserId.set('u-1');
    component.onRemove({ userId: 'u-2' } as any);
    expect(noteService.removeNoteAccess).not.toHaveBeenCalled();
  });

  it('onRemove logs error on failure', () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    noteService.removeNoteAccess.mockReturnValue(throwError(() => new Error('fail')));
    component.onRemove({ userId: 'u-1' } as any);
    expect(consoleSpy).toHaveBeenCalledWith('remove access failed', expect.any(Error));
    consoleSpy.mockRestore();
  });

  it('emits visibility change on hide', () => {
    const hideSpy = vi.spyOn(component.visibleChange, 'emit');
    component.onHide();
    expect(hideSpy).toHaveBeenCalledWith(false);
  });

  it('resets state on hide', () => {
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


