import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  EnderecoRequest,
  EnderecoResponse,
  PageResponse,
  UsuarioResponse,
} from '../../core/models';

export interface EnderecoFormValue {
  request: EnderecoRequest;
  usuarioId: number | null;
}

@Component({
  selector: 'app-endereco-form-dialog',
  imports: [CommonModule, FormsModule],
  templateUrl: './endereco-form-dialog.component.html',
})
export class EnderecoFormDialogComponent implements OnChanges {
  @Input() endereco: EnderecoResponse | null = null;
  @Input() usuarios: UsuarioResponse[] = [];
  @Input() paginaUsuarios: PageResponse<UsuarioResponse> | null = null;
  @Input() carregandoUsuarios = false;
  @Input() isAdmin = false;
  @Input() isSubmitting = false;
  @Input() usuarioSelecionado: UsuarioResponse | null = null;

  @Output() readonly saved = new EventEmitter<EnderecoFormValue>();
  @Output() readonly usuarioChange = new EventEmitter<UsuarioResponse | null>();
  @Output() readonly usuariosPageChange = new EventEmitter<number>();
  @Output() readonly closed = new EventEmitter<void>();

  formulario: EnderecoRequest = this.emptyForm();
  usuarioId: number | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['endereco']) {
      this.formulario = this.endereco
        ? {
            rua: this.endereco.rua,
            numero: this.endereco.numero,
            complemento: this.endereco.complemento ?? '',
            bairro: this.endereco.bairro,
            cidade: this.endereco.cidade,
            estado: this.endereco.estado,
            cep: this.endereco.cep,
          }
        : this.emptyForm();
    }
    if (changes['endereco'] || changes['usuarioSelecionado']) {
      this.usuarioId = this.endereco?.usuarioId ?? this.usuarioSelecionado?.id ?? null;
    }
  }

  onUsuarioChange(usuarioId: number | null): void {
    this.usuarioId = usuarioId;
    const usuario = this.usuarios.find((item) => item.id === usuarioId) ?? null;
    this.usuarioChange.emit(usuario);
  }

  get usuarioSelecionadoForaDaPagina(): boolean {
    return !!this.usuarioSelecionado
      && !this.usuarios.some((usuario) => usuario.id === this.usuarioSelecionado?.id);
  }

  submit(): void {
    const request: EnderecoRequest = {
      numero: this.formulario.numero.trim(),
      complemento: this.formulario.complemento.trim(),
      cep: this.formulario.cep.trim(),
    };
    this.saved.emit({ request, usuarioId: this.usuarioId });
  }

  private emptyForm(): EnderecoRequest {
    return {
      rua: '',
      numero: '',
      complemento: '',
      bairro: '',
      cidade: '',
      estado: '',
      cep: '',
    };
  }
}
