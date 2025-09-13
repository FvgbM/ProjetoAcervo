import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AutenticacaoService } from '../autenticacao/autenticacao.service';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard">
      <header>
        <h1>Sistema Acervo</h1>
        <button (click)="logout()">Sair</button>
      </header>
      
      <main>
        <h2>Bem-vindo, {{ usuario?.email }}</h2>
        <p>Seu perfil: {{ usuario?.papel }}</p>
        
        <div class="dashboard-actions">
          <button routerLink="/consultar" class="action-btn">
            📋 Consultar Atas
          </button>
          
          <button *ngIf="isColaborador" routerLink="/upload" class="action-btn">
            ⬆️ Fazer Upload
          </button>
        </div>
      </main>
    </div>
  `

})

export class DashboardComponent {
  usuario: any;
  isColaborador: boolean = false;

  constructor(
    private autenticacaoService: AutenticacaoService,
    private router: Router) {
    this.usuario = this.autenticacaoService.getUsuario();
    this.isColaborador = this.usuario?.papel === 'COLABORADOR';

    if (!this.usuario) {
      this.router.navigate(['/login']);
    }  
  }

  logout() {
    this.autenticacaoService.logout();
    this.router.navigate(['/login']);
  }
}
