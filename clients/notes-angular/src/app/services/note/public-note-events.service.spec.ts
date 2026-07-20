import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { Subject, of } from 'rxjs';
import { PublicNoteEventsService } from './public-note-events.service';
import { SseService } from '../sse/sse.service';
import {
  BASE_PATH,
  NotePublicShareRemovedEventDTO,
  NotePublicShareUpsertedEventDTO,
  NoteUpdatedEventDTO,
  PublicNoteSseResourceService,
} from '@notes/notes_service';

const futureIso = (msFromNow: number) => new Date(Date.now() + msFromNow).toISOString();

const makeStream = (subjects: Record<string, Subject<any>> = {}) => ({
  get: vi.fn((eventType: string) => {
    if (!subjects[eventType]) {
      subjects[eventType] = new Subject<any>();
    }
    return subjects[eventType].asObservable();
  }),
  close: vi.fn(),
});

const configure = ({
  key = 'k',
  expiresAt = futureIso(15 * 60_000),
  stream = makeStream(),
}: { key?: string; expiresAt?: string; stream?: ReturnType<typeof makeStream> } = {}) => {
  const mockSse = {
    openSharedEventStream: vi.fn().mockReturnValue(stream),
  } as unknown as SseService;
  const mockPublicSse = {
    createPublicNoteStreamKey: vi.fn().mockReturnValue(of({ key, expiresAt })),
  } as unknown as PublicNoteSseResourceService;

  TestBed.configureTestingModule({
    providers: [{ provide: BASE_PATH, useValue: 'http://localhost:8080' }],
  });

  let svc!: PublicNoteEventsService;
  TestBed.runInInjectionContext(() => {
    svc = new PublicNoteEventsService(mockSse, mockPublicSse);
  });

  return { svc, stream, mockSse: mockSse as any, mockPublicSse: mockPublicSse as any };
};

describe('PublicNoteEventsService', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => {
    vi.useRealTimers();
    TestBed.resetTestingModule();
  });

  it('creates a public stream key for the public share id and opens the stream', () => {
    const { svc, mockSse, mockPublicSse } = configure();

    const sub = svc.connect('share-1').subscribe();

    expect(mockPublicSse.createPublicNoteStreamKey).toHaveBeenCalledWith('share-1');
    expect(mockSse.openSharedEventStream).toHaveBeenCalledOnce();
    const call = mockSse.openSharedEventStream.mock.calls[0][0];
    expect(call.path).toBe('/notes/events');
    expect(call.key).toBe('k');
    expect(call.baseUrl).toBe('http://localhost:8080');
    sub.unsubscribe();
  });

  it('forwards public-share upserted, updated, and removed events', () => {
    const subjects: Record<string, Subject<any>> = {};
    const stream = makeStream(subjects);
    const { svc } = configure({ stream });

    const received: unknown[] = [];
    const sub = svc.connect('share-1').subscribe((e) => received.push(e));

    const upserted = {
      id: 'id',
      type: NotePublicShareUpsertedEventDTO.TypeEnum.NOTEPUBLICSHAREUPSERTEDEVENT,
      payload: { id: '1', publicShare: { publicShareId: 'share-1', permissions: ['EDIT'] } },
    };
    subjects[NotePublicShareUpsertedEventDTO.TypeEnum.NOTEPUBLICSHAREUPSERTEDEVENT].next(upserted);

    const updated = {
      id: 'id-update',
      type: NoteUpdatedEventDTO.TypeEnum.NOTEUPDATEDEVENT,
      payload: { id: '1', title: 'Updated', publicShare: { publicShareId: 'share-1' } },
    };
    subjects[NoteUpdatedEventDTO.TypeEnum.NOTEUPDATEDEVENT].next(updated);

    const removed = {
      id: 'id2',
      type: NotePublicShareRemovedEventDTO.TypeEnum.NOTEPUBLICSHAREREMOVEDEVENT,
      payload: { noteId: '1', publicShareId: 'share-1' },
    };
    subjects[NotePublicShareRemovedEventDTO.TypeEnum.NOTEPUBLICSHAREREMOVEDEVENT].next(removed);

    expect(received).toEqual([upserted, updated, removed]);
    sub.unsubscribe();
  });

  it('refreshes the key and reopens the stream before expiry', () => {
    const { svc, mockSse, mockPublicSse } = configure({ expiresAt: futureIso(60_000) });

    const sub = svc.connect('share-1').subscribe();

    expect(mockPublicSse.createPublicNoteStreamKey).toHaveBeenCalledTimes(1);

    // Refresh scheduled at (expiry - 30s margin) ~= 30s from now.
    vi.advanceTimersByTime(31_000);

    expect(mockPublicSse.createPublicNoteStreamKey).toHaveBeenCalledTimes(2);
    expect(mockSse.openSharedEventStream).toHaveBeenCalledTimes(2);
    sub.unsubscribe();
  });

  it('retries when the key request fails', () => {
    const stream = makeStream();
    const mockSse = {
      openSharedEventStream: vi.fn().mockReturnValue(stream),
    } as unknown as SseService;
    const errorSubject = new Subject<any>();
    const createKey = vi
      .fn()
      .mockReturnValueOnce(errorSubject.asObservable())
      .mockReturnValue(of({ key: 'k2', expiresAt: futureIso(15 * 60_000) }));
    const mockPublicSse = {
      createPublicNoteStreamKey: createKey,
    } as unknown as PublicNoteSseResourceService;

    TestBed.configureTestingModule({
      providers: [{ provide: BASE_PATH, useValue: 'http://localhost:8080' }],
    });
    let svc!: PublicNoteEventsService;
    TestBed.runInInjectionContext(() => {
      svc = new PublicNoteEventsService(mockSse, mockPublicSse);
    });

    const sub = svc.connect('share-1').subscribe();
    errorSubject.error(new Error('boom'));

    expect(createKey).toHaveBeenCalledTimes(1);
    vi.advanceTimersByTime(5_000);
    expect(createKey).toHaveBeenCalledTimes(2);
    expect((mockSse as any).openSharedEventStream).toHaveBeenCalledOnce();
    sub.unsubscribe();
  });

  it('closes the stream and cancels timers on unsubscribe', () => {
    const { svc, stream } = configure({ expiresAt: futureIso(60_000) });

    const sub = svc.connect('share-1').subscribe();
    sub.unsubscribe();

    expect(stream.close).toHaveBeenCalled();

    // No refresh should fire after teardown.
    const streamCloseCallsBefore = stream.close.mock.calls.length;
    vi.advanceTimersByTime(120_000);
    expect(stream.close.mock.calls.length).toBe(streamCloseCallsBefore);
  });

  it('does not open a stream when the key is missing', () => {
    const { svc, mockSse } = configure({ key: '' });

    const sub = svc.connect('share-1').subscribe();

    expect(mockSse.openSharedEventStream).not.toHaveBeenCalled();
    sub.unsubscribe();
  });
});
