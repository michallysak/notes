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
  NotePermissionsSetEventDTO,
  NoteAccessRemovedEventDTO,
  NotePublicShareUpsertedEventDTO,
  NotePublicShareRemovedEventDTO,
  DomainEventDTO,
} from '@notes/notes_service';


@Injectable({ providedIn: 'root' })
export class NoteEventsService implements OnDestroy {
  private connectSub = new Subscription();
  private authSub: Subscription;
  private domainEventsSubject = new Subject<DomainEventDTO>();
  public domainEvents$ = this.domainEventsSubject.asObservable();
  private basePath = inject(BASE_PATH, { optional: true });
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
    if (this.stream) {
      return;
    }
    let noteCreatedEventType = NoteCreatedEventDTO.TypeEnum.NOTECREATEDEVENT;
    let noteUpdatedEventType = NoteUpdatedEventDTO.TypeEnum.NOTEUPDATEDEVENT;
    let noteDeletedEventType = NoteDeletedEventDTO.TypeEnum.NOTEDELETEDEVENT;
    let notePermissionsSetEventType = NotePermissionsSetEventDTO.TypeEnum.NOTEPERMISSIONSSETEVENT;
    let noteAccessRemovedEventType = NoteAccessRemovedEventDTO.TypeEnum.NOTEACCESSREMOVEDEVENT;
    let notePublicShareUpsertedEventType =
      NotePublicShareUpsertedEventDTO.TypeEnum.NOTEPUBLICSHAREUPSERTEDEVENT;
    let notePublicShareRemovedEventType =
      NotePublicShareRemovedEventDTO.TypeEnum.NOTEPUBLICSHAREREMOVEDEVENT;
    const requestedEvents = [
      noteCreatedEventType,
      noteUpdatedEventType,
      noteDeletedEventType,
      notePermissionsSetEventType,
      noteAccessRemovedEventType,
      notePublicShareUpsertedEventType,
      notePublicShareRemovedEventType,
    ];

    const keySub = this.noteSse.createStreamKey(requestedEvents).subscribe(({ key }) => {
      if (key && this.basePath) {
        this.stream = this.sse.openSharedEventStream({
          baseUrl: this.basePath,
          path: '/notes/events',
          key: key,
          onError: (event) => console.error('SSE error', event),
          onOpen: (event) => console.log('SSE open'),
        });

        const created$ = this.stream.get<NoteCreatedEventDTO>(noteCreatedEventType);
        this.forwardToSubject(created$, this.domainEventsSubject, this.connectSub);

        const updated$ = this.stream.get<NoteUpdatedEventDTO>(noteUpdatedEventType);
        this.forwardToSubject(updated$, this.domainEventsSubject, this.connectSub);

        const deleted$ = this.stream.get<NoteDeletedEventDTO>(noteDeletedEventType);
        this.forwardToSubject(deleted$, this.domainEventsSubject, this.connectSub);

        const permissionSet$ = this.stream.get<NotePermissionsSetEventDTO>(notePermissionsSetEventType);
        this.forwardToSubject(permissionSet$, this.domainEventsSubject, this.connectSub);

        const accessRemoved$ = this.stream.get<NoteAccessRemovedEventDTO>(noteAccessRemovedEventType);
        this.forwardToSubject(accessRemoved$, this.domainEventsSubject, this.connectSub);

        const publicShareUpserted$ =
          this.stream.get<NotePublicShareUpsertedEventDTO>(notePublicShareUpsertedEventType);
        this.forwardToSubject(publicShareUpserted$, this.domainEventsSubject, this.connectSub);

        const publicShareRemoved$ =
          this.stream.get<NotePublicShareRemovedEventDTO>(notePublicShareRemovedEventType);
        this.forwardToSubject(publicShareRemoved$, this.domainEventsSubject, this.connectSub);

        return;
      }
    });

    this.connectSub.add(keySub);
  }

  private disconnect() {
    try {
      this.connectSub.unsubscribe();
    } catch (_) {}
    this.connectSub = new Subscription();
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
