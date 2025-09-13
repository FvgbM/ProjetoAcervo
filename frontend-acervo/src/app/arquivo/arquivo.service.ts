import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environment';

@Injectable({
  providedIn: 'root'
})
export class ArquivoService {
  private apiUrl = `${environment.apiUrl}/api/arquivos`;

  constructor(private http: HttpClient) { }

  uploadArquivo(arquivo: File, usuarioId: number): Observable<any> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    formData.append('usuarioId', usuarioId.toString());

    return this.http.post(this.apiUrl, formData);
  }

  downloadArquivo(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`, {
      responseType: 'blob',
      observe: 'response'
    });
  }

  listarArquivos(): Observable<any> {
    return this.http.get(this.apiUrl);
  }

  excluirArquivo(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}