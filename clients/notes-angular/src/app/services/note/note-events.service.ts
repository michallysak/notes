import { inject, Injectable, NgZone, OnDestroy } from '@angular/core';
import { Subject, Subscription } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { SseEvent, SseService } from '../sse/sse.service';
import { BASE_PATH } from '@notes/notes_service';

interface DomainEvent<T> {
  type: string;
  payload: T;
}

export interface NoteCreatedEvent extends DomainEvent<{
  title: string;
  content: string;
}> {
  type: 'NOTE_CREATED_EVENT';
}

type NoteEvent = NoteCreatedEvent;

@Injectable({ providedIn: 'root' })
export class NoteEventsService implements OnDestroy {
  private connectSub?: Subscription;
  private authSub: Subscription;
  private noteEventsSubject = new Subject<NoteEvent>();
  public noteEvents$ = this.noteEventsSubject.asObservable();
  private basePath = inject(BASE_PATH);

  constructor(
    private sse: SseService,
    private auth: AuthService,
    private ngZone: NgZone,
  ) {
    this.authSub = this.auth.logged$.subscribe((logged) => {
      if (logged) {
        this.connect();
      } else {
        this.disconnect();
      }
    });
  }

  private connect() {
    this.connectSub = this.sse.connect(this.basePath, '/notes/events').subscribe({
      next: (ev) => {
        if (ev.name === 'NOTE_CREATED_EVENT') {
          this.ngZone.run(() => {
            const data = ev.data as NoteCreatedEvent['payload'];
            const event: NoteEvent = {
              type: 'NOTE_CREATED_EVENT',
              payload: {
                title: data.title,
                content: data.content,
              },
            };
            this.noteEventsSubject.next(event);
          });
        }
      },
      error: (err) => {
        this.ngZone.run(() => {
          try {
            this.noteEventsSubject.error(err);
          } catch (_) {}
        });
      },
      complete: () => {
        this.ngZone.run(() => {
          try {
            this.noteEventsSubject.complete();
          } catch (_) {}
        });
      },
    });
  }

  private disconnect() {
    if (!this.connectSub) {
      return;
    }
    try {
      this.connectSub.unsubscribe();
    } catch (_) {}
    this.connectSub = undefined;
  }

  ngOnDestroy(): void {
    if (this.connectSub) {
      try {
        this.connectSub.unsubscribe();
      } catch (_) {}
    }
    this.authSub?.unsubscribe();
    this.noteEventsSubject.complete();
  }
}
