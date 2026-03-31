import { AuthTokenService } from './auth-token.service';

describe('AuthTokenService', () => {
  const originalLocalStorage = globalThis.localStorage;
  let mockStorage: {
    getItem: ReturnType<typeof vi.fn>;
    setItem: ReturnType<typeof vi.fn>;
    removeItem: ReturnType<typeof vi.fn>;
  };
  const authToken = 'authToken';
  let abc = 'abc';

  beforeEach(() => {
    mockStorage = {
      getItem: vi.fn().mockReturnValue(abc),
      setItem: vi.fn(),
      removeItem: vi.fn(),
    };
    Object.defineProperty(globalThis, 'localStorage', {
      value: mockStorage,
      configurable: true,
    });
  });

  afterEach(() => {
    Object.defineProperty(globalThis, 'localStorage', {
      value: originalLocalStorage,
      configurable: true,
    });
  });

  it('gets token from localStorage', () => {
    const service = new AuthTokenService();

    const result = service.getItem();

    expect(mockStorage.getItem).toHaveBeenCalledWith(authToken);
    expect(result).toBe(abc);
  });

  it('sets token when value is present', () => {
    const service = new AuthTokenService();

    service.setItem('new-token');

    expect(mockStorage.setItem).toHaveBeenCalledWith(authToken, 'new-token');
  });

  it('revokes token when value is missing', () => {
    const service = new AuthTokenService();

    service.setItem(undefined);

    expect(mockStorage.removeItem).toHaveBeenCalledWith(authToken);
  });
});
