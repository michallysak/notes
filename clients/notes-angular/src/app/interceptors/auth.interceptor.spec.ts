import { HttpResponse, HttpRequest } from '@angular/common/http';
import { of } from 'rxjs';
import { AuthInterceptor } from './auth.interceptor';

describe('AuthInterceptor', () => {
  it('adds Authorization header when token is available', () => {
    const authService = { getToken: vi.fn().mockReturnValue('jwt') } as any;
    const interceptor = new AuthInterceptor(authService);
    const req = new HttpRequest('GET', '/notes');
    const handle = vi.fn().mockReturnValue(of(new HttpResponse({ status: 200 })));

    interceptor.intercept(req, { handle } as any).subscribe();

    const passedRequest = handle.mock.calls[0][0] as HttpRequest<unknown>;
    expect(passedRequest.headers.get('Authorization')).toBe('Bearer jwt');
  });

  it('passes original request when token is missing', () => {
    const authService = { getToken: vi.fn().mockReturnValue(undefined) } as any;
    const interceptor = new AuthInterceptor(authService);
    const req = new HttpRequest('GET', '/notes');
    const handle = vi.fn().mockReturnValue(of(new HttpResponse({ status: 200 })));

    interceptor.intercept(req, { handle } as any).subscribe();

    expect(handle).toHaveBeenCalledWith(req);
  });

  it('passes original request when token retrieval throws', () => {
    const authService = { getToken: vi.fn(() => { throw new Error('boom'); }) } as any;
    const interceptor = new AuthInterceptor(authService);
    const req = new HttpRequest('GET', '/notes');
    const handle = vi.fn().mockReturnValue(of(new HttpResponse({ status: 200 })));

    interceptor.intercept(req, { handle } as any).subscribe();

    expect(handle).toHaveBeenCalledWith(req);
  });
});

