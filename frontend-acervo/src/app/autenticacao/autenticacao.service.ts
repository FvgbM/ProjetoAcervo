import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';
import { environment } from '../environment';
import { Usuario } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class AutenticacaoService {
  private readonly apiUrl = `${environment.apiUrl}/api/autenticacao`;
  private readonly currentUserSubject = new BehaviorSubject<Usuario | null>(null);
  readonly currentUser = this.currentUserSubject.asObservable();

  constructor(private readonly http: HttpClient) {
    this.carregarUsuarioLocalStorage();
  }

  private carregarUsuarioLocalStorage(): void {
    const user = localStorage.getItem('currentUser');
    if (!user) return;
    try {
      this.currentUserSubject.next(JSON.parse(user));
    } catch {
      this.limparSessao();
    }
  }

  private obterCsrf(): Observable<unknown> {
    return this.http.get(`${this.apiUrl}/csrf`, { withCredentials: true });
  }

  login(email: string, senha: string): Observable<unknown> {
    return this.obterCsrf().pipe(
      switchMap(() => this.http.post(
        `${this.apiUrl}/login`,
        { email, senha },
        { withCredentials: true, headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
      )),
      tap((response: any) => {
        if (response?.usuario) this.armazenarUsuario(response.usuario);
      }),
      catchError(error => throwError(() => error))
    );
  }

  private armazenarUsuario(usuario: Usuario): void {
    localStorage.setItem('currentUser', JSON.stringify(usuario));
    this.currentUserSubject.next(usuario);
  }

  logout(): void {
    this.http.post(`${this.apiUrl}/logout`, {}, { withCredentials: true }).subscribe({
      complete: () => this.limparSessao(),
      error: () => this.limparSessao()
    });
  }

  private limparSessao(): void {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  getUsuario(): Usuario | null { return this.currentUserSubject.value; }
  estaLogado(): boolean { return !!this.getUsuario(); }
  isColaborador(): boolean { return this.getUsuario()?.perfil === 'COLABORADOR'; }
  isProfessor(): boolean { return this.getUsuario()?.perfil === 'PROFESSOR'; }
  isAluno(): boolean { return this.getUsuario()?.perfil === 'ALUNO'; }
  isAdmin(): boolean { return this.getUsuario()?.perfil === 'ADMIN'; }
  getPerfilUsuario(): string { return this.getUsuario()?.perfil || ''; }
  isPrimeiroAcesso(): boolean { return this.getUsuario()?.primeiroLogin || false; }

  trocarSenha(email: string, senhaAtual: string, novaSenha: string): Observable<unknown> {
    return this.obterCsrf().pipe(
      switchMap(() => this.http.post(`${this.apiUrl}/alterar-senha`, { email, senhaAtual, novaSenha }, { withCredentials: true })),
      catchError(error => throwError(() => error))
    );
  }

  redefinirSenha(token: string, novaSenha: string): Observable<unknown> {
    return this.obterCsrf().pipe(
      switchMap(() => this.http.post(`${this.apiUrl}/redefinir-senha`, { token, novaSenha }, { withCredentials: true })),
      catchError(error => throwError(() => error))
    );
  }

  solicitarRecuperacaoSenha(email: string): Observable<unknown> {
    return this.obterCsrf().pipe(
      switchMap(() => this.http.post(`${this.apiUrl}/solicitar-recuperacao`, { email }, { withCredentials: true })),
      catchError(error => throwError(() => error))
    );
  }

  atualizarPerfilUsuario(usuarioAtualizado: Usuario): void { this.armazenarUsuario(usuarioAtualizado); }
  podeCriarAtas(): boolean { return this.isColaborador() || this.isAdmin(); }
  podeEditarAtas(): boolean { return this.isColaborador() || this.isAdmin(); }
  podeExcluirAtas(): boolean { return this.isAdmin(); }
  podeGerenciarUsuarios(): boolean { return this.isAdmin(); }
}
