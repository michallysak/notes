import { AuthGuard } from './auth.guard';

describe('AuthGuard', () => {
  it('allows route activation when authenticated', () => {
    const authService = { isAuthenticated: vi.fn().mockReturnValue(true) } as any;
    const router = { navigate: vi.fn() } as any;
    const guard = new AuthGuard(authService, router);

    const allowed = guard.canActivate();

    expect(allowed).toBe(true);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('redirects to login when unauthenticated', () => {
    const authService = { isAuthenticated: vi.fn().mockReturnValue(false) } as any;
    const router = { navigate: vi.fn() } as any;
    const guard = new AuthGuard(authService, router);

    const allowed = guard.canActivate();

    expect(allowed).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});

