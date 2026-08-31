import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../services/auth/auth.service';

export const profileSetupGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.me().pipe(
    map((response) => {
      const isPending = response.profileSetupStatus === 'PENDING';
      const isSetupRoute = state.url.includes('/setup');

      if (isPending && !isSetupRoute) {
        return router.createUrlTree(['/setup']);
      }

      return true;
    }),
    catchError(() => of(true))
  );
};
