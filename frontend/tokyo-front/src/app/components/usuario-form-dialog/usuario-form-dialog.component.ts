import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  UsuarioAtualizarRequest,
  UsuarioCriarRequest,
  UsuarioResponse,
} from '../../core/models';

@Component({
  selector: 'app-usuario-form-dialog',
  imports: [CommonModule, FormsModule],
  templateUrl: './usuario-form-dialog.component.html',
})
export class UsuarioFormDialogComponent implements OnChanges {
  @Input() usuario: UsuarioResponse | null = null;
  @Input() isSubmitting = false;

  @Output() readonly saved = new EventEmitter<UsuarioCriarRequest | UsuarioAtualizarRequest>();
  @Output() readonly closed = new EventEmitter<void>();

  formulario = { nome: '', email: '', senha: '' };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['usuario']) {
      this.formulario = {
        nome: this.usuario?.nome ?? '',
        email: this.usuario?.email ?? '',
        senha: '',
      };
    }
  }

  submit(): void {
    const dados = {
      nome: this.formulario.nome.trim(),
      email: this.formulario.email.trim(),
    };
    if (this.usuario) {
      const request: UsuarioAtualizarRequest = dados;
      if (this.formulario.senha.trim()) request.senha = this.formulario.senha;
      this.saved.emit(request);
      return;
    }
    this.saved.emit({ ...dados, senha: this.formulario.senha });
  }
}
