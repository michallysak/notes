import { inject, Injectable, OnDestroy } from '@angular/core';
import { Subject, Observable, Subscription } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { SharedEventStream, SseService } from '../sse/sse.service';
import {
  BASE_PATH,
  NoteCreatedEventDTO,
  NoteUpdatedEventDTO,
  NoteSseResourceService,
  NoteDeletedEventDTO,
} from '@notes/notes_service';


@Injectable({ providedIn: 'root' })
export class NoteEventsService implements OnDestroy {
  private connectSub = new Subscription();
  private authSub: Subscription;
  private noteEventsSubject = new Subject<NoteCreatedEventDTO>();
  public noteEvents$ = this.noteEventsSubject.asObservable();
  private noteUpdatedEventsSubject = new Subject<NoteUpdatedEventDTO>();
  public noteUpdatedEvents$ = this.noteUpdatedEventsSubject.asObservable();
  private noteDeletedEventsSubject = new Subject<NoteDeletedEventDTO>();
  public noteDeletedEvents$ = this.noteDeletedEventsSubject.asObservable();
  private basePath = inject(BASE_PATH);
  private stream?: SharedEventStream;

  constructor(
    private sse: SseService,
    private auth: AuthService,
    private noteSse: NoteSseResourceService,
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
    let noteCreatedEventType = NoteCreatedEventDTO.TypeEnum.NOTECREATEDEVENT;
    let noteUpdatedEventType = NoteUpdatedEventDTO.TypeEnum.NOTEUPDATEDEVENT;
    let noteDeletedEventType = NoteDeletedEventDTO.TypeEnum.NOTEDELETEDEVENT;
    const requestedEvents = [noteCreatedEventType, noteUpdatedEventType, noteDeletedEventType];

    const keySub = this.noteSse.createStreamKey(requestedEvents).subscribe(({ key }) => {
      if (key) {
        this.stream = this.sse.openSharedEventStream({
          baseUrl: this.basePath,
          path: '/notes/events',
          key: key,
          onError: (event) => console.error('SSE error', event),
          onOpen: (event) => console.log('SSE open'),
        });

        const created$ = this.stream.get<NoteCreatedEventDTO>(noteCreatedEventType);
        this.forwardToSubject(created$, this.noteEventsSubject, this.connectSub);

        const updated$ = this.stream.get<NoteUpdatedEventDTO>(noteUpdatedEventType);
        this.forwardToSubject(updated$, this.noteUpdatedEventsSubject, this.connectSub);

        const deleted$ = this.stream.get<NoteDeletedEventDTO>(noteDeletedEventType);
        this.forwardToSubject(deleted$, this.noteDeletedEventsSubject, this.connectSub);

        return;
      }
    });

    this.connectSub.add(keySub);
  }

  private disconnect() {
    if (!this.connectSub) {
      return;
    }
    try {
      this.connectSub.unsubscribe();
    } catch (_) {}
    try {
      this.stream?.close();
    } catch (_) {}
    this.stream = undefined;
  }

  private forwardToSubject<E>(obs: Observable<E>, subj: Subject<E>, parent: Subscription) {
    const sub = obs.subscribe({
      next: (v) => subj.next(v),
      error: (e) => {
        try {
          subj.error(e);
        } catch (_) {}
      },
      complete: () => {
        try {
          subj.complete();
        } catch (_) {}
      },
    });
    parent.add(sub);
    return sub;
  }

  ngOnDestroy(): void {
    if (this.connectSub) {
      try {
        this.connectSub.unsubscribe();
      } catch (_) {}
    }
    this.authSub?.unsubscribe();
    try {
      this.stream?.close();
    } catch (_) {}
    this.stream = undefined;
  }
}
