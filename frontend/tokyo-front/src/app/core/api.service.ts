import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  EnderecoRequest,
  EnderecoResponse,
  PageResponse,
  UsuarioAtualizarRequest,
  UsuarioResponse,
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  users(
    page: number,
    sort: string,
    size = 10,
  ): Observable<PageResponse<UsuarioResponse>> {
    return this.http.get<PageResponse<UsuarioResponse>>(`${this.baseUrl}/usuarios`, {
      params: this.pageParams(page, sort, size),
    });
  }

  updateUser(id: number, request: UsuarioAtualizarRequest): Observable<UsuarioResponse> {
    return this.http.put<UsuarioResponse>(`${this.baseUrl}/usuarios/${id}`, request);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/usuarios/${id}`);
  }

  addresses(page: number, sort: string): Observable<PageResponse<EnderecoResponse>> {
    return this.http.get<PageResponse<EnderecoResponse>>(`${this.baseUrl}/enderecos`, {
      params: this.pageParams(page, sort),
    });
  }

  addressesForUser(
    userId: number,
    page: number,
    sort: string,
  ): Observable<PageResponse<EnderecoResponse>> {
    return this.http.get<PageResponse<EnderecoResponse>>(
      `${this.baseUrl}/usuarios/${userId}/enderecos`,
      { params: this.pageParams(page, sort) },
    );
  }

  createAddress(userId: number, request: EnderecoRequest): Observable<EnderecoResponse> {
    return this.http.post<EnderecoResponse>(
      `${this.baseUrl}/usuarios/${userId}/enderecos`,
      request,
    );
  }

  updateAddress(id: number, request: EnderecoRequest): Observable<EnderecoResponse> {
    return this.http.put<EnderecoResponse>(`${this.baseUrl}/enderecos/${id}`, request);
  }

  deleteAddress(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/enderecos/${id}`);
  }

  private pageParams(page: number, sort: string, size = 10): HttpParams {
    return new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
  }
}
