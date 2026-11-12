import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  EnderecoResponse,
  PageResponse,
  UsuarioResponse,
} from '../../core/models';

@Component({
  selector: 'app-overview',
  imports: [CommonModule],
  templateUrl: './overview.component.html',
})
export class OverviewComponent {
  @Input() isAdmin = false;
  @Input() isLoading = false;
  @Input() usuarios: PageResponse<UsuarioResponse> = this.emptyPage();
  @Input() usuariosRecentes: UsuarioResponse[] = [];
  @Input() enderecos: PageResponse<EnderecoResponse> = this.emptyPage();

  @Output() readonly sectionChange = new EventEmitter<'users' | 'addresses'>();
  @Output() readonly viewUserAddresses = new EventEmitter<UsuarioResponse>();
  @Output() readonly addAddress = new EventEmitter<void>();
  @Output() readonly editAddress = new EventEmitter<EnderecoResponse>();

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
}
