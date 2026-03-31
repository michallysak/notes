import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../services/auth/auth.service';
import { AuthDialogComponent } from '../../components/auth-dialog/auth-dialog.component';
import { NotesListComponent } from '../../components/notes-list/notes-list.component';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-notes-page',
  imports: [CommonModule, ButtonModule, AuthDialogComponent, NotesListComponent],
  templateUrl: './notes-page.html',
  styleUrls: ['./notes-page.scss'],
})
export class NotesPage {
  auth = inject(AuthService);
  logged = toSignal(this.auth.logged$, { initialValue: false });
}
