import { TestBed } from '@angular/core/testing';
import { UsuarioFormDialogComponent } from './usuario-form-dialog.component';
import { UsuarioResponse } from '../../core/models';

describe('UsuarioFormDialogComponent', () => {
  it('omits a blank password when editing an existing user', async () => {
    await TestBed.configureTestingModule({
      imports: [UsuarioFormDialogComponent],
    }).compileComponents();

    const fixture = TestBed.createComponent(UsuarioFormDialogComponent);
    const usuario: UsuarioResponse = {
      id: 1,
      nome: 'Ana Silva',
      email: 'ana@example.com',
      perfil: 'USER',
      createdAt: '2025-01-01T00:00:00',
      updatedAt: '2025-01-01T00:00:00',
    };
    fixture.componentRef.setInput('usuario', usuario);
    fixture.detectChanges();
    const saved = vi.fn();
    fixture.componentInstance.saved.subscribe(saved);
    fixture.componentInstance.formulario = {
      nome: '  Ana Souza  ',
      email: '  ana.souza@example.com  ',
      senha: '   ',
    };

    fixture.componentInstance.submit();

    expect(saved).toHaveBeenCalledWith({
      nome: 'Ana Souza',
      email: 'ana.souza@example.com',
    });
  });
});
