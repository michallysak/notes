import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Button } from 'primeng/button';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-forbidden-page',
  imports: [Button, TranslatePipe],
  templateUrl: './forbidden-page.html',
  styleUrl: './forbidden-page.scss',
})
export class ForbiddenPage {
  constructor(private router: Router) {}

  goHome() {
    this.router.navigate(['/']);
  }
}
