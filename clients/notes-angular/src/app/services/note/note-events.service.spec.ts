import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { Subject, of } from 'rxjs';
import { NoteEventsService } from './note-events.service';
import { AuthService } from '../auth/auth.service';
import { SseService } from '../sse/sse.service';
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
} from '@notes/notes_service';

const makeStream = () => ({
  get: vi.fn().mockReturnValue(new Subject().asObservable()),
  close: vi.fn(),
});

const configureTest = ({ key = 'k' }: { key?: string } = {}) => {
  const authSubj = new Subject<boolean>();
  const stream = makeStream();
  const mockSse = { openSharedEventStream: vi.fn().mockReturnValue(stream) } as unknown as SseService;
  const mockAuth = { logged$: authSubj.asObservable() } as unknown as AuthService;
  const mockNoteSse = { createStreamKey: vi.fn().mockReturnValue(of({ key })) } as unknown as NoteSseResourceService;

  TestBed.configureTestingModule({
    providers: [{ provide: BASE_PATH, useValue: 'http://localhost:8080' }],
  });

  let svc!: NoteEventsService;
  TestBed.runInInjectionContext(() => {
    svc = new NoteEventsService(mockSse, mockAuth, mockNoteSse);
  });

  return { svc, stream, authSubj, mockSse: mockSse as any, mockNoteSse: mockNoteSse as any };
};

describe('NoteEventsService', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => {
    vi.useRealTimers();
    TestBed.resetTestingModule();
  });

  it('does not open a stream when not logged in', () => {
    const { mockSse, authSubj } = configureTest();
    authSubj.next(false);
    vi.runAllTimers();
    expect(mockSse.openSharedEventStream).not.toHaveBeenCalled();
  });

  it('opens a stream when logged in', () => {
    const { mockSse, authSubj } = configureTest();
    authSubj.next(true);
    vi.runAllTimers();
    expect(mockSse.openSharedEventStream).toHaveBeenCalledOnce();
  });

  it('opens a stream when logged-in state is preceded by a logged-out emission (regression: reused closed connectSub)', () => {
    const authSubj = new Subject<boolean>();
    const keySubj = new Subject<{ key: string }>();
    const stream = makeStream();
    const mockSse = {
      openSharedEventStream: vi.fn().mockReturnValue(stream),
    } as unknown as SseService;
    const mockAuth = { logged$: authSubj.asObservable() } as unknown as AuthService;
    const mockNoteSse = {
      createStreamKey: vi.fn().mockReturnValue(keySubj.asObservable()),
    } as unknown as NoteSseResourceService;

    TestBed.configureTestingModule({
      providers: [{ provide: BASE_PATH, useValue: 'http://localhost:8080' }],
    });
    TestBed.runInInjectionContext(() => new NoteEventsService(mockSse, mockAuth, mockNoteSse));

    // Real AuthService.logged$ is backed by a BehaviorSubject(null): it emits false
    // (unauthenticated) before the session is restored and it emits true.
    authSubj.next(false);
    authSubj.next(true);
    // The stream key resolves asynchronously, as a real HTTP response would.
    keySubj.next({ key: 'k' });
    vi.runAllTimers();

    expect(mockSse.openSharedEventStream).toHaveBeenCalledOnce();
  });

  it('passes correct settings to openSharedEventStream', () => {
    const { mockSse, authSubj } = configureTest({ key: 'my-key' });
    const logSpy = vi.spyOn(console, 'log').mockImplementation(() => undefined);
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    authSubj.next(true);
    vi.runAllTimers();
    const call = mockSse.openSharedEventStream.mock.calls[0][0];
    expect(call.path).toBe('/notes/events');
    expect(call.key).toBe('my-key');
    expect(call.baseUrl).toBe('http://localhost:8080');
    call.onOpen?.(new Event('open'));
    call.onError?.(new Event('error'));
    expect(logSpy).toHaveBeenCalledWith('SSE open');
    expect(errorSpy).toHaveBeenCalledWith('SSE error', expect.any(Event));
    errorSpy.mockRestore();
  });

  it('calls createStreamKey with all note event types', () => {
    const { mockNoteSse, authSubj } = configureTest();
    authSubj.next(true);
    vi.runAllTimers();
    expect(mockNoteSse.createStreamKey).toHaveBeenCalledWith([
      NoteCreatedEventDTO.TypeEnum.NOTECREATEDEVENT,
      NoteUpdatedEventDTO.TypeEnum.NOTEUPDATEDEVENT,
      NoteDeletedEventDTO.TypeEnum.NOTEDELETEDEVENT,
      NotePermissionsSetEventDTO.TypeEnum.NOTEPERMISSIONSSETEVENT,
      NoteAccessRemovedEventDTO.TypeEnum.NOTEACCESSREMOVEDEVENT,
      NotePublicShareUpsertedEventDTO.TypeEnum.NOTEPUBLICSHAREUPSERTEDEVENT,
      NotePublicShareRemovedEventDTO.TypeEnum.NOTEPUBLICSHAREREMOVEDEVENT,
    ]);
  });

  it('forwards events from stream.get() to domainEvents$', () => {
    const eventSubj = new Subject<NoteCreatedEventDTO>();
    const authSubj = new Subject<boolean>();
    const stream = {
      get: vi.fn((eventType: string) => {
        if (eventType === NoteCreatedEventDTO.TypeEnum.NOTECREATEDEVENT) {
          return eventSubj.asObservable();
        }
        return new Subject().asObservable();
      }),
      close: vi.fn(),
    };
    const mockSse = { openSharedEventStream: vi.fn().mockReturnValue(stream) } as unknown as SseService;
    const mockAuth = { logged$: authSubj.asObservable() } as unknown as AuthService;
    const mockNoteSse = { createStreamKey: vi.fn().mockReturnValue(of({ key: 'k' })) } as unknown as NoteSseResourceService;

    TestBed.configureTestingModule({ providers: [{ provide: BASE_PATH, useValue: 'http://localhost:8080' }] });

    let svc!: NoteEventsService;
    TestBed.runInInjectionContext(() => { svc = new NoteEventsService(mockSse, mockAuth, mockNoteSse); });

    const received: unknown[] = [];
    svc.domainEvents$.subscribe((v: any) => received.push(v));

    authSubj.next(true);
    vi.runAllTimers();

    const event: NoteCreatedEventDTO = {
      id: 'id',
      type: NoteCreatedEventDTO.TypeEnum.NOTECREATEDEVENT,
      payload: { id: '1', title: 'T', content: 'C', pinned: false } as any,
    };
    eventSubj.next(event);

    expect(received).toHaveLength(1);
    expect(received[0]).toEqual(event);
  });

  it('forwards deleted events from stream.get() to domainEvents$', () => {
    const deletedSubj = new Subject<any>();
    const authSubj = new Subject<boolean>();
    const stream = {
      get: vi.fn((eventType: string) => {
        if (eventType === NoteDeletedEventDTO.TypeEnum.NOTEDELETEDEVENT) {
          return deletedSubj.asObservable();
        }
        return new Subject().asObservable();
      }),
      close: vi.fn(),
    };
    const mockSse = { openSharedEventStream: vi.fn().mockReturnValue(stream) } as unknown as SseService;
    const mockAuth = { logged$: authSubj.asObservable() } as unknown as AuthService;
    const mockNoteSse = { createStreamKey: vi.fn().mockReturnValue(of({ key: 'k' })) } as unknown as NoteSseResourceService;

    TestBed.configureTestingModule({ providers: [{ provide: BASE_PATH, useValue: 'http://localhost:8080' }] });

    let svc!: NoteEventsService;
    TestBed.runInInjectionContext(() => { svc = new NoteEventsService(mockSse, mockAuth, mockNoteSse); });

    const received: unknown[] = [];
    svc.domainEvents$.subscribe((v: any) => received.push(v));

    authSubj.next(true);
    vi.runAllTimers();

    const event = { id: 'id', type: NoteDeletedEventDTO.TypeEnum.NOTEDELETEDEVENT, payload: { id: '1' } };
    deletedSubj.next(event);

    expect(received).toHaveLength(1);
    expect(received[0]).toEqual(event);
  });

  it('forwards public-share upserted events from stream.get() to domainEvents$', () => {
    const upsertedSubj = new Subject<any>();
    const authSubj = new Subject<boolean>();
    const stream = {
      get: vi.fn((eventType: string) => {
        if (eventType === NotePublicShareUpsertedEventDTO.TypeEnum.NOTEPUBLICSHAREUPSERTEDEVENT) {
          return upsertedSubj.asObservable();
        }
        return new Subject().asObservable();
      }),
      close: vi.fn(),
    };
    const mockSse = { openSharedEventStream: vi.fn().mockReturnValue(stream) } as unknown as SseService;
    const mockAuth = { logged$: authSubj.asObservable() } as unknown as AuthService;
    const mockNoteSse = { createStreamKey: vi.fn().mockReturnValue(of({ key: 'k' })) } as unknown as NoteSseResourceService;

    TestBed.configureTestingModule({ providers: [{ provide: BASE_PATH, useValue: 'http://localhost:8080' }] });

    let svc!: NoteEventsService;
    TestBed.runInInjectionContext(() => { svc = new NoteEventsService(mockSse, mockAuth, mockNoteSse); });

    const received: unknown[] = [];
    svc.domainEvents$.subscribe((v: any) => received.push(v));

    authSubj.next(true);
    vi.runAllTimers();

    const event = {
      id: 'id',
      type: NotePublicShareUpsertedEventDTO.TypeEnum.NOTEPUBLICSHAREUPSERTEDEVENT,
      payload: { id: '1', publicShare: { publicShareId: 'share-1', permissions: ['READ'] } },
    };
    upsertedSubj.next(event);

    expect(received).toHaveLength(1);
    expect(received[0]).toEqual(event);
  });

  it('forwards public-share removed events from stream.get() to domainEvents$', () => {
    const removedSubj = new Subject<any>();
    const authSubj = new Subject<boolean>();
    const stream = {
      get: vi.fn((eventType: string) => {
        if (eventType === NotePublicShareRemovedEventDTO.TypeEnum.NOTEPUBLICSHAREREMOVEDEVENT) {
          return removedSubj.asObservable();
        }
        return new Subject().asObservable();
      }),
      close: vi.fn(),
    };
    const mockSse = { openSharedEventStream: vi.fn().mockReturnValue(stream) } as unknown as SseService;
    const mockAuth = { logged$: authSubj.asObservable() } as unknown as AuthService;
    const mockNoteSse = { createStreamKey: vi.fn().mockReturnValue(of({ key: 'k' })) } as unknown as NoteSseResourceService;

    TestBed.configureTestingModule({ providers: [{ provide: BASE_PATH, useValue: 'http://localhost:8080' }] });

    let svc!: NoteEventsService;
    TestBed.runInInjectionContext(() => { svc = new NoteEventsService(mockSse, mockAuth, mockNoteSse); });

    const received: unknown[] = [];
    svc.domainEvents$.subscribe((v: any) => received.push(v));

    authSubj.next(true);
    vi.runAllTimers();

    const event = {
      id: 'id',
      type: NotePublicShareRemovedEventDTO.TypeEnum.NOTEPUBLICSHAREREMOVEDEVENT,
      payload: { noteId: '1', publicShareId: 'share-1' },
    };
    removedSubj.next(event);

    expect(received).toHaveLength(1);
    expect(received[0]).toEqual(event);
  });

  it('closes stream and disconnects when logged out', () => {
    const { stream, authSubj } = configureTest();
    authSubj.next(true);
    vi.runAllTimers();
    authSubj.next(false);
    vi.runAllTimers();
    expect(stream.close).toHaveBeenCalledOnce();
  });

  it('propagates stream errors to domainEvents$ subscribers', () => {
    const eventSubj = new Subject<NoteCreatedEventDTO>();
    const authSubj = new Subject<boolean>();
    const stream = { get: vi.fn().mockReturnValue(eventSubj.asObservable()), close: vi.fn() };
    const mockSse = { openSharedEventStream: vi.fn().mockReturnValue(stream) } as unknown as SseService;
    const mockAuth = { logged$: authSubj.asObservable() } as unknown as AuthService;
    const mockNoteSse = { createStreamKey: vi.fn().mockReturnValue(of({ key: 'k' })) } as unknown as NoteSseResourceService;

    TestBed.configureTestingModule({ providers: [{ provide: BASE_PATH, useValue: 'http://localhost:8080' }] });

    let svc!: NoteEventsService;
    TestBed.runInInjectionContext(() => {
      svc = new NoteEventsService(mockSse, mockAuth, mockNoteSse);
    });

    const errorSpy = vi.fn();
    svc.domainEvents$.subscribe({ error: errorSpy });

    authSubj.next(true);
    vi.runAllTimers();
    eventSubj.error(new Error('boom'));

    expect(errorSpy).toHaveBeenCalled();
  });

  it('propagates stream completion to domainEvents$ subscribers', () => {
    const eventSubj = new Subject<NoteCreatedEventDTO>();
    const authSubj = new Subject<boolean>();
    const stream = { get: vi.fn().mockReturnValue(eventSubj.asObservable()), close: vi.fn() };
    const mockSse = { openSharedEventStream: vi.fn().mockReturnValue(stream) } as unknown as SseService;
    const mockAuth = { logged$: authSubj.asObservable() } as unknown as AuthService;
    const mockNoteSse = { createStreamKey: vi.fn().mockReturnValue(of({ key: 'k' })) } as unknown as NoteSseResourceService;

    TestBed.configureTestingModule({ providers: [{ provide: BASE_PATH, useValue: 'http://localhost:8080' }] });

    let svc!: NoteEventsService;
    TestBed.runInInjectionContext(() => {
      svc = new NoteEventsService(mockSse, mockAuth, mockNoteSse);
    });

    const completeSpy = vi.fn();
    svc.domainEvents$.subscribe({ complete: completeSpy });

    authSubj.next(true);
    vi.runAllTimers();
    eventSubj.complete();

    expect(completeSpy).toHaveBeenCalledOnce();
  });

  it('disconnect returns early when connectSub is missing', () => {
    const { svc } = configureTest();
    (svc as any).connectSub = undefined;

    expect(() => (svc as any).disconnect()).not.toThrow();
  });

  it('disconnect tolerates unsubscribe and close errors', () => {
    const { svc } = configureTest();
    (svc as any).connectSub = { unsubscribe: vi.fn(() => { throw new Error('unsubscribe'); }) };
    (svc as any).stream = { close: vi.fn(() => { throw new Error('close'); }) };

    expect(() => (svc as any).disconnect()).not.toThrow();
    expect((svc as any).stream).toBeUndefined();
  });

  it('does not open stream when createStreamKey returns no key', () => {
    const authSubj = new Subject<boolean>();
    const mockSse = { openSharedEventStream: vi.fn() } as unknown as SseService;
    const mockAuth = { logged$: authSubj.asObservable() } as unknown as AuthService;
    const mockNoteSse = { createStreamKey: vi.fn().mockReturnValue(of({ key: undefined })) } as unknown as NoteSseResourceService;

    TestBed.configureTestingModule({ providers: [{ provide: BASE_PATH, useValue: 'http://localhost:8080' }] });

    let svc!: NoteEventsService;
    TestBed.runInInjectionContext(() => { svc = new NoteEventsService(mockSse, mockAuth, mockNoteSse); });

    authSubj.next(true);
    vi.runAllTimers();

    expect((mockSse as any).openSharedEventStream).not.toHaveBeenCalled();
  });

  it('ngOnDestroy closes stream and unsubscribes', () => {
    const { svc, stream, authSubj } = configureTest();
    authSubj.next(true);
    vi.runAllTimers();
    svc.ngOnDestroy();
    expect(stream.close).toHaveBeenCalledOnce();
  });
});

