import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../environment';

@Injectable({
  providedIn: 'root'
})
export class AutenticacaoService {
  private apiUrl = `${environment.apiUrl}/api/autenticacao`;
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const user = localStorage.getItem('currentUser');
    if (user) {
      this.currentUserSubject.next(JSON.parse(user));
    }
  }

  login(email: string, senha: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, 
      { email, senha }, {
        withCredentials: true,
        headers: new HttpHeaders({
          'Content-Type': 'application/json'
        })
      }).pipe(
      catchError(error => {
        console.error('Erro completo:', error);
        console.error('Status:', error.status);
        console.error('Mensagem:', error.message);
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  getCurrentUser(): any {
    return this.currentUserSubject.value;
  }

  trocarSenha(email: string, senhaAtual: string, novaSenha: string): Observable<any> {
    const url = `${this.apiUrl}/alterar-senha`;
    const body = { email, senhaAtual, novaSenha };

    return this.http.post(url, body).pipe(
      catchError(error => {
        console.error('Erro ao trocar senha', error);
        throw error;
      })
    );
  }


  redefinirSenha(token: string, novaSenha: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/redefinir-senha`, 
      { token, novaSenha },
      {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
      }
    ).pipe(
      catchError(error => {
        console.error('Erro ao redefinir senha', error);
        throw error;
      })
    );
  }

  solicitarRecuperacaoSenha(email: string): Observable<any> {
    const url = `${this.apiUrl}/solicitar-recuperacao`;
    return this.http.post(url, { email }).pipe(
      catchError(error => {
        console.error('Erro ao solicitar recuperação de senha', error);
        throw error;
      })
    );
  }

  estaLogado(): boolean {
    return !!this.getCurrentUser();
  }

  getUsuario(): any {
    return this.getCurrentUser();
  }
}