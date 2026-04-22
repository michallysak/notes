import { Injectable } from '@angular/core';
import { BehaviorSubject, firstValueFrom, Observable, switchMap, tap } from 'rxjs';
import {
  AuthTokenResponse,
  LoginUserRequest,
  RegisterUserRequest,
  UsersAPIService,
} from '@notes/notes_service';
import { AuthTokenService } from '../auth-token/auth-token.service';
import { User } from '../../types/user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  public logged$ = this.currentUser$.pipe(switchMap((user) => [!!user]));

  constructor(
    private usersApi: UsersAPIService,
    private tokenService: AuthTokenService,
  ) {}

  public async init() {
    const token = this.getToken();
    if (!token) {
      return;
    }
    try {
      const user = await firstValueFrom(this.usersApi.getCurrentUser());
      this.currentUserSubject.next(user);
    } catch (err) {
      this.logout();
    }
  }

  getToken() {
    return this.tokenService.getItem();
  }

  public isAuthenticated() {
    return !!this.getToken();
  }

  public logout() {
    this.tokenService.revoke();
    this.currentUserSubject.next(null);
  }

  login(loginUserRequest: LoginUserRequest): Observable<User> {
    return this.authenticateWithToken(this.usersApi.loginUser(loginUserRequest));
  }

  register(registerUserRequest: RegisterUserRequest): Observable<User> {
    return this.authenticateWithToken(this.usersApi.registerUser(registerUserRequest));
  }

  private authenticateWithToken(authResponse$: Observable<AuthTokenResponse>): Observable<User> {
    return authResponse$.pipe(
      tap(({ token }: AuthTokenResponse) => {
        this.tokenService.setItem(token);
      }),
      switchMap(() => this.usersApi.getCurrentUser()),
      tap((user) => this.currentUserSubject.next(user)),
    );
  }
}






