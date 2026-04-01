import { Component, EventEmitter, Input, Output, SimpleChanges, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';
import { ProgressBarModule } from 'primeng/progressbar';
import { debounceTime, delay } from 'rxjs/operators';
import {
  CreateNoteRequest,
  NoteResponse,
  NotesAPIService,
  NoteUpdateRequest,
} from '@notes/notes_service';
import { NoteChangeDateTimeComponent } from '../note-change-datetime/note-change-date-time.component';
import { TextRangeComponent } from '../text-range/text-range.component';
import { FloatLabelModule } from 'primeng/floatlabel';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { MessageModule } from 'primeng/message';
import { TranslatePipe } from '@ngx-translate/core';

type NoteForm = {
  title: FormControl<string>;
  content: FormControl<string>;
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
    MessageModule,
    TranslatePipe,
    FloatLabelModule,
  ],
  templateUrl: './note-change-dialog.component.html',
  styleUrls: ['./note-change-dialog.component.scss'],
})
export class NoteChangeDialogComponent {
  @Input({ required: true }) visible = false;
  @Input({ required: true }) note: NoteResponse | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();

  form: FormGroup<NoteForm>;

  saving = signal(false);
  lastSavedNote = signal<NoteResponse | null>(null);
  notSaved = signal(false);

  private saveDebounce = 1000;

  constructor(private notesApi: NotesAPIService) {
    this.form = this.form = new FormGroup<NoteForm>({
      title: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(3), Validators.maxLength(64)],
      }),
      content: new FormControl('', {
        nonNullable: true,
        validators: [Validators.maxLength(2048)],
      }),
    });

    this.form.valueChanges.pipe(debounceTime(this.saveDebounce)).subscribe(() => {
      if (this.form.valid) {
        this.save();
      } else {
        this.notSaved.set(true);
      }
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['note']) {
      if (this.note) {
        this.form.patchValue({ title: this.note.title || '', content: this.note.content || '' });
        this.lastSavedNote.set({ ...this.note });
        this.form.markAsPristine();
        this.notSaved.set(false);
      } else {
        this.form.reset();
        this.lastSavedNote.set(null);
        this.notSaved.set(true);
      }
    }
  }

  onHide() {
    this.visible = false;
    this.visibleChange.emit(this.visible);
    this.form.reset();
  }

  private save() {
    this.notSaved.set(false);
    if (this.form.invalid || this.saving()) {
      return;
    }

    this.saving.set(true);
    this.notSaved.set(false);

    if (this.note && this.note.id) {
      const body: NoteUpdateRequest = this.form.value;
      this.notesApi
        .updateNote(this.note.id, body)
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
      this.notesApi
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
    this.form.markAsPristine();
    this.note = res;
    this.notSaved.set(false);
  }

  private onSaveError() {
    this.saving.set(false);
    this.notSaved.set(true);
  }
}
