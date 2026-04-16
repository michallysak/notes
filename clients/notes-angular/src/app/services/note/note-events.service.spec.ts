import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { Subject, of } from 'rxjs';
import { NoteEventsService } from './note-events.service';
import { AuthService } from '../auth/auth.service';
import { SseService } from '../sse/sse.service';
import { BASE_PATH, NoteCreatedEventDTO, NoteSseResourceService } from '@notes/notes_service';

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

  it('passes correct settings to openSharedEventStream', () => {
    const { mockSse, authSubj } = configureTest({ key: 'my-key' });
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    authSubj.next(true);
    vi.runAllTimers();
    const call = mockSse.openSharedEventStream.mock.calls[0][0];
    expect(call.path).toBe('/notes/events');
    expect(call.key).toBe('my-key');
    expect(call.baseUrl).toBe('http://localhost:8080');
    call.onOpen?.(new Event('open'));
    call.onError?.(new Event('error'));
    expect(errorSpy).toHaveBeenCalledWith('SSE open', expect.any(Event));
    expect(errorSpy).toHaveBeenCalledWith('SSE error', expect.any(Event));
    errorSpy.mockRestore();
  });

  it('calls createStreamKey with the NOTE_CREATED event type', () => {
    const { mockNoteSse, authSubj } = configureTest();
    authSubj.next(true);
    vi.runAllTimers();
    expect(mockNoteSse.createStreamKey).toHaveBeenCalledWith([NoteCreatedEventDTO.TypeEnum.NOTECREATEDEVENT]);
  });

  it('forwards events from stream.get() to noteEvents$', () => {
    const eventSubj = new Subject<NoteCreatedEventDTO>();
    const authSubj = new Subject<boolean>();
    const stream = { get: vi.fn().mockReturnValue(eventSubj.asObservable()), close: vi.fn() };
    const mockSse = { openSharedEventStream: vi.fn().mockReturnValue(stream) } as unknown as SseService;
    const mockAuth = { logged$: authSubj.asObservable() } as unknown as AuthService;
    const mockNoteSse = { createStreamKey: vi.fn().mockReturnValue(of({ key: 'k' })) } as unknown as NoteSseResourceService;

    TestBed.configureTestingModule({ providers: [{ provide: BASE_PATH, useValue: 'http://localhost:8080' }] });

    let svc!: NoteEventsService;
    TestBed.runInInjectionContext(() => { svc = new NoteEventsService(mockSse, mockAuth, mockNoteSse); });

    const received: unknown[] = [];
    svc.noteEvents$.subscribe((v) => received.push(v));

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

  it('closes stream and disconnects when logged out', () => {
    const { stream, authSubj } = configureTest();
    authSubj.next(true);
    vi.runAllTimers();
    authSubj.next(false);
    vi.runAllTimers();
    expect(stream.close).toHaveBeenCalledOnce();
  });

  it('propagates stream errors to noteEvents$ subscribers', () => {
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
    svc.noteEvents$.subscribe({ error: errorSpy });

    authSubj.next(true);
    vi.runAllTimers();
    eventSubj.error(new Error('boom'));

    expect(errorSpy).toHaveBeenCalled();
  });

  it('propagates stream completion to noteEvents$ subscribers', () => {
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
    svc.noteEvents$.subscribe({ complete: completeSpy });

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

