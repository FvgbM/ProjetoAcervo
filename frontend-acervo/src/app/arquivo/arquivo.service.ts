import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../environment';

export interface ArquivoInfo {
  id: number;
  nomeOriginal: string;
  tipoConteudo: string;
  tamanho: number;
  criptografado: boolean;
  dataEnvio: string;
  ataIdLong: number;
}

export interface ValidacaoArquivo {
  valido: boolean;
  tamanho: number;
  tipo: string;
  mensagem?: string;
}

@Injectable({ providedIn: 'root' })
export class ArquivoService {
  private readonly apiUrl = `${environment.apiUrl}/api/arquivos`;

  constructor(private readonly http: HttpClient) {}

  validar(file: File): Observable<ValidacaoArquivo> {
    const formData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post<ValidacaoArquivo>(
      `${this.apiUrl}/validar`,
      formData,
      { withCredentials: true }
    );
  }

  uploadParaAta(ataId: number, file: File): Observable<ArquivoInfo> {
    const formData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post<ArquivoInfo>(
      `${this.apiUrl}/upload/${ataId}`,
      formData,
      { withCredentials: true }
    );
  }

  download(ataId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/download/${ataId}`, {
      withCredentials: true,
      responseType: 'blob'
    });
  }
}