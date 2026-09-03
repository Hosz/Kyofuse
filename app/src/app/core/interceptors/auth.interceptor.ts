import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "../services/auth/auth.service";
import { AccountManagerService } from "../services/auth/account-manager.service";
import { ToastService } from "../services/ui/toast.service";
import { catchError, switchMap, throwError } from "rxjs";

/**
 * Endpoints de auth que não fazem sentido reprocessar com refresh+retry: são os pontos
 * de entrada do fluxo (login/register), o próprio refresh, o logout, e o passo de
 * verificação de 2FA (que roda com um mfaToken, não com o access token). Note que
 * /api/auth/2fa/{setup,confirm,disable} FICAM de fora dessa lista — são chamadas
 * autenticadas normais (settings da conta) e devem se beneficiar do retry como
 * qualquer outro endpoint protegido.
 *
 * IMPORTANTE: Logout é incluído aqui, mas se a própria requisição de logout falhar com
 * 401 (access token expirado), o interceptador ainda vai tentar fazer refresh.
 * Para evitar ciclos infinitos, diferenciamos 400 (token ausente) de 401 (expirado).
 */
const PUBLIC_AUTH_PATHS = [
    '/api/auth/register',
    '/api/auth/login',
    '/api/auth/refresh',
    '/api/auth/logout',
    '/api/auth/2fa/verify',
    '/api/auth/switch-account',
    '/api/auth/disconnect-account',
];

export const AuthInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const accountManager = inject(AccountManagerService);

    const deviceId = accountManager.getDeviceId();
    const headers = req.headers.has('X-Device-Id') ? req.headers : req.headers.set('X-Device-Id', deviceId);

    const request = req.clone({
        withCredentials: true,
        headers,
    });

    if (PUBLIC_AUTH_PATHS.some((path) => request.url.includes(path))) {
        return next(request);
    }

    return next(request).pipe(
        catchError((error: unknown) => {
            if (!(error instanceof HttpErrorResponse)) {
                return throwError(() => error);
            }

            /**
             * ⚠️ NOVO: Tratar 400 Bad Request (refresh token ausente)
             * Indica erro técnico: cookies foram deletados, sessão realmente expirou.
             * Nunca tenta retry, pois não há ponto de recuperação.
             */
            if (error.status === 400) {
                // Token ausente = erro técnico (não tenta retry)
                authService.clearSession();
                // Apenas throwError, NÃO faz navigate (evita ciclo de requisições)
                return throwError(() => error);
            }

            /**
             * ✅ Tratar 401 Unauthorized (access token expirado)
             * Indica que o token precisa ser renovado. Tenta fazer refresh uma vez.
             * A navegação para login fica a cargo dos guards (authGuard/guestGuard),
             * NÃO do interceptor — navigateByUrl aqui causava um ciclo infinito
             * porque disparava guestGuard → checkSession() → 401 → refresh → navigateByUrl → ...
             */
            if (error.status === 401) {
                return authService.refresh().pipe(
                    switchMap((refreshed) => {
                        if (refreshed) {
                            return next(request);
                        }

                        authService.clearSession();
                        return throwError(() => error);
                    }),
                );
            }

            /**
             * 🛡️ Tratar 429 Too Many Requests (Rate Limiter do Redis)
             */
            if (error.status === 429) {
                const retryAfter = error.headers.get('Retry-After');
                const toastService = inject(ToastService, { optional: true });
                const message = error.error?.message || (retryAfter ? `Muitas requisições. Aguarde ${retryAfter} segundos antes de tentar novamente.` : 'Limite de requisições excedido. Tente novamente em instantes.');
                if (toastService) {
                    toastService.error(message);
                }
                return throwError(() => error);
            }

            return throwError(() => error);
        })
    )

}
