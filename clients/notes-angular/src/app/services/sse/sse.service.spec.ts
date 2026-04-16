import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { SseService } from './sse.service';

class MockEventSource {
  static lastInstance: MockEventSource;
  url: string;
  onopen: ((e: Event) => void) | null = () => {};
  onerror: ((e: Event) => void) | null = () => {};
  private listeners: Record<string, EventListener[]> = {};
  closed = false;

  constructor(url: string, _opts?: EventSourceInit) {
    this.url = url;
    MockEventSource.lastInstance = this;
  }

  addEventListener(type: string, fn: EventListener) {
    if (!this.listeners[type]) this.listeners[type] = [];
    this.listeners[type].push(fn);
  }

  removeEventListener(type: string, fn: EventListener) {
    if (this.listeners[type]) {
      this.listeners[type] = this.listeners[type].filter((l) => l !== fn);
    }
  }

  dispatchNamed(type: string, data: unknown) {
    const ev = Object.assign(new MessageEvent(type, { data: JSON.stringify(data) }));
    (this.listeners[type] || []).forEach((fn) => fn(ev));
  }

  dispatchRaw(type: string, data: string) {
    const ev = new MessageEvent(type, { data });
    (this.listeners[type] || []).forEach((fn) => fn(ev));
  }

  dispatchNoData(type: string) {
    const ev = new MessageEvent(type, { data: '' });
    (this.listeners[type] || []).forEach((fn) => fn(ev));
  }

  close() {
    this.closed = true;
  }
}

const defaultSettings = () => ({
  baseUrl: 'http://localhost',
  path: '/events',
  key: 'tok',
});

const makeService = () => {
  TestBed.configureTestingModule({});
  return TestBed.inject(SseService);
};

describe('SseService', () => {
  let origEventSource: typeof EventSource;

  beforeEach(() => {
    origEventSource = (globalThis as any).EventSource;
    (globalThis as any).EventSource = MockEventSource;
  });

  afterEach(() => {
    (globalThis as any).EventSource = origEventSource;
  });

  it('builds a URL with key query param', () => {
    const svc = makeService();
    svc.openSharedEventStream({ baseUrl: 'http://api', path: '/events', key: 'tok' });
    expect(MockEventSource.lastInstance.url).toBe('http://api/events?key=tok');
  });

  it('appends & separator when base URL already contains a query string', () => {
    const svc = makeService();
    svc.openSharedEventStream({ baseUrl: 'http://api', path: '/events?foo=bar', key: 'tok' });
    expect(MockEventSource.lastInstance.url).toContain('&key=tok');
  });

  it('builds URL without key when key is empty', () => {
    const svc = makeService();
    svc.openSharedEventStream({ baseUrl: 'http://api', path: '/events', key: '' });
    expect(MockEventSource.lastInstance.url).toBe('http://api/events');
  });

  it('builds URL from path only when baseUrl is empty', () => {
    const svc = makeService();
    svc.openSharedEventStream({ baseUrl: '', path: '/events', key: '' });
    expect(MockEventSource.lastInstance.url).toBe('/events');
  });

  it('adds separator slash when base does not end with / and path does not start with /', () => {
    const svc = makeService();
    svc.openSharedEventStream({ baseUrl: 'http://api', path: 'events', key: '' });
    expect(MockEventSource.lastInstance.url).toBe('http://api/events');
  });

  it('skips separator when base already ends with /', () => {
    const svc = makeService();
    svc.openSharedEventStream({ baseUrl: 'http://api/', path: 'events', key: '' });
    expect(MockEventSource.lastInstance.url).toBe('http://api/events');
  });

  it('calls onOpen callback when EventSource opens', () => {
    const svc = makeService();
    const onOpen = vi.fn();
    svc.openSharedEventStream({ ...defaultSettings(), onOpen });

    const fakeEvent = new Event('open');
    MockEventSource.lastInstance.onopen!(fakeEvent);

    expect(onOpen).toHaveBeenCalledWith(fakeEvent);
  });

  it('does not throw when onOpen is not provided', () => {
    const svc = makeService();
    svc.openSharedEventStream(defaultSettings());
    expect(() => MockEventSource.lastInstance.onopen!(new Event('open'))).not.toThrow();
  });

  it('calls onError callback when EventSource errors', () => {
    const svc = makeService();
    const onError = vi.fn();
    svc.openSharedEventStream({ ...defaultSettings(), onError });

    const fakeEvent = new Event('error');
    MockEventSource.lastInstance.onerror!(fakeEvent);

    expect(onError).toHaveBeenCalledWith(fakeEvent);
  });

  it('does not throw when onError is not provided', () => {
    const svc = makeService();
    svc.openSharedEventStream(defaultSettings());
    expect(() => MockEventSource.lastInstance.onerror!(new Event('error'))).not.toThrow();
  });

  it('get() returns an observable that emits wrapped events for named types', () => {
    const svc = makeService();
    const stream = svc.openSharedEventStream(defaultSettings());
    const received: unknown[] = [];

    stream.get<{ type: 'NOTE_CREATED'; payload: { id: string } }>('NOTE_CREATED').subscribe((v) => received.push(v));
    MockEventSource.lastInstance.dispatchNamed('NOTE_CREATED', { id: '1' });

    expect(received).toEqual([{ type: 'NOTE_CREATED', payload: { id: '1' } }]);
  });

  it('get() emits parsed payload for "message" type (no type wrapper)', () => {
    const svc = makeService();
    const stream = svc.openSharedEventStream(defaultSettings());
    const received: unknown[] = [];

    stream.get<{ type: 'message' }>('message').subscribe((v) => received.push(v));
    MockEventSource.lastInstance.dispatchNamed('message', { hello: 'world' });

    expect(received).toEqual([{ hello: 'world' }]);
  });

  it('get() returns the same observable when called twice for the same type', () => {
    const svc = makeService();
    const stream = svc.openSharedEventStream(defaultSettings());
    const received1: unknown[] = [];
    const received2: unknown[] = [];

    stream.get<{ type: 'EV' }>('EV').subscribe((v) => received1.push(v));
    stream.get<{ type: 'EV' }>('EV').subscribe((v) => received2.push(v));
    MockEventSource.lastInstance.dispatchNamed('EV', { x: 1 });

    expect(received1).toHaveLength(1);
    expect(received2).toHaveLength(1);
  });

  it('get() handles non-JSON data gracefully (falls back to raw string)', () => {
    const svc = makeService();
    const stream = svc.openSharedEventStream(defaultSettings());
    const received: unknown[] = [];

    stream.get<{ type: 'EV' }>('EV').subscribe((v) => received.push(v));
    MockEventSource.lastInstance.dispatchRaw('EV', 'not-json');

    expect((received[0] as any).payload).toBe('not-json');
  });

  it('get() emits undefined payload when data is empty', () => {
    const svc = makeService();
    const stream = svc.openSharedEventStream(defaultSettings());
    const received: unknown[] = [];

    stream.get<{ type: 'EV' }>('EV').subscribe((v) => received.push(v));
    MockEventSource.lastInstance.dispatchNoData('EV');

    expect(received).toHaveLength(1);
    expect((received[0] as any).payload).toBeUndefined();
  });

  it('get() accepts an event object (uses .type property)', () => {
    const svc = makeService();
    const stream = svc.openSharedEventStream(defaultSettings());
    const received: unknown[] = [];

    stream.get<{ type: 'EV' }>({ type: 'EV' }).subscribe((v) => received.push(v));
    MockEventSource.lastInstance.dispatchNamed('EV', { x: 1 });

    expect(received).toHaveLength(1);
  });

  it('warns when addEventListener throws', () => {
    const svc = makeService();
    const stream = svc.openSharedEventStream(defaultSettings());
    const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    MockEventSource.lastInstance.addEventListener = vi.fn(() => {
      throw new Error('add failed');
    });

    stream.get<{ type: 'BROKEN' }>('BROKEN').subscribe();

    expect(warnSpy).toHaveBeenCalledWith('Shared SSE: could not add listener for', 'BROKEN', expect.any(Error));
    warnSpy.mockRestore();
  });

  it('routes listener processing exceptions to the observable error channel', () => {
    const svc = makeService();
    let runCount = 0;
    (svc as any).ngZone = {
      run: (fn: () => void) => {
        runCount += 1;
        if (runCount === 1) {
          throw new Error('zone boom');
        }
        return fn();
      },
    };

    const stream = svc.openSharedEventStream(defaultSettings());
    const errorSpy = vi.fn();
    stream.get<{ type: 'EV' }>('EV').subscribe({ error: errorSpy });

    MockEventSource.lastInstance.dispatchNamed('EV', { x: 1 });

    expect(errorSpy).toHaveBeenCalledWith(expect.any(Error));
  });

  it('close() closes the EventSource and completes subjects', () => {
    const svc = makeService();
    const stream = svc.openSharedEventStream(defaultSettings());
    let completed = false;

    stream.get<{ type: 'EV' }>('EV').subscribe({ complete: () => (completed = true) });
    stream.close();

    expect(MockEventSource.lastInstance.closed).toBe(true);
    expect(completed).toBe(true);
  });

  it('close() removes all named listeners', () => {
    const svc = makeService();
    const stream = svc.openSharedEventStream(defaultSettings());
    const removeListenerSpy = vi.spyOn(MockEventSource.lastInstance, 'removeEventListener');

    stream.get<{ type: 'EV' }>('EV').subscribe();
    stream.close();

    expect(removeListenerSpy).toHaveBeenCalledWith('EV', expect.any(Function));
  });

  it('close() logs when teardown fails in the outer catch', () => {
    const svc = makeService();
    const stream = svc.openSharedEventStream(defaultSettings());
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    const objectKeysSpy = vi.spyOn(Object, 'keys').mockImplementationOnce(() => {
      throw new Error('keys failed');
    });

    stream.close();

    expect(errorSpy).toHaveBeenCalledWith('Error tearing down shared SSE', expect.any(Error));
    objectKeysSpy.mockRestore();
    errorSpy.mockRestore();
  });
});





