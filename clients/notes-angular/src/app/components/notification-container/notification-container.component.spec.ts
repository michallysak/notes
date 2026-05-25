import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NotificationContainerComponent } from './notification-container.component';
import { TranslateModule } from '@ngx-translate/core';
import { NotificationService } from '../../services/notification/notification.service';
import { Subject } from 'rxjs';

describe('NotificationContainerComponent', () => {
  let component: NotificationContainerComponent;
  let fixture: ComponentFixture<NotificationContainerComponent>;
  let notificationServiceMock: any;
  let notificationsSubject: Subject<any[]>;

  beforeEach(async () => {
    notificationsSubject = new Subject();
    notificationServiceMock = {
      notifications$: notificationsSubject.asObservable()
    };

    await TestBed.configureTestingModule({
      imports: [
        NotificationContainerComponent,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: NotificationService, useValue: notificationServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render notifications when notifications$ emits', () => {
    const mockNotifications = [
      { id: '1', type: 'success', message: 'Test success msg' },
      { id: '2', type: 'error', message: 'Test error msg' }
    ];

    notificationsSubject.next(mockNotifications);
    fixture.detectChanges(); // update view

    const wrapperElements = fixture.nativeElement.querySelectorAll('.notification-wrapper');
    expect(wrapperElements.length).toBe(2);

    expect(wrapperElements[0].classList.contains('notification-success')).toBeTruthy();
    expect(wrapperElements[1].classList.contains('notification-error')).toBeTruthy();

    // Check if texts are rendered (using ngx-translate mock which just returns the key)
    expect(wrapperElements[0].textContent).toContain('Test success msg');
    expect(wrapperElements[1].textContent).toContain('Test error msg');
  });
});

