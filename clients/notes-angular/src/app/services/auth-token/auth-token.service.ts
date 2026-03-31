import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthTokenService {
  private readonly key = 'authToken';

  getItem() {
    return localStorage.getItem(this.key)
  }

  setItem(value: string | undefined) {
    if (!value) {
      this.revoke();
      return;
    }
    localStorage.setItem(this.key, value);
  }

  revoke() {
    localStorage.removeItem(this.key);
  }
}

