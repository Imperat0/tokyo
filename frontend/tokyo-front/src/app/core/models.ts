export type Perfil = 'ADMIN' | 'USER';

export interface UsuarioResponse {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
  createdAt: string;
  updatedAt: string;
}

export interface EnderecoResponse {
  id: number;
  rua: string;
  numero: string;
  complemento: string | null;
  bairro: string;
  cidade: string;
  estado: string;
  cep: string;
  usuarioId: number;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface UsuarioCriarRequest {
  nome: string;
  email: string;
  senha: string;
}

export interface UsuarioAtualizarRequest {
  nome: string;
  email: string;
  senha?: string;
}

export interface EnderecoRequest {
  rua?: string;
  numero: string;
  complemento: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
  cep: string;
}

export type DashboardSection = 'overview' | 'users' | 'addresses';
