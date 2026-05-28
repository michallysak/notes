import { Component, inject, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TranslatePipe } from '@ngx-translate/core';
import * as AuthModule from '../../services/auth/auth.service';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { UserResponse } from '@notes/notes_service';
import { NoteSearchComponent } from '../note-search/note-search.component';
import { Toolbar } from 'primeng/toolbar';
import { NoteChangeDialogComponent } from '../note-change-dialog/note-change-dialog.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, ButtonModule, TranslatePipe, NoteSearchComponent, Toolbar, NoteChangeDialogComponent],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent {
  currentUser$: Observable<UserResponse | null>;
  showCreateDialog = signal(false);

  constructor(
    private auth: AuthModule.AuthService,
    private router: Router,
  ) {
    this.currentUser$ = this.auth.currentUser$;
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  protected openCreate() {
    this.showCreateDialog.set(true);
  }
}
