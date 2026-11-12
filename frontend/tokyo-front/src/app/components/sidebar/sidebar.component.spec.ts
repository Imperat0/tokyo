import { TestBed } from '@angular/core/testing';
import { SidebarComponent } from './sidebar.component';
import { UsuarioResponse } from '../../core/models';

describe('SidebarComponent', () => {
  it('shows user management only to administrators', async () => {
    await TestBed.configureTestingModule({
      imports: [SidebarComponent],
    }).compileComponents();

    const fixture = TestBed.createComponent(SidebarComponent);
    const user: UsuarioResponse = {
      id: 1,
      nome: 'Ana Silva',
      email: 'ana@example.com',
      perfil: 'USER',
      createdAt: '2025-01-01T00:00:00',
      updatedAt: '2025-01-01T00:00:00',
    };
    fixture.componentRef.setInput('currentUser', user);
    fixture.componentRef.setInput('isAdmin', false);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).not.toContain('Usuários');

    fixture.componentRef.setInput('isAdmin', true);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Usuários');
  });
});
