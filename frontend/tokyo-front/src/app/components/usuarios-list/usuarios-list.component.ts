import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PageResponse, UsuarioResponse } from '../../core/models';

@Component({
  selector: 'app-usuarios-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios-list.component.html',
})
export class UsuariosListComponent {
  @Input({ required: true }) pagina!: PageResponse<UsuarioResponse>;
  @Input() isLoading = false;
  @Input() ordenacao = 'nome,asc';

  @Output() readonly edit = new EventEmitter<UsuarioResponse>();
  @Output() readonly delete = new EventEmitter<UsuarioResponse>();
  @Output() readonly viewAddresses = new EventEmitter<UsuarioResponse>();
  @Output() readonly sortChange = new EventEmitter<string>();
  @Output() readonly pageChange = new EventEmitter<number>();

  onSortChange(event: Event): void {
    this.sortChange.emit((event.target as HTMLSelectElement).value);
  }
}
