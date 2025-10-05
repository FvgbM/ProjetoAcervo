import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AtaService } from '../services/ata.service';
import { AutenticacaoService } from 'src/app/autenticacao/autenticacao.service';
import { Ata, AtaDTO } from 'src/app/models/ata.model';

@Component({
  selector: 'app-enviar-ata',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: './enviar.component.html',
  styleUrls: ['./enviar.component.css']
})

export class EnviarComponent implements OnInit {

  ataDTO: AtaDTO = {
    dataReuniao: '',
    pauta: '',
    participantes: ''
  };
  
  ata?: Ata;
  ehEdicao: boolean = false;
  processando: boolean = false;
  submetido: boolean = false;
  mensagemErro: string = '';
  mensagemSucesso: string = '';

  constructor(
    private ataService: AtaService,
    private autenticacaoService: AutenticacaoService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.ehEdicao = true;
      this.carregarAtaParaEdicao(id);
    }
  }

  carregarAtaParaEdicao(id: number): void {
    this.processando = true;
    this.ataService.buscarPorId(id).subscribe({
      next: (ata) => {
        this.ata = ata;
        this.ataDTO = {
          dataReuniao: ata.dataReuniao,
          pauta: ata.pauta,
          participantes: ata.participantes || ''
        };
        this.processando = false;
      },
      error: (error) => {
        console.error('Erro ao carregar ata:', error);
        this.mensagemErro = 'Erro ao carregar ata para edição.';
        this.processando = false;
      }
    });
  }

  onSubmit(): void {
    this.submetido = true;
    this.mensagemErro = '';
    this.mensagemSucesso = '';

    if (!this.ataDTO.dataReuniao || !this.ataDTO.pauta || this.ataDTO.pauta.length < 10) {
      this.mensagemErro = 'Por favor, preencha todos os campos obrigatórios corretamente.';
      return;
    }

    const dataReuniao = new Date(this.ataDTO.dataReuniao);
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    
    if (dataReuniao > hoje) {
      this.mensagemErro = 'A data da reunião não pode ser futura.';
      return;
    }

    this.processando = true;

    if (this.ehEdicao && this.ata) {
      this.ataService.atualizarAta(this.ata.id!, this.ataDTO).subscribe({
        next: (ataAtualizada) => {
          this.processando = false;
          this.mensagemSucesso = 'Ata atualizada com sucesso!';
          setTimeout(() => {
            this.router.navigate(['/atas/listar']);
          }, 1500);
        },
        error: (error) => {
          console.error('Erro ao atualizar ata:', error);
          this.mensagemErro = this.obterMensagemErro(error);
          this.processando = false;
        }
      });
    } else {
      this.ataService.criarAta(this.ataDTO).subscribe({
        next: (novaAta) => {
          this.processando = false;
          this.mensagemSucesso = `Ata #${novaAta.numeroAta} criada com sucesso!`;
          setTimeout(() => {
            this.router.navigate(['/atas/listar']);
          }, 1500);
        },
        error: (error) => {
          console.error('Erro ao criar ata:', error);
          this.mensagemErro = this.obterMensagemErro(error);
          this.processando = false;
        }
      });
    }
  }

  private obterMensagemErro(error: any): string {
    if (error.error && typeof error.error === 'string') {
      return error.error;
    }
    if (error.status === 0) {
      return 'Erro de conexão. Verifique se o servidor está rodando.';
    }
    return 'Ocorreu um erro ao processar sua solicitação. Tente novamente.';
  }

  cancelar(): void {
    this.router.navigate(['/atas/listar']);
  }
}