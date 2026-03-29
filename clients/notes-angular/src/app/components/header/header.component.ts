import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TranslatePipe } from '@ngx-translate/core';
import * as AuthModule from '../../services/auth.service';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { UserResponse } from '@notes/notes_service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, ButtonModule, TranslatePipe],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent {
  currentUser$: Observable<UserResponse | null>;

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
}
