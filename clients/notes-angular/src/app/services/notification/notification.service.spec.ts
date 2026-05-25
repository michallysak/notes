import { TestBed } from '@angular/core/testing';
import { NotificationService } from './notification.service';
import { take } from 'rxjs';

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NotificationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should show notification and emit it', () => {
    service.show('Test message', 'success', undefined);

    service.notifications$.pipe(take(1)).subscribe(notifs => {
      expect(notifs.length).toBe(1);
      expect(notifs[0].message).toBe('Test message');
      expect(notifs[0].type).toBe('success');
    });
  });

  it('should auto-dismiss notification after duration', () => {
    vi.useFakeTimers();
    service.show('Test timeout', 'info', 1000);

    let currentNotifs: any[] = [];
    const sub = service.notifications$.subscribe(notifs => currentNotifs = notifs);

    expect(currentNotifs.length).toBe(1);

    vi.advanceTimersByTime(1000);

    expect(currentNotifs.length).toBe(0);
    sub.unsubscribe();
    vi.useRealTimers();
  });

  it('should dismiss notification by id', () => {
    service.show('Test dismiss', 'warn', undefined);
    let id = '';
    service.notifications$.pipe(take(1)).subscribe(notifs => {
      id = notifs[0].id;
    });

    service.dismiss(id);

    service.notifications$.pipe(take(1)).subscribe(notifs => {
      expect(notifs.length).toBe(0);
    });
  });
});
