import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TooltipModule } from 'primeng/tooltip';
import { MenuModule } from 'primeng/menu';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { NoteMetaComponent } from '../note-change-datetime/note-meta.component';
import { NoteResponse } from '@notes/notes_service';
import { MenuItem } from 'primeng/api';

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
    NoteMetaComponent,
  ],
  styleUrls: ['./note-card.component.scss'],
  templateUrl: './note-card.component.html',
})
export class NoteCardComponent {
  @Input({ required: true }) note!: NoteResponse;
  items: MenuItem[] = [];

  constructor(private translate: TranslateService) {}

  ngOnInit() {
    this.items = [
      {
        label: this.translate.instant('NOTES.DELETE'),
        icon: 'pi pi-trash',
        command: () => console.log('delete', this.note.id),
      },
    ];
  }

  handleCardClick() {
    console.log('click', this.note.id);
  }

  onPinClick(evt: Event) {
    evt.stopPropagation();
    console.log('pin', this.note.id);
  }

  onMenuClick(evt: Event, menu: any) {
    evt.stopPropagation();
    menu.toggle(evt);
  }
}
