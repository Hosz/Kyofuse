import { HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "../services/auth/auth.service";
import { catchError, throwError } from "rxjs";
import { Router } from "@angular/router";

export const AuthInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (req.url.includes('/api/auth')) {
        return next(req);
    }

    const token = authService.getToken();

    const request = token ? req.clone({
        setHeaders: {
            Authorization: `Bearer ${token}`
        }
    }) : req;

    return next(request).pipe(
        catchError(error => {
            if (error.status === 401) {
                authService.clearToken();
                router.navigateByUrl('');
            }

            return throwError(() => error);
        })
    )

}
