import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DialogModule } from 'primeng/dialog';
import { LoginFormComponent } from '../login-form/login-form.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-auth-dialog',
  standalone: true,
  imports: [CommonModule, DialogModule, LoginFormComponent],
  templateUrl: './auth-dialog.component.html',
})
export class AuthDialogComponent {
  @Input() visible = false;
  @Output() visibleChange = new EventEmitter<boolean>();

  onHide() {
    this.visible = false;
    this.visibleChange.emit(this.visible);
  }
}
