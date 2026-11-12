import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { of, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';
import { App } from './app';
import { ApiService } from './core/api.service';
import { AuthService } from './core/auth.service';
import { EnderecoResponse, PageResponse, UsuarioResponse } from './core/models';

describe('App', () => {
  const usuario: UsuarioResponse = {
    id: 1,
    nome: 'Ana Silva',
    email: 'ana@example.com',
    perfil: 'USER',
    createdAt: '2025-01-01T00:00:00',
    updatedAt: '2025-01-01T00:00:00',
  };
  const paginaVazia: PageResponse<UsuarioResponse> = {
    content: [],
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  };
  const paginaVaziaEndereco: PageResponse<EnderecoResponse> = {
    ...paginaVazia,
    content: [],
  };
  const usuarioAtual = signal<UsuarioResponse | null>(null);
  const authMock = {
    currentUser: usuarioAtual.asReadonly(),
    clearSession: vi.fn(() => usuarioAtual.set(null)),
    getCurrentUser: vi.fn(() => throwError(() => new HttpErrorResponse({ status: 401 }))),
    logout: vi.fn(() => of(undefined)),
    register: vi.fn(),
    setCurrentUser: vi.fn((user: UsuarioResponse) => usuarioAtual.set(user)),
  };
  const apiMock = {
    addresses: vi.fn(() => of(paginaVaziaEndereco)),
    addressesForUser: vi.fn(() => of(paginaVaziaEndereco)),
    createAddress: vi.fn(),
    deleteAddress: vi.fn(),
    deleteUser: vi.fn(),
    updateAddress: vi.fn(),
    updateUser: vi.fn(),
    users: vi.fn(() => of(paginaVazia)),
  };

  beforeEach(async () => {
    usuarioAtual.set(null);
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideHttpClient(),
        { provide: AuthService, useValue: authMock },
        { provide: ApiService, useValue: apiMock },
      ],
    })
      .compileComponents();
  });

  it('renders the login screen', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.boot-screen')).toBeNull();
    expect(compiled.querySelector('h2')?.textContent).toContain('Acesse sua conta');
  });

  it('renders API errors in a visible error toast', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();

    fixture.componentInstance.onAuthFeedback({
      message: 'CEP não encontrado. Verifique o número informado.',
      type: 'error',
    });
    fixture.detectChanges();

    const toast = (fixture.nativeElement as HTMLElement).querySelector('.toast');
    expect(toast?.textContent).toContain('CEP não encontrado. Verifique o número informado.');
    expect(toast?.classList.contains('toast-error')).toBe(true);
  });

  it('clears the address loading state after the API response', async () => {
    const addresses = new Subject<PageResponse<EnderecoResponse>>();
    apiMock.addresses.mockReturnValue(addresses);
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    usuarioAtual.set({ ...usuario, perfil: 'ADMIN' });
    fixture.detectChanges();

    fixture.componentInstance.activeSection = 'addresses';
    fixture.componentInstance.loadAddresses();
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Carregando endereços...');

    addresses.next(paginaVaziaEndereco);
    addresses.complete();
    await fixture.whenStable();

    const text = (fixture.nativeElement as HTMLElement).textContent;
    expect(fixture.componentInstance.isLoading()).toBe(false);
    expect(text).not.toContain('Carregando endereços...');
    expect(text).toContain('Nenhum endereço cadastrado');
  });

  it('ends the session when the current user changes their email', () => {
    usuarioAtual.set(usuario);
    const updatedUser = { ...usuario, email: 'ana.nova@example.com' };
    apiMock.updateUser.mockReturnValue(of(updatedUser));

    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    app.editingUser = usuario;
    const loadDashboard = vi.spyOn(app, 'loadDashboard');

    app.submitUser({ nome: usuario.nome, email: updatedUser.email });

    expect(authMock.logout).toHaveBeenCalledOnce();
    expect(authMock.setCurrentUser).not.toHaveBeenCalled();
    expect(app.toastMessage()).toContain('Entre novamente');
    expect(loadDashboard).not.toHaveBeenCalled();
  });

  it('keeps the admin session when updating another user email', () => {
    const admin: UsuarioResponse = { ...usuario, id: 2, perfil: 'ADMIN' };
    usuarioAtual.set(admin);
    apiMock.updateUser.mockReturnValue(of({ ...usuario, email: 'ana.nova@example.com' }));

    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    app.editingUser = usuario;
    const loadDashboard = vi.spyOn(app, 'loadDashboard');

    app.submitUser({ nome: usuario.nome, email: 'ana.nova@example.com' });

    expect(authMock.logout).not.toHaveBeenCalled();
    expect(loadDashboard).toHaveBeenCalledOnce();
  });

  it('loads address-owner options independently from the users list page', () => {
    const owner = { ...usuario, id: 12 };
    apiMock.users.mockReturnValue(of({
      ...paginaVazia,
      content: [owner],
      page: 3,
      totalElements: 31,
      totalPages: 4,
      first: false,
      last: true,
    }));

    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    app.usuariosPage = { ...paginaVazia, content: [usuario] };
    app.userPage = 0;

    app.loadAddressSelectorUsers(3);

    expect(apiMock.users).toHaveBeenCalledWith(3, 'nome,asc');
    expect(app.addressUsersPage.content).toEqual([owner]);
    expect(app.usuariosPage.content).toEqual([usuario]);
  });

  it('requests recent users ordered by creation date', () => {
    const admin: UsuarioResponse = { ...usuario, perfil: 'ADMIN' };
    usuarioAtual.set(admin);
    apiMock.users.mockReturnValue(of(paginaVazia));

    const fixture = TestBed.createComponent(App);
    fixture.componentInstance.loadDashboard();

    expect(apiMock.users).toHaveBeenCalledWith(0, 'createdAt,desc', 5);
  });
});
