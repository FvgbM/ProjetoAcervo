import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { AtaService } from '../services/ata.service';
import { AutenticacaoService } from 'src/app/autenticacao/autenticacao.service';
import { Ata } from 'src/app/models/ata.model';

@Component({
  selector: 'app-visualizar-ata',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './visualizar.component.html',
  styleUrls: ['./visualizar.component.css']
})
export class VisualizarComponent implements OnInit {
  ata?: Ata;
  carregando: boolean = true;
  erro: string = '';

  constructor(
    private ataService: AtaService,
    public autenticacaoService: AutenticacaoService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.carregarAta();
  }

  carregarAta(): void {
    const id = this.route.snapshot.params['id'];
    if (!id) {
      this.erro = 'ID da ata não especificado.';
      this.carregando = false;
      return;
    }

    this.ataService.buscarPorId(+id).subscribe({
      next: (ata) => {
        this.ata = ata;
        this.carregando = false;
      },
      error: (error) => {
        console.error('Erro ao carregar ata:', error);
        this.erro = 'Erro ao carregar ata. Verifique se ela existe.';
        this.carregando = false;
      }
    });
  }

  voltar(): void {
    this.router.navigate(['/atas/listar']);
  }

  confirmarExclusao(): void {
    if (!this.ata) return;

    if (confirm(`Tem certeza que deseja excluir a Ata #${this.ata.numeroAta}? Esta ação não pode ser desfeita.`)) {
      this.ataService.excluirAta(this.ata.id!).subscribe({
        next: () => {
          this.router.navigate(['/atas/listar']);
        },
        error: (error) => {
          console.error('Erro ao excluir ata:', error);
          alert('Erro ao excluir ata.');
        }
      });
    }
  }
}