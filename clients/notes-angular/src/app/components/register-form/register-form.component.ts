import { Component, EventEmitter, Output, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { RegisterUserRequest } from '@notes/notes_service';
import { AuthService } from '../../services/auth/auth.service';
import { TranslatePipe } from '@ngx-translate/core';
import { catchError, EMPTY } from 'rxjs';

type UserForm = {
  email: FormControl<string>;
  password: FormControl<string>;
};

@Component({
  selector: 'app-register-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ButtonModule, InputTextModule, TranslatePipe],
  templateUrl: './register-form.component.html',
  styleUrls: ['./register-form.component.scss'],
})
export class RegisterFormComponent {
  form: FormGroup<UserForm>;
  error = signal(false);
  @Output() toggleToLogin = new EventEmitter<void>();

  constructor(
    private router: Router,
    private auth: AuthService,
  ) {
    this.form = new FormGroup<UserForm>({
      email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
      password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(8)] }),
    });
    this.form.valueChanges.subscribe(() => this.error.set(false));
  }

  register() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const registerUserRequest: RegisterUserRequest = {
      email: this.form.controls.email.value,
      password: this.form.controls.password.value,
    };
    this.auth
      .register(registerUserRequest)
      .pipe(
        catchError(() => {
          this.error.set(true);
          return EMPTY;
        }),
      )
      .subscribe(() => this.router.navigate(['/']));
  }

  protected toggleLogin() {
    this.toggleToLogin.emit();
  }
}

