import { isPlatformBrowser } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { forkJoin, Observable } from 'rxjs';
import { AuthPageComponent } from './components/auth-page/auth-page.component';
import { EnderecoFormDialogComponent, EnderecoFormValue } from './components/endereco-form-dialog/endereco-form-dialog.component';
import { EnderecosListComponent } from './components/enderecos-list/enderecos-list.component';
import { OverviewComponent } from './components/overview/overview.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { UsuarioFormDialogComponent } from './components/usuario-form-dialog/usuario-form-dialog.component';
import { UsuariosListComponent } from './components/usuarios-list/usuarios-list.component';
import { ApiService } from './core/api.service';
import { apiErrorMessage } from './core/api-error';
import { AuthService } from './core/auth.service';
import {
  DashboardSection,
  EnderecoResponse,
  PageResponse,
  UsuarioAtualizarRequest,
  UsuarioCriarRequest,
  UsuarioResponse,
} from './core/models';

type Modal = 'user' | 'address' | null;

@Component({
  imports: [
    AuthPageComponent,
    EnderecoFormDialogComponent,
    EnderecosListComponent,
    OverviewComponent,
    SidebarComponent,
    UsuarioFormDialogComponent,
    UsuariosListComponent,
  ],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App implements OnInit {
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  activeSection: DashboardSection = 'overview';
  modal: Modal = null;
  isInitializing = signal(true);
  isLoading = signal(false);
  isSubmitting = signal(false);
  isLoadingAddressUsers = signal(false);
  toastMessage = signal('');
  toastType = signal<'success' | 'error'>('success');
  editingUser: UsuarioResponse | null = null;
  editingAddress: EnderecoResponse | null = null;
  viewingUser: UsuarioResponse | null = null;
  addressSelectedUser: UsuarioResponse | null = null;
  usuariosPage = this.emptyPage<UsuarioResponse>();
  usuariosRecentes: UsuarioResponse[] = [];
  addressUsersPage = this.emptyPage<UsuarioResponse>();
  enderecosPage = this.emptyPage<EnderecoResponse>();
  userPage = 0;
  addressPage = 0;
  userSort = 'nome,asc';
  addressSort = 'createdAt,desc';

  constructor(
    readonly auth: AuthService,
    private readonly api: ApiService,
  ) {}

  get currentUser(): UsuarioResponse | null {
    return this.auth.currentUser();
  }

  get isAdmin(): boolean {
    return this.currentUser?.perfil === 'ADMIN';
  }

  get pageTitle(): string {
    if (this.activeSection === 'users') return 'Usuários';
    if (this.activeSection === 'addresses') {
      return this.viewingUser
        ? `Endereços de ${this.viewingUser.nome}`
        : 'Endereços';
    }
    return 'Visão geral';
  }

  ngOnInit(): void {
    if (!this.isBrowser) return;

    this.auth.getCurrentUser().subscribe({
      next: () => {
        this.isInitializing.set(false);
        this.loadDashboard();
      },
      error: (error: unknown) => {
        this.auth.clearSession();
        this.isInitializing.set(false);
        if (!(error instanceof HttpErrorResponse && error.status === 401)) {
          this.showToast(apiErrorMessage(error), 'error');
        }
      },
    });
  }

  onAuthenticated(user: UsuarioResponse): void {
    this.auth.setCurrentUser(user);
    this.activeSection = 'overview';
    this.viewingUser = null;
    this.loadDashboard();
  }

  onAuthFeedback(feedback: { message: string; type: 'success' | 'error' }): void {
    this.showToast(feedback.message, feedback.type);
  }

  loadDashboard(): void {
    if (!this.currentUser) return;
    this.isLoading.set(true);

    if (this.isAdmin) {
      forkJoin({
        usuarios: this.api.users(this.userPage, this.userSort),
        usuariosRecentes: this.api.users(0, 'createdAt,desc', 5),
        enderecos: this.getAddressesPage(),
      }).subscribe({
        next: ({ usuarios, usuariosRecentes, enderecos }) => {
          this.usuariosPage = usuarios;
          this.usuariosRecentes = usuariosRecentes.content;
          this.enderecosPage = enderecos;
          this.isLoading.set(false);
        },
        error: (error: unknown) => {
          this.isLoading.set(false);
          this.showToast(apiErrorMessage(error), 'error');
        },
      });
      return;
    }

    this.getAddressesPage().subscribe({
      next: (pagina) => {
        this.enderecosPage = pagina;
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.showToast(apiErrorMessage(error), 'error');
      },
    });
  }

  changeSection(section: DashboardSection): void {
    this.activeSection = section;
    if (section === 'addresses') {
      this.viewingUser = null;
      this.addressPage = 0;
      this.loadAddresses();
    }
  }

  viewUserAddresses(user: UsuarioResponse): void {
    this.viewingUser = user;
    this.activeSection = 'addresses';
    this.addressPage = 0;
    this.loadAddresses();
  }

  changeUserPage(page: number): void {
    this.userPage = page;
    this.loadUsers();
  }

  changeAddressPage(page: number): void {
    this.addressPage = page;
    this.loadAddresses();
  }

  showAllAddresses(): void {
    this.viewingUser = null;
    this.addressPage = 0;
    this.loadAddresses();
  }

  changeUserSort(sort: string): void {
    this.userSort = sort;
    this.userPage = 0;
    this.loadUsers();
  }

  changeAddressSort(sort: string): void {
    this.addressSort = sort;
    this.addressPage = 0;
    this.loadAddresses();
  }

  loadUsers(): void {
    this.isLoading.set(true);
    this.api.users(this.userPage, this.userSort).subscribe({
      next: (pagina) => {
        this.usuariosPage = pagina;
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.showToast(apiErrorMessage(error), 'error');
      },
    });
  }

  loadAddresses(): void {
    this.isLoading.set(true);
    this.getAddressesPage().subscribe({
      next: (pagina) => {
        this.enderecosPage = pagina;
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);
        this.showToast(apiErrorMessage(error), 'error');
      },
    });
  }

  openNewUser(): void {
    this.editingUser = null;
    this.modal = 'user';
  }

  openEditUser(user: UsuarioResponse): void {
    this.editingUser = user;
    this.modal = 'user';
  }

  openProfile(): void {
    if (this.currentUser) this.openEditUser(this.currentUser);
  }

  submitUser(request: UsuarioCriarRequest | UsuarioAtualizarRequest): void {
    let action: Observable<UsuarioResponse>;
    if (this.editingUser) {
      action = this.api.updateUser(this.editingUser.id, request);
    } else if (typeof request.senha === 'string') {
      action = this.auth.register({
        nome: request.nome,
        email: request.email,
        senha: request.senha,
      });
    } else {
      this.showToast('Informe uma senha para criar o usuário.', 'error');
      return;
    }

    const editingUser = this.editingUser;
    this.isSubmitting.set(true);
    action.subscribe({
      next: (user) => {
        this.isSubmitting.set(false);
        this.closeModal();
        const isCurrentUser = this.currentUser?.id === user.id;
        const emailChanged = isCurrentUser && this.currentUser?.email !== user.email;
        if (emailChanged) {
          this.auth.logout().subscribe({
            next: () => this.showToast('E-mail atualizado. Entre novamente com seu novo endereço.'),
            error: (error: unknown) => this.showToast(
              `E-mail atualizado. Entre novamente com seu novo endereço. ${apiErrorMessage(error)}`,
              'error',
            ),
          });
          return;
        }
        if (isCurrentUser) this.auth.setCurrentUser(user);
        this.showToast(editingUser ? 'Usuário atualizado.' : 'Usuário cadastrado.');
        this.loadDashboard();
      },
      error: (error: unknown) => {
        this.isSubmitting.set(false);
        this.showToast(apiErrorMessage(error), 'error');
      },
    });
  }

  deleteUser(user: UsuarioResponse): void {
    if (!confirm(`Excluir o usuário ${user.nome}? Esta ação não pode ser desfeita.`)) {
      return;
    }

    this.api.deleteUser(user.id).subscribe({
      next: () => {
        if (this.currentUser?.id === user.id) {
          this.auth.logout().subscribe({
            next: () => this.showToast('Sua conta foi excluída.'),
            error: (error: unknown) => this.showToast(
              `Conta excluída; não foi possível limpar o cookie de sessão. ${apiErrorMessage(error)}`,
              'error',
            ),
          });
          return;
        }
        this.showToast('Usuário excluído.');
        this.loadDashboard();
      },
      error: (error: unknown) => this.showToast(apiErrorMessage(error), 'error'),
    });
  }

  openNewAddress(): void {
    this.editingAddress = null;
    this.addressSelectedUser = this.viewingUser ?? this.currentUser;
    this.modal = 'address';
    if (this.isAdmin) this.loadAddressSelectorUsers(0);
  }

  openEditAddress(address: EnderecoResponse): void {
    this.editingAddress = address;
    this.addressSelectedUser = null;
    this.modal = 'address';
  }

  loadAddressSelectorUsers(page: number): void {
    this.isLoadingAddressUsers.set(true);
    this.api.users(page, 'nome,asc').subscribe({
      next: (pagina) => {
        this.addressUsersPage = pagina;
        this.isLoadingAddressUsers.set(false);
      },
      error: (error: unknown) => {
        this.isLoadingAddressUsers.set(false);
        this.showToast(apiErrorMessage(error), 'error');
      },
    });
  }

  selectAddressUser(user: UsuarioResponse | null): void {
    this.addressSelectedUser = user;
  }

  submitAddress(value: EnderecoFormValue): void {
    const usuarioId = value.usuarioId ?? this.currentUser?.id ?? null;
    if (!this.editingAddress && usuarioId === null) {
      this.showToast('Selecione o usuário deste endereço.', 'error');
      return;
    }

    let action: Observable<EnderecoResponse>;
    if (this.editingAddress) {
      action = this.api.updateAddress(this.editingAddress.id, value.request);
    } else if (usuarioId !== null) {
      action = this.api.createAddress(usuarioId, value.request);
    } else {
      this.showToast('Selecione o usuário deste endereço.', 'error');
      return;
    }

    const editingAddress = this.editingAddress;
    this.isSubmitting.set(true);
    action.subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.closeModal();
        this.showToast(editingAddress ? 'Endereço atualizado.' : 'Endereço cadastrado.');
        this.loadDashboard();
      },
      error: (error: unknown) => {
        this.isSubmitting.set(false);
        this.showToast(apiErrorMessage(error), 'error');
      },
    });
  }

  deleteAddress(address: EnderecoResponse): void {
    if (!confirm('Excluir este endereço? Esta ação não pode ser desfeita.')) {
      return;
    }

    this.api.deleteAddress(address.id).subscribe({
      next: () => {
        this.showToast('Endereço excluído.');
        this.loadDashboard();
      },
      error: (error: unknown) => this.showToast(apiErrorMessage(error), 'error'),
    });
  }

  closeModal(): void {
    this.modal = null;
  }

  signOut(): void {
    this.closeModal();
    this.auth.logout().subscribe({
      next: () => this.showToast('Você saiu da sua conta.'),
      error: (error: unknown) => this.showToast(
        `Sessão encerrada nesta tela, mas não foi possível limpar o cookie. ${apiErrorMessage(error)}`,
        'error',
      ),
    });
  }

  private getAddressesPage(): Observable<PageResponse<EnderecoResponse>> {
    const user = this.currentUser;
    if (!user) return this.api.addresses(0, this.addressSort);
    if (this.isAdmin && !this.viewingUser) {
      return this.api.addresses(this.addressPage, this.addressSort);
    }
    return this.api.addressesForUser(
      this.viewingUser?.id ?? user.id,
      this.addressPage,
      this.addressSort,
    );
  }

  private emptyPage<T>(): PageResponse<T> {
    return {
      content: [],
      page: 0,
      size: 10,
      totalElements: 0,
      totalPages: 0,
      first: true,
      last: true,
    };
  }

  private showToast(message: string, type: 'success' | 'error' = 'success'): void {
    this.toastType.set(type);
    this.toastMessage.set(message);
    setTimeout(() => {
      if (this.toastMessage() === message) this.toastMessage.set('');
    }, 4200);
  }
}
