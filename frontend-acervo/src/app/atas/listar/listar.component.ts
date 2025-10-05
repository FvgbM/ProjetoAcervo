import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AtaService } from '../services/ata.service';
import { AutenticacaoService } from 'src/app/autenticacao/autenticacao.service';
import { Ata } from 'src/app/models/ata.model';

@Component({
  selector: 'app-listar-atas',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './listar.component.html',
  styleUrls: ['./listar.component.css']
})

export class ListarComponent implements OnInit {
  atas: Ata[] = [];
  atasFiltradas: Ata[] = [];
  carregando: boolean = true;
  termoBusca: string = '';
  dataInicio: string = '';
  dataFim: string = '';

  constructor(
    public autenticacaoService: AutenticacaoService,
    private ataService: AtaService
  ) {}

  ngOnInit(): void {
    this.carregarAtas();
  }

  carregarAtas(): void {
    this.carregando = true;
    this.ataService.listarAtas().subscribe({
      next: (atas) => {
        this.atas = atas;
        this.atasFiltradas = atas;
        this.carregando = false;
      },
      error: (error) => {
        console.error('Erro ao carregar atas:', error);
        this.carregando = false;
      }
    });
  }

  filtrarAtas(): void {
    if (!this.termoBusca) {
      this.atasFiltradas = this.atas;
      return;
    }

    const termo = this.termoBusca.toLowerCase();
    this.atasFiltradas = this.atas.filter(ata => ata.pauta.toLowerCase().includes(termo) || ata.participantes?.toLowerCase().includes(termo)
    );
  }

  filtrarPorData(): void {
    if (!this.dataInicio && !this.dataFim) {
      this.atasFiltradas = this.atas;
      return;
    }

    this.ataService.buscarPorPeriodo(this.dataInicio, this.dataFim).subscribe({
      next: (atas) => {
        this.atasFiltradas = atas;
      },
      error: (error) => {
        console.error('Erro ao filtrar por data:', error);
      }
    });
  }

  confirmarExclusao(ata: Ata): void {
    if (confirm(`Tem certeza que deseja excluir a Ata #${ata.numeroAta}?`)) {
      this.ataService.excluirAta(ata.id!).subscribe({
        next: () => {
          this.carregarAtas();
        },
        error: (error) => {
          console.error('Erro ao excluir ata:', error);
          alert('Erro ao excluir ata.');
        }
      });
    }
  }
}