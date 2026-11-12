import { TestBed } from '@angular/core/testing';
import { EnderecoFormDialogComponent } from './endereco-form-dialog.component';
import { PageResponse, UsuarioResponse } from '../../core/models';

describe('EnderecoFormDialogComponent', () => {
  const usuario: UsuarioResponse = {
    id: 12,
    nome: 'Pessoa fora da primeira página',
    email: 'pessoa@example.com',
    perfil: 'USER',
    createdAt: '2025-01-01T00:00:00',
    updatedAt: '2025-01-01T00:00:00',
  };
  const pagina: PageResponse<UsuarioResponse> = {
    content: [],
    page: 0,
    size: 10,
    totalElements: 11,
    totalPages: 2,
    first: true,
    last: false,
  };

  it('emits the requested user page and preserves the selected owner', async () => {
    await TestBed.configureTestingModule({
      imports: [EnderecoFormDialogComponent],
    }).compileComponents();

    const fixture = TestBed.createComponent(EnderecoFormDialogComponent);
    fixture.componentRef.setInput('isAdmin', true);
    fixture.componentRef.setInput('usuarios', []);
    fixture.componentRef.setInput('paginaUsuarios', pagina);
    fixture.componentRef.setInput('usuarioSelecionado', usuario);
    fixture.detectChanges();

    const pageChange = vi.fn();
    fixture.componentInstance.usuariosPageChange.subscribe(pageChange);
    const nextButton = fixture.nativeElement.querySelector(
      '.selector-pagination button:last-child',
    ) as HTMLButtonElement;
    nextButton.click();

    expect(pageChange).toHaveBeenCalledWith(1);
    expect(fixture.componentInstance.usuarioId).toBe(usuario.id);
  });

  it('emits the owner and only user-provided address fields when submitted', async () => {
    await TestBed.configureTestingModule({
      imports: [EnderecoFormDialogComponent],
    }).compileComponents();

    const fixture = TestBed.createComponent(EnderecoFormDialogComponent);
    fixture.componentRef.setInput('isAdmin', true);
    fixture.componentRef.setInput('usuarios', [usuario]);
    fixture.componentRef.setInput('usuarioSelecionado', usuario);
    fixture.detectChanges();
    const saved = vi.fn();
    fixture.componentInstance.saved.subscribe(saved);
    fixture.componentInstance.formulario = {
      rua: ' Rua A ',
      numero: ' 10 ',
      complemento: '',
      bairro: ' Centro ',
      cidade: ' São Paulo ',
      estado: ' SP ',
      cep: ' 01001-000 ',
    };

    fixture.componentInstance.submit();

    expect(saved).toHaveBeenCalledWith({
      usuarioId: usuario.id,
      request: {
        numero: '10',
        complemento: '',
        cep: '01001-000',
      },
    });
  });
});
