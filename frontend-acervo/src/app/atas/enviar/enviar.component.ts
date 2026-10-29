import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AtaService } from '../services/ata.service';
import { Ata, AtaDTO } from 'src/app/models/ata.model';

@Component({
  selector: 'app-enviar-ata',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './enviar.component.html',
  styleUrls: ['./enviar.component.css']
})
export class EnviarComponent implements OnInit {
  ataDTO: AtaDTO = { dataReuniao: '', pauta: '', participantes: '' };
  ata?: Ata;
  ehEdicao = false;
  processando = false;
  submetido = false;
  mensagemErro = '';
  mensagemSucesso = '';

  constructor(
    private readonly ataService: AtaService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (Number.isInteger(id) && id > 0) {
      this.ehEdicao = true;
      this.carregarAtaParaEdicao(id);
    }
  }

  carregarAtaParaEdicao(id: number): void {
    this.processando = true;
    this.ataService.buscarPorId(id).subscribe({
      next: (ata: Ata) => {
        this.ata = ata;
        this.ataDTO = {
          dataReuniao: ata.dataReuniao,
          pauta: ata.pauta,
          participantes: ata.participantes || ''
        };
        this.processando = false;
      },
      error: (erro: HttpErrorResponse) => {
        this.mensagemErro = this.obterMensagemErro(erro, 'Erro ao carregar ata para edição.');
        this.processando = false;
      }
    });
  }

  onSubmit(): void {
    this.submetido = true;
    this.mensagemErro = '';
    this.mensagemSucesso = '';
    const pauta = this.ataDTO.pauta.trim();

    if (!this.ataDTO.dataReuniao || pauta.length < 10) {
      this.mensagemErro = 'Preencha a data e uma pauta com pelo menos 10 caracteres.';
      return;
    }

    const dataReuniao = new Date(`${this.ataDTO.dataReuniao}T00:00:00`);
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    if (Number.isNaN(dataReuniao.getTime()) || dataReuniao > hoje) {
      this.mensagemErro = 'A data da reunião não pode ser futura.';
      return;
    }

    const payload: AtaDTO = {
      dataReuniao: this.ataDTO.dataReuniao,
      pauta,
      participantes: this.ataDTO.participantes?.trim() || ''
    };
    this.processando = true;

    if (this.ehEdicao && this.ata?.id) {
      this.ataService.atualizar(this.ata.id, payload).subscribe({
        next: () => this.concluir('Ata atualizada com sucesso!'),
        error: (erro: HttpErrorResponse) => this.falhar(erro)
      });
      return;
    }

    this.ataService.criar(payload).subscribe({
      next: (novaAta: Ata) => this.concluir(`Ata #${novaAta.numeroAta} criada com sucesso!`),
      error: (erro: HttpErrorResponse) => this.falhar(erro)
    });
  }

  cancelar(): void { this.router.navigate(['/atas/listar']); }

  private concluir(mensagem: string): void {
    this.processando = false;
    this.mensagemSucesso = mensagem;
    setTimeout(() => this.router.navigate(['/atas/listar']), 1200);
  }

  private falhar(erro: HttpErrorResponse): void {
    this.processando = false;
    this.mensagemErro = this.obterMensagemErro(erro, 'Não foi possível processar a ata.');
  }

  private obterMensagemErro(erro: HttpErrorResponse, fallback: string): string {
    if (typeof erro.error === 'string' && erro.error.trim()) return erro.error;
    if (erro.error && typeof erro.error.mensagem === 'string') return erro.error.mensagem;
    if (erro.status === 0) return 'Erro de conexão. Verifique se o servidor está disponível.';
    return fallback;
  }
}