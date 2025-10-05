import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AutenticacaoService } from '../autenticacao/autenticacao.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  usuario: any = null;
  showNavigation: boolean = false;
  isColaborador: boolean = false;
  isAdmin: boolean = false;

  constructor(private autenticacaoService: AutenticacaoService) {}

    ngOnInit(): void {
    this.carregarUsuario();
  }

  carregarUsuario(): void {
    this.usuario = this.autenticacaoService.getUsuario();
    this.showNavigation = !!this.usuario;
    this.isColaborador = this.autenticacaoService.isColaborador();
    this.isAdmin = this.autenticacaoService.isAdmin();
  }

  getPerfilLabel(): string {
    const papeis: { [key: string]: string } = {
      'ALUNO': 'Aluno',
      'PROFESSOR': 'Professor',
      'COLABORADOR': 'Colaborador',
      'ADMIN': 'Administrador'
    };
    return papeis[this.usuario?.perfil || ''] || this.usuario?.perfil || '';
  }

  logout(): void {
    this.autenticacaoService.logout();
  }

  handleImageError(event: any): void {
    console.error('Erro ao carregar logo:', event);
    event.target.src = 'assets/logo-default.png';
  }
}


