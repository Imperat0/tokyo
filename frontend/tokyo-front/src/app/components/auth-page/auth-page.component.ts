import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { apiErrorMessage } from '../../core/api-error';
import { AuthService } from '../../core/auth.service';
import {
  LoginRequest,
  UsuarioCriarRequest,
  UsuarioResponse,
} from '../../core/models';

type AuthView = 'login' | 'register';
type Feedback = { message: string; type: 'success' | 'error' };

@Component({
  selector: 'app-auth-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './auth-page.component.html',
})
export class AuthPageComponent {
  @Output() readonly authenticated = new EventEmitter<UsuarioResponse>();
  @Output() readonly feedback = new EventEmitter<Feedback>();

  authView: AuthView = 'login';
  isSubmitting = signal(false);
  loginEmail = '';
  loginPassword = '';
  registration = { nome: '', email: '', senha: '', confirmarSenha: '' };

  constructor(private readonly auth: AuthService) {}

  submitLogin(): void {
    const request: LoginRequest = {
      email: this.loginEmail.trim(),
      senha: this.loginPassword,
    };
    this.isSubmitting.set(true);
    this.auth.login(request).subscribe({
      next: (user) => {
        this.isSubmitting.set(false);
        this.authenticated.emit(user);
      },
      error: (error: unknown) => {
        this.isSubmitting.set(false);
        this.feedback.emit({ message: apiErrorMessage(error), type: 'error' });
      },
    });
  }

  submitRegistration(): void {
    if (this.registration.senha.length < 8 || this.registration.senha.length > 72) {
      this.feedback.emit({
        message: 'A senha deve ter entre 8 e 72 caracteres.',
        type: 'error',
      });
      return;
    }

    if (this.registration.senha !== this.registration.confirmarSenha) {
      this.feedback.emit({
        message: 'As senhas informadas não coincidem.',
        type: 'error',
      });
      return;
    }

    const request: UsuarioCriarRequest = {
      nome: this.registration.nome.trim(),
      email: this.registration.email.trim(),
      senha: this.registration.senha,
    };
    this.isSubmitting.set(true);
    this.auth.register(request).subscribe({
      next: (user) => {
        this.isSubmitting.set(false);
        this.loginEmail = user.email;
        this.registration = { nome: '', email: '', senha: '', confirmarSenha: '' };
        this.authView = 'login';
        this.feedback.emit({
          message: 'Conta criada. Entre com seu e-mail e senha.',
          type: 'success',
        });
      },
      error: (error: unknown) => {
        this.isSubmitting.set(false);
        this.feedback.emit({ message: apiErrorMessage(error), type: 'error' });
      },
    });
  }
}
