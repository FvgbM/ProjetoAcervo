import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AutenticacaoService } from '../autenticacao/autenticacao.service';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})

export class DashboardComponent {
  usuario: any;
  isColaborador: boolean = false;

  constructor(
    private autenticacaoService: AutenticacaoService,
    private router: Router) {
    this.usuario = this.autenticacaoService.getUsuario();
    this.isColaborador = this.usuario?.perfil === 'COLABORADOR';

    if (!this.usuario) {
      this.router.navigate(['/login']);
    }  
  }

  logout() {
    this.autenticacaoService.logout();
    this.router.navigate(['/login']);
  }
}
