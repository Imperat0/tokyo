import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const isApiRequest = request.url.startsWith(environment.apiUrl);
  const csrfToken = auth.csrfToken;
  const unsafeMethod = !['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(request.method);
  const headers: Record<string, string> = {};
  if (unsafeMethod && csrfToken) headers['X-XSRF-TOKEN'] = csrfToken;
  const authenticatedRequest = isApiRequest
    ? request.clone({ withCredentials: true, setHeaders: headers })
    : request;

  return next(authenticatedRequest).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        auth.clearSession();
      }
      return throwError(() => error);
    }),
  );
};
