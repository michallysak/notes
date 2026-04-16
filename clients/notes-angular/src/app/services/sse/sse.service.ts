import { inject, Injectable, NgZone } from '@angular/core';
import { Observable, Subject } from 'rxjs';

export interface SharedEventStream {
  get: <E extends { type: string }>(t: E | E['type']) => Observable<E>;
  close: () => void;
}

export interface SharedEventStreamSettings {
  baseUrl: string;
  path: string;
  key: string;
  onOpen?: (event: Event) => void;
  onError?: (event: Event) => void;
}

@Injectable({ providedIn: 'root' })
export class SseService {
  private ngZone = inject(NgZone);

  openSharedEventStream(settings: SharedEventStreamSettings): SharedEventStream {
    const { baseUrl, path, key, onOpen, onError } = settings;
    const base = baseUrl || '';
    const sep = base.endsWith('/') || path.startsWith('/') ? '' : '/';
    const urlBase = `${base}${sep}${path}`;
    const url = key
      ? `${urlBase}${urlBase.includes('?') ? '&' : '?'}key=${encodeURIComponent(key)}`
      : urlBase;

    const es = new EventSource(url, { withCredentials: true });
    console.log('[openSharedEventStream] opening EventSource', url);

    const subjects: Record<string, Subject<any>> = {};
    const listeners: Record<string, EventListener> = {};

    const makeListener = (name: string) => {
      const fn: EventListener = (ev: Event) => {
        try {
          const mev = ev as MessageEvent;
          let parsed: any;
          try {
            parsed = mev.data ? JSON.parse(mev.data) : undefined;
          } catch (e) {
            parsed = mev.data;
          }

          const subj = subjects[name];
          if (subj) {
            const emitted = name === 'message' ? parsed : { type: name, payload: parsed };
            this.ngZone.run(() => subj.next(emitted));
          }
        } catch (err) {
          const subj = subjects[name];
          if (subj) {
            this.ngZone.run(() => subj.error(err as any));
          }
        }
      };
      return fn;
    };

    if (onOpen) {
      es.onopen = (e) => {
        this.ngZone.run(() => onOpen(e));
      };
    }
    if (onError) {
      es.onerror = (e) => {
        this.ngZone.run(() => onError(e));
      };
    }

    const get = <E extends { type: string }>(t: E | E['type']): Observable<E> => {
      const name = typeof t === 'string' ? t : t.type;
      if (!subjects[name]) {
        subjects[name] = new Subject<any>();
        const fn = makeListener(name);
        listeners[name] = fn;
        try {
          es.addEventListener(name, fn as EventListener);
        } catch (err) {
          console.warn('Shared SSE: could not add listener for', name, err);
        }
      }

      return subjects[name].asObservable() as Observable<E>;
    };

    const close = () => {
      try {
        for (const name of Object.keys(listeners)) {
          try {
            es.removeEventListener(name, listeners[name]);
          } catch (_) {}
        }
        try {
          es.close();
        } catch (_) {}
        for (const k of Object.keys(subjects)) {
          try {
            subjects[k].complete();
          } catch (_) {}
        }
      } catch (e) {
        console.error('Error tearing down shared SSE', e);
      }
    };

    return { get, close };
  }
}
