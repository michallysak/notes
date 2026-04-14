import { inject, Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';

export interface SseEvent<T> {
  id: string;
  name: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class SseService {
  private ngZone = inject(NgZone);
  private authService = inject(AuthService);

  connect(basePath: string, path: string): Observable<Partial<SseEvent<unknown>>> {
    const token = this.authService.getToken();
    const base = basePath || '';
    const sep = base.endsWith('/') || path.startsWith('/') ? '' : '/';
    const url = `${base}${sep}${path}`;

    return new Observable<Partial<SseEvent<unknown>>>((observer) => {
      const controller = new AbortController();
      const signal = controller.signal;

      const errFun = (err: unknown) => {
        console.error('err', err);
        // If the fetch was aborted treat it as a normal completion instead of an error
        const name = (err as any)?.name;
        if (name === 'AbortError') {
          this.ngZone.run(() => observer.complete());
          return;
        }
        this.ngZone.run(() => observer.error(err));
      };

      fetch(url, {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${token}`,
        },
        credentials: 'include',
        signal,
      })
        .then((response) => {
          console.log('response', response);
          if (!response.ok) {
            throw new Error(`SSE request failed: ${response.status} ${response.statusText}`);
          }
          if (!response.body) {
            throw new Error('SSE response has no body');
          }
          const reader = response.body.getReader();
          const decoder = new TextDecoder();
          let buffer = '';

          const read = () => {
            reader
              .read()
              .then(({ done, value }) => {
                if (done) {
                  this.ngZone.run(() => observer.complete());
                  console.log('SSE stream completed');
                  return;
                }

                buffer += decoder.decode(value, { stream: true });

                const events = buffer.split('\n\n');
                // the last element is either a partial event or empty fragment - keep in buffer
                buffer = events.pop()!;

                for (const ev of events) {
                  // ignore empty fragments or heartbeat/newline-only messages
                  if (!ev || !ev.trim()) {
                    continue;
                  }
                  try {
                    const parsed = this.parseSseEvent(ev);
                    this.ngZone.run(() => observer.next(parsed));
                  } catch (err) {
                    errFun(err);
                  }
                }

                read();
              })
              .catch((err) => errFun(err));
          };

          read();
        })
        .catch((err) => errFun(err));

      return () => {
        try {
          controller.abort();
        } catch (e) {
          console.error(e);
        }
      };
    });
  }

  private parseSseEvent(raw: string): Partial<SseEvent<unknown>> {
    // Be defensive: ignore empty/whitespace-only fragments
    if (!raw || !raw.trim()) {
      return {};
    }

    const lines = raw.split(/\r?\n/);
    let id: string | undefined;
    let name: string | undefined;
    const dataLines: string[] = [];
    for (const line of lines) {
      // ignore comment lines (start with ':')
      if (!line) {
        continue;
      }
      if (line.startsWith(':')) {
        continue;
      }
      if (line.startsWith('id:')) {
        id = line.slice(3).trim();
      } else if (line.startsWith('event:')) {
        name = line.slice(6).trim();
      } else if (line.startsWith('data:')) {
        dataLines.push(line.slice(5));
      }
    }

    const dataText = dataLines.map((l) => l.replace(/^\s+|\s+$/g, '')).join('\n');
    if (!dataText) {
      return { id, name, data: undefined };
    }

    try {
      const data = JSON.parse(dataText);
      return { id, name, data };
    } catch (err) {
      // If payload is not valid JSON, return as raw string to avoid crashing the stream
      console.warn('Failed to parse SSE data as JSON, returning raw string', dataText, err);
      return { id, name, data: dataText };
    }
  }
}
