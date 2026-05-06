import { Component, EventEmitter, Input, Output, signal, effect, Signal } from '@angular/core';
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
import { mixHexWithBase } from '../../utils/color-contrast.util';
import { AuthService } from '../../services/auth/auth.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { UserResponse } from '@notes/notes_service';

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
  @Output() shareClick = new EventEmitter<Note>();
  items: MenuItem[] = [];

  isAuthor = signal(false);
  currentUser: Signal<UserResponse | null | undefined>;

  constructor(
    private translate: TranslateService,
    private noteService: NoteService,
    private authService: AuthService,
  ) {
    this.currentUser = toSignal(this.authService.currentUser$);

    effect(() => {
      const user = this.currentUser();
      this.isAuthor.set(!!(user && user.id === this.note.authorId));
    });
  }


  ngOnInit() {
    this.items = [
      {
        label: this.translate.instant('NOTES.SHARE'),
        icon: 'pi pi-share-alt',
        command: () => {
          this.shareClick.emit(this.note);
        },
      },
      {
        label: this.translate.instant('NOTES.DELETE'),
        icon: 'pi pi-trash',
        command: () => {
          this.noteService.deleteNote(this.note.id).subscribe({
            next: () => console.log('deleted', this.note?.id),
            error: (err) => console.error('delete failed', err),
          });
        },
      },
    ];
  }

  get cardPt() {
    const backgroundColor = this.cardBackgroundColor();
    const textColor = this.cardTextColor();

    return {
      root: {
        style: {
          backgroundColor,
          color: textColor,
          borderRadius: '0.75rem',
          overflow: 'hidden',
        },
      },
      header: { style: { backgroundColor, color: textColor, padding: '0 0.75rem 0' } },
      body: { style: { backgroundColor, color: textColor, padding: '0 0.75rem 0.75rem' } },
      content: { style: { backgroundColor, color: textColor, padding: '0.5rem 0 0' } },
      footer: { style: { backgroundColor, color: textColor } },
    };
  }

  get controlButtonPt() {
    const textColor = this.cardTextColor();

    return {
      root: {
        style: {
          color: textColor,
        },
      },
      icon: {
        style: {
          color: textColor,
        },
      },
    };
  }

  cardBackgroundColor() {
    return mixHexWithBase(this.note?.style?.color, 'var(--p-content-background)', 32);
  }

  cardTextColor() {
    return 'var(--p-text-color)';
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
