import { Component, inject, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { ToolbarModule } from 'primeng/toolbar';
import { AuthService } from '../../services/auth/auth.service';
import { AuthDialogComponent } from '../../components/auth-dialog/auth-dialog.component';
import { NotesListComponent } from '../../components/notes-list/notes-list.component';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { HeaderComponent } from '../../components/header/header.component';

@Component({
  selector: 'app-notes-page',
  imports: [
    CommonModule,
    ButtonModule,
    ToolbarModule,
    AuthDialogComponent,
    NotesListComponent,
    HeaderComponent,
  ],
  templateUrl: './notes-page.html',
  styleUrls: ['./notes-page.scss'],
})
export class NotesPage {
  auth = inject(AuthService);
  router = inject(Router);
  logged = toSignal(this.auth.logged$, { initialValue: false });
  currentUser = toSignal(this.auth.currentUser$);

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
