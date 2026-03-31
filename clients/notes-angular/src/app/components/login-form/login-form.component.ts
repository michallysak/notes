import { Component, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { LoginUserRequest } from '@notes/notes_service';
import * as AuthModule from '../../services/auth/auth.service';
import { TranslatePipe } from '@ngx-translate/core';
import { catchError, throwError } from 'rxjs';

type UserForm = {
  email: FormControl<string>;
  password: FormControl<string>;
};

@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ButtonModule, InputTextModule, TranslatePipe],
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.scss'],
})
export class LoginFormComponent {
  form: FormGroup<UserForm>;
  error = signal(false);

  constructor(
    private router: Router,
    private auth: AuthModule.AuthService,
  ) {
    this.form = new FormGroup<UserForm>({
      email: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    });
    this.form.valueChanges.subscribe(() => this.error.set(false));
  }

  login() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const loginUserRequest: LoginUserRequest = {
      email: this.form.controls.email.value,
      password: this.form.controls.password.value,
    };
    this.auth
      .login(loginUserRequest)
      .pipe(
        catchError(() => {
          this.error.set(true);
          return throwError(() => new Error('Login failed'));
        }),
      )
      .subscribe(() => this.router.navigate(['/']));
  }

  protected toggleRegister() {
    console.log('Toggle register form');
  }
}
