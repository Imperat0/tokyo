import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthPageComponent } from './auth-page.component';
import { AuthService } from '../../core/auth.service';

describe('AuthPageComponent', () => {
  it('rejects passwords shorter than the API minimum before submitting', async () => {
    const auth = {
      login: () => of({}),
      register: vi.fn(() => of({})),
    };
    await TestBed.configureTestingModule({
      imports: [AuthPageComponent],
      providers: [{ provide: AuthService, useValue: auth }],
    }).compileComponents();

    const fixture = TestBed.createComponent(AuthPageComponent);
    const feedback = vi.fn();
    fixture.componentInstance.feedback.subscribe(feedback);
    fixture.componentInstance.registration = {
      nome: 'Ana Silva',
      email: 'ana@example.com',
      senha: '321',
      confirmarSenha: '321',
    };

    fixture.componentInstance.submitRegistration();

    expect(auth.register).not.toHaveBeenCalled();
    expect(feedback).toHaveBeenCalledWith({
      message: 'A senha deve ter entre 8 e 72 caracteres.',
      type: 'error',
    });
  });

  it('surfaces API errors as feedback', async () => {
    const auth = {
      login: () => throwError(() => new HttpErrorResponse({
        status: 401,
        error: { detail: 'Credenciais inválidas.' },
      })),
      register: () => of({}),
    };
    await TestBed.configureTestingModule({
      imports: [AuthPageComponent],
      providers: [{ provide: AuthService, useValue: auth }],
    }).compileComponents();

    const fixture = TestBed.createComponent(AuthPageComponent);
    const feedback = vi.fn();
    fixture.componentInstance.feedback.subscribe(feedback);
    fixture.componentInstance.loginEmail = 'ana@example.com';
    fixture.componentInstance.loginPassword = 'senha-incorreta';

    fixture.componentInstance.submitLogin();

    expect(feedback).toHaveBeenCalledWith({
      message: 'Credenciais inválidas.',
      type: 'error',
    });
  });
});
