import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AtaService } from '../services/ata.service';
import { AutenticacaoService } from 'src/app/autenticacao/autenticacao.service';
import { Ata } from 'src/app/models/ata.model';

@Component({
  selector: 'app-visualizar-ata',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './visualizar.component.html',
  styleUrls: ['./visualizar.component.css']
})
export class VisualizarComponent implements OnInit {
  ata?: Ata;
  carregando = true;
  erro = '';

  constructor(
    private readonly ataService: AtaService,
    public readonly autenticacaoService: AutenticacaoService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void { this.carregarAta(); }

  carregarAta(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id <= 0) {
      this.erro = 'ID da ata inválido.';
      this.carregando = false;
      return;
    }

    this.ataService.buscarPorId(id).subscribe({
      next: (ata: Ata) => { this.ata = ata; this.carregando = false; },
      error: (erro: HttpErrorResponse) => {
        this.erro = erro.status === 404 ? 'Ata não encontrada.' : 'Não foi possível carregar a ata.';
        this.carregando = false;
      }
    });
  }

  voltar(): void { this.router.navigate(['/atas/listar']); }

  confirmarExclusao(): void {
    if (!this.ata?.id || !this.autenticacaoService.podeExcluirAtas()) return;
    if (!confirm(`Tem certeza que deseja excluir a Ata #${this.ata.numeroAta}?`)) return;

    this.ataService.excluir(this.ata.id).subscribe({
      next: () => this.router.navigate(['/atas/listar']),
      error: () => { this.erro = 'Não foi possível excluir a ata.'; }
    });
  }
}