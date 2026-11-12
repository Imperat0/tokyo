import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';
import { authInterceptor } from './auth.interceptor';
import { UsuarioResponse } from './models';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('fetches CSRF before login and loads the authenticated profile by cookie', () => {
    const user: UsuarioResponse = {
      id: 1,
      nome: 'Ana Silva',
      email: 'ana@example.com',
      perfil: 'USER',
      createdAt: '2025-01-01T00:00:00',
      updatedAt: '2025-01-01T00:00:00',
    };
    let response: UsuarioResponse | undefined;

    service.login({ email: user.email, senha: 'senha-segura' }).subscribe((result) => {
      response = result;
    });

    const csrf = http.expectOne(`${environment.apiUrl}/auth/csrf`);
    expect(csrf.request.withCredentials).toBe(true);
    csrf.flush({ token: 'csrf-token' });

    const login = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(login.request.method).toBe('POST');
    expect(login.request.withCredentials).toBe(true);
    expect(login.request.headers.get('X-XSRF-TOKEN')).toBe('csrf-token');
    expect(login.request.headers.has('Authorization')).toBe(false);
    login.flush(null, { status: 204, statusText: 'No Content' });

    const profile = http.expectOne(`${environment.apiUrl}/usuarios/me`);
    expect(profile.request.withCredentials).toBe(true);
    profile.flush(user);

    expect(response).toEqual(user);
    expect(service.currentUser()).toEqual(user);
  });

  it('clears local user state before requesting server logout', () => {
    service.setCurrentUser({
      id: 1,
      nome: 'Ana Silva',
      email: 'ana@example.com',
      perfil: 'USER',
      createdAt: '2025-01-01T00:00:00',
      updatedAt: '2025-01-01T00:00:00',
    });

    service.logout().subscribe();

    expect(service.currentUser()).toBeNull();
    http.expectOne(`${environment.apiUrl}/auth/csrf`).flush({ token: 'csrf-token' });
    const logout = http.expectOne(`${environment.apiUrl}/auth/logout`);
    expect(logout.request.headers.get('X-XSRF-TOKEN')).toBe('csrf-token');
    logout.flush(null, { status: 204, statusText: 'No Content' });
  });
});
