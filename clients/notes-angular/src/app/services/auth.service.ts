import { Injectable } from '@angular/core';
import { BehaviorSubject, firstValueFrom, Observable, switchMap, tap } from 'rxjs';
import {
  AuthTokenResponse,
  LoginUserRequest,
  UserResponse,
  UsersAPIService,
} from '@notes/notes_service';
import { AuthTokenService } from './auth-token.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject = new BehaviorSubject<UserResponse | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

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

  login(loginUserRequest: LoginUserRequest): Observable<UserResponse> {
    return this.usersApi.loginUser(loginUserRequest).pipe(
      tap(({ token }: AuthTokenResponse) => {
        this.tokenService.setItem(token);
      }),
      switchMap(() => this.usersApi.getCurrentUser()),
      tap((user) => this.currentUserSubject.next(user)),
    );
  }
}




