import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../services/auth.service';
import { AuthDialogComponent } from '../../components/auth-dialog/auth-dialog.component';
import { NotesListComponent } from '../../components/notes-list/notes-list.component';

@Component({
  selector: 'app-notes-page',
  imports: [CommonModule, ButtonModule, AuthDialogComponent, NotesListComponent],
  templateUrl: './notes-page.html',
  styleUrls: ['./notes-page.scss'],
})
export class NotesPage implements OnInit {
  showLogin = false;
  logged = false;

  constructor(public auth: AuthService) {}

  ngOnInit(): void {
    this.auth.currentUser$.subscribe((user) => {
      this.logged = !!user;
      if (!user) {
        this.showLogin = true;
      }
    });
  }
}
