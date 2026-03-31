import { HttpErrorResponse, HttpRequest } from '@angular/common/http';
import { throwError } from 'rxjs';
import { ErrorInterceptor } from './error.interceptor';

describe('ErrorInterceptor', () => {
  it('logs out and redirects on 401 errors', () => {
    const authService = { logout: vi.fn() } as any;
    const router = { navigate: vi.fn() } as any;
    const interceptor = new ErrorInterceptor(authService, router);
    const req = new HttpRequest('GET', '/notes');
    const handle = vi
      .fn()
      .mockReturnValue(throwError(() => new HttpErrorResponse({ status: 401 })));
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    interceptor.intercept(req, { handle } as any).subscribe({ error: () => undefined });

    expect(authService.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
    expect(errorSpy).toHaveBeenCalled();
  });

  it('does not logout on non-401 errors', () => {
    const authService = { logout: vi.fn() } as any;
    const router = { navigate: vi.fn() } as any;
    const interceptor = new ErrorInterceptor(authService, router);
    const req = new HttpRequest('GET', '/notes');
    const handle = vi
      .fn()
      .mockReturnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    interceptor.intercept(req, { handle } as any).subscribe({ error: () => undefined });

    expect(authService.logout).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
    expect(errorSpy).toHaveBeenCalled();
  });
});
