import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AutenticacaoService } from '../autenticacao/autenticacao.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <header class="header">
      <div class="logo-container">
        <img 
          src="assets/logo_CPS2.png" 
          alt="Logo Sistema Acervo" 
          class="logo"
          (error)="handleImageError($event)">

      </div>
      
      <nav class="navigation" *ngIf="showNavigation">
        <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
        <a routerLink="/consultar" routerLinkActive="active">Consultar Atas</a>
        <a routerLink="/upload" routerLinkActive="active" *ngIf="isColaborador">Upload</a>
      </nav>

      <div class="user-info" *ngIf="usuario">
        <span>Olá, {{ usuario.nome }}</span>
        <button (click)="logout()">Sair</button>
      </div>
    </header>
  `,
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  usuario: any = null;
  showNavigation: boolean = false;
  isColaborador: boolean = false;

  constructor(private autenticacaoService: AutenticacaoService) {

    this.usuario = this.autenticacaoService.getCurrentUser();
    this.showNavigation = !!this.usuario;
    this.isColaborador = this.usuario?.papel === 'COLABORADOR';
  }

  handleImageError(event: any) {
    console.error('Erro ao carregar logo:', event);
    event.target.style.display = 'none';
  }

  logout() {
    this.autenticacaoService.logout();
  }
}

