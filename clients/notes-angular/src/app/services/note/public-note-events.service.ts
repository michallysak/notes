import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SharedEventStream, SseService } from '../sse/sse.service';
import {
  BASE_PATH,
  DomainEventDTO,
  KeyResponse,
  NotePublicShareRemovedEventDTO,
  NotePublicShareUpsertedEventDTO,
  NoteUpdatedEventDTO,
  PublicNoteSseResourceService,
} from '@notes/notes_service';

const REFRESH_MARGIN_MS = 30_000;
const MIN_REFRESH_MS = 5_000;
const RETRY_DELAY_MS = 5_000;

@Injectable({ providedIn: 'root' })
export class PublicNoteEventsService {
  private basePath = inject(BASE_PATH, { optional: true });

  constructor(
    private sse: SseService,
    private publicNoteSse: PublicNoteSseResourceService,
  ) {}

  connect(publicShareId: string): Observable<DomainEventDTO> {
    return new Observable<DomainEventDTO>((subscriber) => {
      let stream: SharedEventStream | undefined;
      let refreshTimer: ReturnType<typeof setTimeout> | undefined;
      let retryTimer: ReturnType<typeof setTimeout> | undefined;
      let keySub: { unsubscribe: () => void } | undefined;
      let closed = false;

      const clearTimers = () => {
        if (refreshTimer) {
          clearTimeout(refreshTimer);
          refreshTimer = undefined;
        }
        if (retryTimer) {
          clearTimeout(retryTimer);
          retryTimer = undefined;
        }
      };

      const scheduleRefresh = (expiresAt?: string | Date) => {
        if (!expiresAt) {
          return;
        }
        const expiryMs = new Date(expiresAt).getTime();
        if (Number.isNaN(expiryMs)) {
          return;
        }
        const delay = Math.max(expiryMs - Date.now() - REFRESH_MARGIN_MS, MIN_REFRESH_MS);
        refreshTimer = setTimeout(() => open(), delay);
      };

      const scheduleRetry = () => {
        if (closed || retryTimer) {
          return;
        }
        retryTimer = setTimeout(() => {
          retryTimer = undefined;
          open();
        }, RETRY_DELAY_MS);
      };

      const open = () => {
        if (closed) {
          return;
        }
        keySub = this.publicNoteSse.createPublicNoteStreamKey(publicShareId).subscribe({
          next: (response: KeyResponse) => {
            if (closed) {
              return;
            }
            const key = response?.key;
            if (!key || !this.basePath) {
              scheduleRetry();
              return;
            }

            const previous = stream;
            const next = this.sse.openSharedEventStream({
              baseUrl: this.basePath,
              path: '/notes/events',
              key,
              onError: (event) => console.error('Public SSE error', event),
            });
            stream = next;
            try {
              previous?.close();
            } catch (_) {}

            const upserted$ = next.get<NotePublicShareUpsertedEventDTO>(
              NotePublicShareUpsertedEventDTO.TypeEnum.NOTEPUBLICSHAREUPSERTEDEVENT,
            );
            upserted$.subscribe((v) => subscriber.next(v));

            const updated$ = next.get<NoteUpdatedEventDTO>(
              NoteUpdatedEventDTO.TypeEnum.NOTEUPDATEDEVENT,
            );
            updated$.subscribe((v) => subscriber.next(v));

            const removed$ = next.get<NotePublicShareRemovedEventDTO>(
              NotePublicShareRemovedEventDTO.TypeEnum.NOTEPUBLICSHAREREMOVEDEVENT,
            );
            removed$.subscribe((v) => subscriber.next(v));

            scheduleRefresh(response?.expiresAt);
          },
          error: () => scheduleRetry(),
        });
      };

      open();

      return () => {
        closed = true;
        clearTimers();
        try {
          keySub?.unsubscribe();
        } catch (_) {}
        try {
          stream?.close();
        } catch (_) {}
        stream = undefined;
      };
    });
  }
}
