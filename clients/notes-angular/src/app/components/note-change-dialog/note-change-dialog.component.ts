import { Component, EventEmitter, Input, Output, SimpleChanges, signal, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';
import { ProgressBarModule } from 'primeng/progressbar';
import { debounceTime, delay, Subject, takeUntil } from 'rxjs';
import { CreateNoteRequest, NoteResponse, NoteUpdateRequest } from '@notes/notes_service';
import { NoteService } from '../../services/note/note.service';
import { NoteChangeDateTimeComponent } from '../note-change-datetime/note-change-date-time.component';
import { TextRangeComponent } from '../text-range/text-range.component';
import { FloatLabelModule } from 'primeng/floatlabel';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslatePipe } from '@ngx-translate/core';
import { mixHexWithBase, normalizeHexColor } from '../../utils/color-contrast.util';
import { Message } from 'primeng/message';

type NoteForm = {
  title: FormControl<string>;
  content: FormControl<string>;
  color: FormControl<string | null>;
};

@Component({
  selector: 'app-note-change-dialog',
  standalone: true,
  imports: [
    CommonModule,
    DialogModule,
    ReactiveFormsModule,
    InputTextModule,
    TextareaModule,
    ButtonModule,
    ProgressBarModule,
    NoteChangeDateTimeComponent,
    TextRangeComponent,
    ProgressSpinnerModule,
    TranslatePipe,
    FloatLabelModule,
    Message,
  ],
  templateUrl: './note-change-dialog.component.html',
  styleUrls: ['./note-change-dialog.component.scss'],
})
export class NoteChangeDialogComponent implements OnDestroy {
  @Input({ required: true }) visible!: boolean;
  @Input({ required: true }) note!: NoteResponse | null;
  @Input({ required: true }) readonly!: boolean;
  @Output() visibleChange = new EventEmitter<boolean>();

  form: FormGroup<NoteForm>;

  saving = signal(false);
  lastSavedNote = signal<NoteResponse | null>(null);
  notSaved = signal(false);
  saved = signal(false);

  private saveDebounce = 1000;
  private destroy$ = new Subject<void>();

  constructor(private noteService: NoteService) {
    this.form = new FormGroup<NoteForm>({
      title: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(3), Validators.maxLength(64)],
      }),
      content: new FormControl('', {
        nonNullable: true,
        validators: [Validators.maxLength(2048)],
      }),
      color: new FormControl<string | null>(null),
    });

    this.form.valueChanges.pipe(debounceTime(this.saveDebounce), takeUntil(this.destroy$)).subscribe(() => {
      if (this.readonly) {
        return;
      }
      if (!this.form.dirty) {
        return;
      }
      // User modified the form after any previous save -> hide saved indicator
      this.saved.set(false);

      if (this.form.valid) {
        this.save();
      } else {
        this.notSaved.set(true);
        this.saved.set(false);
      }
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['note']) {
      this.syncDialogState(this.note);
    }

    if (changes['visible']) {
      const vis = changes['visible'].currentValue as boolean;
      if (vis) {
        this.syncDialogState(this.note);
      }
    }

    if (changes['readonly']) {
      this.syncDialogState(this.note);
      if (this.readonly) {
        this.form.disable();
      } else {
        this.form.enable();
      }
    }
  }

  onHide() {
    this.visible = false;
    this.visibleChange.emit(this.visible);
    this.resetForm();
    this.lastSavedNote.set(null);
    this.notSaved.set(false);
    this.saved.set(false);
  }

  colorPickerValue() {
    return this.form.controls.color.value ?? '#ffffff';
  }

  onColorInput(event: Event) {
    const target = event.target as HTMLInputElement | null;
    const color = this.normalizeColor(target?.value);

    this.form.controls.color.setValue(color);
    this.form.controls.color.markAsDirty();
    this.form.controls.color.markAsTouched();
    this.form.markAsDirty();
  }

  clearColor() {
    this.form.controls.color.setValue(null);
    this.form.controls.color.markAsDirty();
    this.form.controls.color.markAsTouched();
    this.form.markAsDirty();
  }

  get dialogPt() {
    const backgroundColor = this.dialogSurfaceColor();
    const textColor = this.dialogTextColor();

    return {
      root: {
        style: {
          backgroundColor,
          color: textColor,
          borderRadius: '0.75rem',
          overflow: 'hidden',
        },
      },
      header: {
        style: {
          backgroundColor,
          color: textColor,
        },
      },
      content: {
        style: {
          backgroundColor,
          color: textColor,
        },
      },
      footer: {
        style: {
          backgroundColor,
          color: textColor,
        },
      },
    };
  }

  dialogTextColor() {
    return 'var(--p-text-color)';
  }


  private dialogSurfaceColor() {
    return mixHexWithBase(this.form.controls.color.value, 'var(--p-content-background)', 28);
  }

  inputBackgroundColor() {
    return mixHexWithBase(
      this.form.controls.color.value,
      'var(--p-inputtext-background, var(--p-content-background))',
      36,
    );
  }

  inputBorderColor() {
    return mixHexWithBase(
      this.form.controls.color.value,
      'var(--p-inputtext-border-color, var(--p-content-border-color))',
      68,
    );
  }

  inputTextColor() {
    return 'var(--p-text-color)';
  }

  activeNote() {
    return this.lastSavedNote() ?? this.note;
  }

  private save() {
    this.notSaved.set(false);
    this.saved.set(false);
    if (this.form.invalid || this.saving() || this.readonly) {
      return;
    }

    this.saving.set(true);

    const currentNote = this.lastSavedNote() ?? this.note;
    if (currentNote && currentNote.id) {
      const body = this.buildUpdateRequest();
      this.noteService
        .updateNote(currentNote.id, body)
        .pipe(delay(1000)) // simulate network delay
        .subscribe({
          next: (res) => this.onSaveSuccess(res),
          error: () => this.onSaveError(),
        });
    } else {
      const value = this.form.value;
      if (value.title === undefined || value.content === undefined) {
        return;
      }
      const body: CreateNoteRequest = {
        title: value.title,
        content: value.content,
      };
      this.noteService
        .createNote(body)
        .pipe(delay(1000)) // simulate network delay
        .subscribe({
          next: (res) => this.onSaveSuccess(res),
          error: () => this.onSaveError(),
        });
    }
  }

  private onSaveSuccess(res: NoteResponse) {
    this.saving.set(false);
    this.lastSavedNote.set(res);
    this.resetForm(res);
    this.notSaved.set(false);
    // Only mark saved on successful server response
    this.saved.set(true);
  }

  private onSaveError() {
    this.saving.set(false);
    this.notSaved.set(true);
    this.saved.set(false);
  }

  private syncDialogState(note: NoteResponse | null) {
    this.resetForm(note);
    this.lastSavedNote.set(note ? { ...note } : null);
    this.notSaved.set(!note);
    this.saved.set(false);
  }

  private resetForm(note: NoteResponse | null = null) {
    this.form.reset(
      {
        title: note?.title || '',
        content: note?.content || '',
        color: this.normalizeColor(note?.style?.color),
      },
      { emitEvent: false },
    );
    this.form.markAsPristine();
    this.form.markAsUntouched();
  }

  private buildUpdateRequest(): NoteUpdateRequest {
    const { title, content, color } = this.form.getRawValue();
    const normalizedColor = this.normalizeColor(color);
    const styleWithNullableColor = {
      color: normalizedColor ?? null,
    } as unknown as NoteUpdateRequest['style'];

    return {
      title,
      content,
      style: styleWithNullableColor,
    };
  }

  private normalizeColor(color: string | null | undefined) {
    return normalizeHexColor(color);
  }
}
