import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TooltipModule } from 'primeng/tooltip';
import { MenuModule } from 'primeng/menu';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { NoteChangeDateTimeComponent } from '../note-change-datetime/note-change-date-time.component';
import { MenuItem } from 'primeng/api';
import { Note } from '../../types/note';
import { NoteService } from '../../services/note/note.service';

@Component({
  selector: 'app-note-card',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    CardModule,
    TooltipModule,
    MenuModule,
    TranslatePipe,
    NoteChangeDateTimeComponent,
  ],
  styleUrls: ['./note-card.component.scss'],
  templateUrl: './note-card.component.html',
})
export class NoteCardComponent {
  @Input({ required: true }) note!: Note;
  @Output() onClick = new EventEmitter<Note>();
  @Output() pinClick = new EventEmitter<Note>();
  items: MenuItem[] = [];

  constructor(private translate: TranslateService, private noteService: NoteService) {}

  ngOnInit() {
    this.items = [
      {
        label: this.translate.instant('NOTES.DELETE'),
        icon: 'pi pi-trash',
        command: () => {
          if (!this.note?.id) return;
          this.noteService.deleteNote(this.note.id).subscribe({
            next: () => console.log('deleted', this.note?.id),
            error: (err) => console.error('delete failed', err),
          });
        },
      },
    ];
  }

  handleCardClick() {
    console.log('click', this.note?.id);
    this.onClick.emit(this.note);
  }

  onPinClick(evt: Event) {
    evt.stopPropagation();
    this.pinClick.emit(this.note);
  }

  onMenuClick(evt: Event, menu: any) {
    evt.stopPropagation();
    menu.toggle(evt);
  }
}
