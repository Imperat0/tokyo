import { TestBed } from '@angular/core/testing';
import { UsuariosListComponent } from './usuarios-list.component';
import { PageResponse, UsuarioResponse } from '../../core/models';

describe('UsuariosListComponent', () => {
  it('emits the next page when pagination is used', async () => {
    await TestBed.configureTestingModule({
      imports: [UsuariosListComponent],
    }).compileComponents();

    const fixture = TestBed.createComponent(UsuariosListComponent);
    const usuario: UsuarioResponse = {
      id: 1,
      nome: 'Ana Silva',
      email: 'ana@example.com',
      perfil: 'USER',
      createdAt: '2025-01-01T00:00:00',
      updatedAt: '2025-01-01T00:00:00',
    };
    const page: PageResponse<UsuarioResponse> = {
      content: [usuario],
      page: 0,
      size: 10,
      totalElements: 11,
      totalPages: 2,
      first: true,
      last: false,
    };
    fixture.componentRef.setInput('pagina', page);
    fixture.detectChanges();
    const pageChange = vi.fn();
    fixture.componentInstance.pageChange.subscribe(pageChange);

    const nextButton = fixture.nativeElement.querySelector(
      '.pagination button:last-child',
    ) as HTMLButtonElement;
    nextButton.click();

    expect(pageChange).toHaveBeenCalledWith(1);
  });
});
