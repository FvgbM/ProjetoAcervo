import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ata, AtaDTO } from '../../models/ata.model';

@Injectable({
  providedIn: 'root'
})
export class AtaService {
  private apiUrl = 'http://localhost:8080/api/atas';

  constructor(private http: HttpClient) { }

  criarAta(ata: AtaDTO): Observable<Ata> {
    return this.http.post<Ata>(this.apiUrl, ata);
  }

  listarAtas(): Observable<Ata[]> {
    return this.http.get<Ata[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Ata> {
    return this.http.get<Ata>(`${this.apiUrl}/${id}`);
  }

  buscarPorPeriodo(inicio: string, fim: string): Observable<Ata[]> {
    return this.http.get<Ata[]>(`${this.apiUrl}/periodo?inicio=${inicio}&fim=${fim}`);
  }

  buscarPorPauta(trechoPauta: string): Observable<Ata[]> {
    return this.http.get<Ata[]>(`${this.apiUrl}/busca?pauta=${trechoPauta}`);
  }

  atualizarAta(id: number, ata: AtaDTO): Observable<Ata> {
    return this.http.put<Ata>(`${this.apiUrl}/${id}`, ata);
  }

  excluirAta(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}