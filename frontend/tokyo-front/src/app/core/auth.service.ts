import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, finalize, switchMap, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  LoginRequest,
  UsuarioCriarRequest,
  UsuarioResponse,
} from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly userState = signal<UsuarioResponse | null>(null);
  private csrfTokenValue: string | null = null;

  readonly currentUser = this.userState.asReadonly();

  get csrfToken(): string | null {
    return this.csrfTokenValue;
  }

  login(request: LoginRequest): Observable<UsuarioResponse> {
    return this.fetchCsrfToken().pipe(
      switchMap(() => this.http.post<void>(`${environment.apiUrl}/auth/login`, request)),
      switchMap(() => this.loadCurrentUser()),
    );
  }

  register(request: UsuarioCriarRequest): Observable<UsuarioResponse> {
    return this.fetchCsrfToken().pipe(
      switchMap(() => this.http.post<UsuarioResponse>(`${environment.apiUrl}/usuarios`, request)),
    );
  }

  getCurrentUser(): Observable<UsuarioResponse> {
    return this.fetchCsrfToken().pipe(
      switchMap(() => this.loadCurrentUser()),
    );
  }

  setCurrentUser(user: UsuarioResponse): void {
    this.userState.set(user);
  }

  logout(): Observable<void> {
    this.userState.set(null);
    return this.fetchCsrfToken().pipe(
      switchMap(() => this.http.post<void>(`${environment.apiUrl}/auth/logout`, {})),
      finalize(() => {
        this.csrfTokenValue = null;
      }),
    );
  }

  clearSession(): void {
    this.userState.set(null);
    this.csrfTokenValue = null;
  }

  private fetchCsrfToken(): Observable<{ token: string }> {
    return this.http.get<{ token: string }>(`${environment.apiUrl}/auth/csrf`).pipe(
      tap((response) => {
        this.csrfTokenValue = response.token;
      }),
    );
  }

  private loadCurrentUser(): Observable<UsuarioResponse> {
    return this.http
      .get<UsuarioResponse>(`${environment.apiUrl}/usuarios/me`)
      .pipe(tap((user) => this.userState.set(user)));
  }
}
