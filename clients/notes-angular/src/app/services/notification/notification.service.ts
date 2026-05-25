import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Notification {
  id: string;
  message: string;
  type: 'info' | 'success' | 'warn' | 'error';
  duration: number | undefined;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  private nextId = 0;

  show(message: string, type: Notification['type'] = 'info', duration: number | undefined = 3000) {
    const id = `notification-${this.nextId++}`;
    const notification: Notification = { id, message, type, duration };

    this.notificationsSubject.next([...this.notificationsSubject.value, notification]);

    if (duration !== undefined && duration > 0) {
      setTimeout(() => {
        this.dismiss(id);
      }, duration);
    }
  }

  dismiss(id: string) {
    this.notificationsSubject.next(this.notificationsSubject.value.filter((n) => n.id !== id));
  }
}

