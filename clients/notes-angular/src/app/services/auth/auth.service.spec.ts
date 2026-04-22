import { firstValueFrom, of, throwError } from 'rxjs';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  const usersApi = {
    getCurrentUser: vi.fn(),
    loginUser: vi.fn(),
    registerUser: vi.fn(),
  };

  const tokenService = {
    getItem: vi.fn(),
    setItem: vi.fn(),
    revoke: vi.fn(),
  };

  const createService = () => new AuthService(usersApi as any, tokenService as any);

  beforeEach(() => {
    usersApi.getCurrentUser.mockReset();
    usersApi.loginUser.mockReset();
    usersApi.registerUser.mockReset();
    tokenService.getItem.mockReset();
    tokenService.setItem.mockReset();
    tokenService.revoke.mockReset();
  });

  it('returns token from token service', () => {
    tokenService.getItem.mockReturnValue('jwt-token');
    const service = createService();

    const token = service.getToken();

    expect(token).toBe('jwt-token');
    expect(tokenService.getItem).toHaveBeenCalled();
  });

  it('reports authentication status based on token', () => {
    tokenService.getItem.mockReturnValue('jwt-token');
    const service = createService();

    expect(service.isAuthenticated()).toBe(true);

    tokenService.getItem.mockReturnValue(undefined);
    expect(service.isAuthenticated()).toBe(false);
  });

  it('does not initialize user when token is missing', async () => {
    tokenService.getItem.mockReturnValue(undefined);
    const service = createService();

    await service.init();

    expect(usersApi.getCurrentUser).not.toHaveBeenCalled();
  });

  it('loads current user on init when token exists', async () => {
    tokenService.getItem.mockReturnValue('jwt-token');
    const user = { id: '1', email: 'user@example.com' };
    usersApi.getCurrentUser.mockReturnValue(of(user));
    const service = createService();
    let latestUser: unknown = undefined;
    service.currentUser$.subscribe((value) => {
      latestUser = value;
    });

    await service.init();

    expect(usersApi.getCurrentUser).toHaveBeenCalled();
    expect(latestUser).toEqual(user);
  });

  it('logs out when user loading fails during init', async () => {
    tokenService.getItem.mockReturnValue('jwt-token');
    usersApi.getCurrentUser.mockReturnValue(throwError(() => new Error('boom')));
    const service = createService();

    await service.init();

    expect(tokenService.revoke).toHaveBeenCalled();
  });

  it('revokes token and clears current user on logout', () => {
    const service = createService();
    service['currentUserSubject'].next({ id: '1', email: 'user@example.com' });

    service.logout();

    expect(tokenService.revoke).toHaveBeenCalled();
    expect(service['currentUserSubject'].value).toBeNull();
  });

  describe('Authentication Flow (shared login/register logic)', () => {
    const testAuthenticationFlow = (authMethod: 'login' | 'register') => {
      const request =
        authMethod === 'login'
          ? { email: 'user@example.com', password: 'secret' }
          : { email: 'user@example.com', password: 'secret' };
      const tokenResponse = { token: 'jwt-token' };
      const user = { id: '1', email: 'user@example.com' };
      const mockApi = authMethod === 'login' ? usersApi.loginUser : usersApi.registerUser;

      mockApi.mockReturnValue(of(tokenResponse));
      usersApi.getCurrentUser.mockReturnValue(of(user));
      const service = createService();

      return { request, user, service };
    };

    it('stores token and user on successful login', async () => {
      const { request, user, service } = testAuthenticationFlow('login');

      const result = await firstValueFrom(service.login(request));

      expect(usersApi.loginUser).toHaveBeenCalledWith(request);
      expect(tokenService.setItem).toHaveBeenCalledWith('jwt-token');
      expect(usersApi.getCurrentUser).toHaveBeenCalled();
      expect(result).toEqual(user);
    });

    it('stores token and user on successful register', async () => {
      const { request, user, service } = testAuthenticationFlow('register');

      const result = await firstValueFrom(service.register(request));

      expect(usersApi.registerUser).toHaveBeenCalledWith(request);
      expect(tokenService.setItem).toHaveBeenCalledWith('jwt-token');
      expect(usersApi.getCurrentUser).toHaveBeenCalled();
      expect(result).toEqual(user);
    });

    it('updates current user subject during authentication', async () => {
      const { request, user, service } = testAuthenticationFlow('login');
      let latestUser: unknown = undefined;
      service.currentUser$.subscribe((value) => {
        latestUser = value;
      });

      await firstValueFrom(service.login(request));

      expect(latestUser).toEqual(user);
    });

    it('emits logged state transitions during authentication', async () => {
      const { request, service } = testAuthenticationFlow('login');
      const states: boolean[] = [];
      service.logged$.subscribe((value) => {
        states.push(value);
      });

      await firstValueFrom(service.login(request));

      expect(states).toContain(true);
    });
  });
});


