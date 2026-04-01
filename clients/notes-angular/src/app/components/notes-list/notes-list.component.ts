import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NoteService } from '../../services/note/note.service';
import { NoteCardComponent } from '../note-card/note-card.component';
import { Observable, Subscription } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TextareaModule } from 'primeng/textarea';
import { InputTextModule } from 'primeng/inputtext';
import { TranslatePipe } from '@ngx-translate/core';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { Note } from '../../types/note';
import { NoteChangeDialogComponent } from '../note-change-dialog/note-change-dialog.component';

type ChangeNoteDialogStatus = { visible: false } | ({ visible: true } & { note: Note | null });

@Component({
  selector: 'app-notes-list',
  standalone: true,
  imports: [
    CommonModule,
    NoteCardComponent,
    ReactiveFormsModule,
    ButtonModule,
    DialogModule,
    TextareaModule,
    InputTextModule,
    TranslatePipe,
    ProgressSpinnerModule,
    NoteChangeDialogComponent,
  ],
  styleUrls: ['./notes-list.component.scss'],
  templateUrl: './notes-list.component.html',
})
export class NotesListComponent implements OnInit, OnDestroy {
  private notes$: Observable<Note[]>;
  private notesSubscription: Subscription | null = null;
  pinnedNotes = signal<Note[]>([]);
  otherNotes = signal<Note[]>([]);
  clickNote = signal<ChangeNoteDialogStatus>({ visible: false});

  constructor(private noteService: NoteService) {
    this.notes$ = this.noteService.notes$;
  }

  ngOnInit(): void {
    this.notesSubscription = this.notes$.subscribe((list) => {
      this.pinnedNotes.set(list.filter((n) => !!n.pinned));
      this.otherNotes.set(list.filter((n) => !n.pinned));
    });
  }
  ngOnDestroy(): void {
    this.notesSubscription?.unsubscribe();
  }

  openCreate() {
    console.log('open create');
    this.clickNote.set({ visible: true, note: null });
  }

  noteCardClick(note: Note) {
    this.clickNote.set({ visible: true, note });
  }

  noteDialogClose() {
    this.clickNote.set({ visible: false});
  }
}
