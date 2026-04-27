import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { NoteResponse } from '@notes/notes_service';

@Component({
  selector: 'app-note-change-datetime',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  template: `
    <div class="note-meta">
      <span>
        {{
          note.updated
            ? ('NOTES.META.EDITED' | translate: { date: (note.updated | date:'dd.MM.yyyy, HH:mm') })
            : ('NOTES.META.CREATED' | translate: { date: (note.created | date:'dd.MM.yyyy, HH:mm') })
        }}
      </span>
    </div>
  `,
  styles: [
    `
      .note-meta {
        font-size: 0.85rem;
        opacity: 0.8;
        margin-top: 0.25rem;
      }
    `,
  ],
})
export class NoteChangeDateTimeComponent {
  @Input({required: true}) note!: NoteResponse;
}

