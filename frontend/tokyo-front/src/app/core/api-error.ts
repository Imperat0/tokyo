import { HttpErrorResponse } from '@angular/common/http';

export function apiErrorMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Não foi possível concluir a operação. Tente novamente.';
  }

  if (typeof error.error === 'object' && error.error !== null) {
    const body = error.error as Record<string, unknown>;
    if (typeof body['detail'] === 'string') {
      return body['detail'];
    }
    if (typeof body['message'] === 'string') {
      return body['message'];
    }
  }

  if (error.status === 0) {
    return 'Não foi possível conectar à API. Verifique se o servidor está ativo.';
  }
  if (error.status === 401) {
    return 'Sessão expirada. Entre novamente para continuar.';
  }
  if (error.status === 403) {
    return 'Você não tem permissão para realizar esta ação.';
  }
  if (error.status === 409) {
    return 'Este e-mail já está cadastrado.';
  }
  return 'Ocorreu um erro ao comunicar com a API. Tente novamente.';
}
