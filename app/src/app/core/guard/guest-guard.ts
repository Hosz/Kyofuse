import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../services/auth/auth.service';

/** Impede que um usuário já autenticado veja a tela de login/registro de novo, exceto ao adicionar outra conta. */
export const guestGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (route.queryParams['addAccount'] === 'true') {
    return true;
  }

  return authService.checkSession().pipe(
    map((authenticated) => authenticated ? router.createUrlTree(['/home']) : true)
  );
};
