import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AuthService } from './auth.service';

/** Adds the Bearer token to every request; on 401 clears the session and returns to login. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  const cloned = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(cloned).pipe(
    catchError(err => {
      if (err.status === 401 && !req.url.includes('/api/auth/login')) {
        auth.logout();
      }
      return throwError(() => err);
    })
  );
};
