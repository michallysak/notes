import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { DialogModule } from 'primeng/dialog';
import { LoginFormComponent } from '../login-form/login-form.component';
import { RegisterFormComponent } from '../register-form/register-form.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-auth-dialog',
  standalone: true,
  imports: [CommonModule, DialogModule, LoginFormComponent, RegisterFormComponent],
  templateUrl: './auth-dialog.component.html',
})
export class AuthDialogComponent {
  @Input() visible = false;
  @Output() visibleChange = new EventEmitter<boolean>();
  isLoginForm = signal(true);

  onHide() {
    this.visible = false;
    this.visibleChange.emit(this.visible);
  }

  toggleForm() {
    this.isLoginForm.update((value) => !value);
  }
}
