import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  EnderecoResponse,
  PageResponse,
  UsuarioResponse,
} from '../../core/models';

@Component({
  selector: 'app-enderecos-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './enderecos-list.component.html',
})
export class EnderecosListComponent {
  @Input({ required: true }) pagina!: PageResponse<EnderecoResponse>;
  @Input() isAdmin = false;
  @Input() isLoading = false;
  @Input() ordenacao = 'createdAt,desc';
  @Input() vendoUsuario: UsuarioResponse | null = null;
  @Input() usuarios: UsuarioResponse[] = [];

  @Output() readonly edit = new EventEmitter<EnderecoResponse>();
  @Output() readonly delete = new EventEmitter<EnderecoResponse>();
  @Output() readonly showAll = new EventEmitter<void>();
  @Output() readonly sortChange = new EventEmitter<string>();
  @Output() readonly pageChange = new EventEmitter<number>();

  onSortChange(event: Event): void {
    this.sortChange.emit((event.target as HTMLSelectElement).value);
  }

  nomeUsuario(endereco: EnderecoResponse): string {
    return this.usuarios.find((usuario) => usuario.id === endereco.usuarioId)?.nome
      ?? `Usuário #${endereco.usuarioId}`;
  }
}
