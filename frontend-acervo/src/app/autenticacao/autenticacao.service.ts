import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';
import { environment } from '../environment';
import { Usuario } from '../models/usuario.model';


@Injectable({
  providedIn: 'root'
})
export class AutenticacaoService {
  private apiUrl = `${environment.apiUrl}/api/autenticacao`;
  private currentUserSubject = new BehaviorSubject<Usuario | null>(null);
  public currentUser = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.carregarUsuarioLocalStorage();
  }

  private carregarUsuarioLocalStorage(): void {
    const user = localStorage.getItem('currentUser');
    if (user) {
      try {
        this.currentUserSubject.next(JSON.parse(user));
      } catch (e) {
        console.error('Erro ao carregar usuário do localStorage:', e);
        this.limparSessao();
      }
    }
  }

  private obterCsrf(): Observable<unknown> {
    return this.http.get('${this.apiUrl}/csrf', { withCredentials: true});
  }

  login(email: string, senha: string): Observable<any> {
    return this.obterCsrf().pipe(
      switchMap(() => this.http.post('${this.apiUrl}/login', { email, senha },
        {
          withCredentials: true, headers: new HttpHeaders({ 'Content-Type': 'application/jason' })
        }
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
    this.limparSessao();
    this.http.post(`${this.apiUrl}/logout`, {}).subscribe();
  }

  private limparSessao(): void {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  getUsuario(): any {
  return this.currentUserSubject.value;
}

  estaLogado(): boolean {
    return !!this.getUsuario();
  }

  isColaborador(): boolean {
    const usuario = this.getUsuario();
    return usuario?.perfil === 'COLABORADOR';
  }

  isProfessor(): boolean {
    const usuario = this.getUsuario();
    return usuario?.perfil === 'PROFESSOR';
  }

  isAluno(): boolean {
    const usuario = this.getUsuario();
    return usuario?.perfil === 'ALUNO';
  }

  isAdmin(): boolean {
    const usuario = this.getUsuario();
    return usuario?.perfil === 'ADMIN';
  }

  getPerfilUsuario(): string {
    return this.getUsuario()?.perfil || '';
  }

  isPrimeiroAcesso(): boolean {
    const usuario = this.getUsuario();
    return usuario?.primeiroLogin || false;
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
    return this.obterCsrf().pipe(
      switchMap(() =>
        this.http.post(
          '${this.apiUrl}/redefinir-senha',
          { token, novaSenha },
          {
            withCredentials: true,
            headers: new HttpHeaders({
              'Content-Type': 'application/json'
            })
          }
        )
    ),
    catchError(error => {
      console.error('Erro ao redefinir a senha', error);
      return throwError(() => error);
    })
  );
  }

  solicitarRecuperacaoSenha(email: string): Observable<any> {
    return this.obterCsrf().pipe(
      switchMap(() =>
        this.http.post(
          '${this.apiUrl}/solicitar-recuperacao',
              { email },
        {
          withCredentials: true,
          headers: new HttpHeaders({
            'Content-Type': 'application/json',
          })
        }
      )
    ),
    catchError(error => {
      console.error('Erro ao solicitar a recuperacao de senha:', error);
      return throwError(() => error);
    })
  );
  }

  atualizarPerfilUsuario(usuarioAtualizado: Usuario): void {
    this.armazenarUsuario(usuarioAtualizado);
  }


  podeCriarAtas(): boolean {
    return this.isColaborador();
  }

  podeEditarAtas(): boolean {
    return this.isColaborador();
  }

  podeExcluirAtas(): boolean {
    return this.isColaborador();
  }

  podeGerenciarUsuarios(): boolean {
    return this.isColaborador() || this.isAdmin();
  }
}