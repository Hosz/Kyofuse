import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { of } from 'rxjs';
import { guestGuard } from './guest-guard';
import { AuthService } from '../services/auth/auth.service';

describe('guestGuard', () => {
  let authServiceSpy: { checkSession: ReturnType<typeof vi.fn> };
  let router: Router;

  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => guestGuard(...guardParameters));

  beforeEach(() => {
    authServiceSpy = {
      checkSession: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
      ],
    });

    router = TestBed.inject(Router);
  });

  it('should allow access when addAccount query param is true, even if authenticated', () => {
    const route = {
      queryParams: { addAccount: 'true' },
    } as unknown as ActivatedRouteSnapshot;
    const state = {} as RouterStateSnapshot;

    const result = executeGuard(route, state);
    expect(result).toBe(true);
    expect(authServiceSpy.checkSession).not.toHaveBeenCalled();
  });

  it('should redirect to /home when user is already authenticated and addAccount is not set', async () => {
    authServiceSpy.checkSession.mockReturnValue(of(true));

    const route = {
      queryParams: {},
    } as unknown as ActivatedRouteSnapshot;
    const state = {} as RouterStateSnapshot;

    const result$ = executeGuard(route, state);
    if (typeof result$ === 'object' && 'subscribe' in result$) {
      await new Promise<void>((resolve) => {
        result$.subscribe((result) => {
          expect(result instanceof UrlTree).toBe(true);
          expect(router.serializeUrl(result as UrlTree)).toBe('/home');
          resolve();
        });
      });
    }
  });

  it('should allow access when user is not authenticated', async () => {
    authServiceSpy.checkSession.mockReturnValue(of(false));

    const route = {
      queryParams: {},
    } as unknown as ActivatedRouteSnapshot;
    const state = {} as RouterStateSnapshot;

    const result$ = executeGuard(route, state);
    if (typeof result$ === 'object' && 'subscribe' in result$) {
      await new Promise<void>((resolve) => {
        result$.subscribe((result) => {
          expect(result).toBe(true);
          resolve();
        });
      });
    }
  });
});
