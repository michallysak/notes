import { Component, ChangeDetectionStrategy, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, Notification } from '../../services/notification/notification.service';
import { MessageModule } from 'primeng/message';
import { TranslatePipe } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-notification-container',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, MessageModule, TranslatePipe],
  template: `
    <div class="notification-container">
      @for (notification of notifications; track notification.id) {
        <div class="notification-wrapper" [ngClass]="'notification-' + notification.type">
          <p-message [severity]="notification.type">{{
              notification.message | translate
            }}
          </p-message>
        </div>
      }
    </div>
  `,
  styles: [
    `
      .notification-container {
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 10000;
        display: flex;
        flex-direction: column;
        gap: 10px;
        max-width: 400px;
      }

      .notification-wrapper {
        min-width: 300px;
        animation: slideIn 0.3s ease-out;
        background-color: var(--p-surface-card);
        padding: 1rem;
        border-radius: 0.5rem;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
        display: flex;
        align-items: center;
        margin-top: 4rem;
      }

      @keyframes slideIn {
        from {
          transform: translateX(400px);
          opacity: 0;
        }
        to {
          transform: translateX(0);
          opacity: 1;
        }
      }
    `,
  ],
})
export class NotificationContainerComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  private destroy$ = new Subject<void>();

  constructor(
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.notificationService.notifications$
      .pipe(takeUntil(this.destroy$))
      .subscribe((notifications) => {
        this.notifications = notifications;
        this.cdr.markForCheck();
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}


