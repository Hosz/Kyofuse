import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "../services/auth/auth.service";
import { catchError, switchMap, throwError } from "rxjs";
import { Router } from "@angular/router";

export const AuthInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const request = req.clone({ withCredentials: true });

    if (request.url.includes('/api/auth')) {
        return next(request);
    }

    return next(request).pipe(
        catchError((error: unknown) => {
            if (error instanceof HttpErrorResponse && error.status === 401) {
                return authService.refresh().pipe(
                    switchMap((refreshed) => {
                        if (refreshed) {
                            return next(request);
                        }

                        authService.clearSession();
                        router.navigateByUrl('');
                        return throwError(() => error);
                    }),
                );
            }

            return throwError(() => error);
        })
    )

}
