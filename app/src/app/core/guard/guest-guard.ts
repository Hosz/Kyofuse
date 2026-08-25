import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../services/auth/auth.service';

/** Impede que um usuário já autenticado veja a tela de login/registro de novo. */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.checkSession().pipe(
    map((authenticated) => authenticated ? router.createUrlTree(['/home']) : true)
  );
};
