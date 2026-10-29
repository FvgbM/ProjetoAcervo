import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environment';
import { Ata, AtaDTO } from '../../models/ata.model';

export type AtaPayload = AtaDTO;

@Injectable({ providedIn: 'root' })
export class AtaService {
  private readonly apiUrl = `${environment.apiUrl}/api/atas`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<Ata[]> {
    return this.http.get<Ata[]>(this.apiUrl, { withCredentials: true });
  }

  buscarPorId(id: number): Observable<Ata> {
    return this.http.get<Ata>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  criar(payload: AtaPayload): Observable<Ata> {
    return this.http.post<Ata>(this.apiUrl, payload, { withCredentials: true });
  }

  atualizar(id: number, payload: AtaPayload): Observable<Ata> {
    return this.http.put<Ata>(`${this.apiUrl}/${id}`, payload, { withCredentials: true });
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  buscarPorPeriodo(inicio: string, fim: string): Observable<Ata[]> {
    const params = new HttpParams().set('inicio', inicio).set('fim', fim);
    return this.http.get<Ata[]>(`${this.apiUrl}/periodo`, { params, withCredentials: true });
  }

  buscarPorPauta(trechoPauta: string): Observable<Ata[]> {
    const params = new HttpParams().set('trechoPauta', trechoPauta);
    return this.http.get<Ata[]>(`${this.apiUrl}/busca`, { params, withCredentials: true });
  }

  definirLiberacaoAlunos(id: number, liberada: boolean): Observable<Ata> {
    const params = new HttpParams().set('liberada', String(liberada));
    return this.http.patch<Ata>(
      `${this.apiUrl}/${id}/liberacao-alunos`,
      null,
      { params, withCredentials: true }
    );
  }

  listarAtas(): Observable<Ata[]> { return this.listar(); }
  criarAta(dto: AtaDTO): Observable<Ata> { return this.criar(dto); }
  atualizarAta(id: number, dto: AtaDTO): Observable<Ata> { return this.atualizar(id, dto); }
  excluirAta(id: number): Observable<void> { return this.excluir(id); }
}