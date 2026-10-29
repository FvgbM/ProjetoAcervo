import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { Ata } from '../models/ata.model';
import { AtaService } from '../atas/services/ata.service';
import { ArquivoService, ValidacaoArquivo } from '../arquivo/arquivo.service';

@Component({
  selector: 'app-upload',
  standalone: true,
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.css'],
  imports: [CommonModule, ReactiveFormsModule, RouterLink]
})
export class UploadComponent implements OnInit {
  readonly limiteBytes = 10 * 1024 * 1024;

  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

  readonly uploadForm = this.fb.group({
    ataId: ['', Validators.required]
  });

  arquivoSelecionado: File | null = null;
  atasSemArquivo: Ata[] = [];
  isDragover = false;
  processando = false;
  submetido = false;
  mensagemErro = '';
  mensagemSucesso = '';
  erroArquivo = '';
  arquivoValidado = false;

  private sequenciaValidacao = 0;

  constructor(
    private readonly fb: FormBuilder,
    private readonly router: Router,
    private readonly atas: AtaService,
    private readonly arquivos: ArquivoService
  ) {}

  ngOnInit(): void {
    this.carregarAtasSemArquivo();
  }

  carregarAtasSemArquivo(): void {
    this.atas.listar().subscribe({
      next: atas => {
        this.atasSemArquivo = atas.filter(ata => !ata.possuiArquivo);
      },
      error: () => {
        this.mensagemErro = 'Não foi possível carregar as atas disponíveis.';
      }
    });
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragover = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragover = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragover = false;

    const arquivos = event.dataTransfer?.files;
    if (!arquivos || arquivos.length !== 1) {
      this.erroArquivo = 'Selecione somente um arquivo PDF.';
      return;
    }
    this.processarArquivo(arquivos[0]);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const arquivo = input.files?.item(0);
    if (arquivo) {
      this.processarArquivo(arquivo);
    }
  }

  processarArquivo(file: File): void {
    const requisicaoAtual = ++this.sequenciaValidacao;
    this.erroArquivo = '';
    this.arquivoSelecionado = null;
    this.arquivoValidado = false;

    if (!file.name.toLowerCase().endsWith('.pdf') || file.type !== 'application/pdf') {
      this.erroArquivo = 'Apenas arquivos PDF são permitidos.';
      return;
    }
    if (file.size === 0 || file.size > this.limiteBytes) {
      this.erroArquivo = 'O PDF deve ter até 10 MB e não pode estar vazio.';
      return;
    }

    this.arquivos.validar(file).subscribe({
      next: (resposta: ValidacaoArquivo) => {
        if (requisicaoAtual !== this.sequenciaValidacao) return;
        if (!resposta.valido) {
          this.erroArquivo = resposta.mensagem || 'O servidor recusou este PDF.';
          return;
        }
        this.arquivoSelecionado = file;
        this.arquivoValidado = true;
      },
      error: erro => {
        if (requisicaoAtual !== this.sequenciaValidacao) return;
        this.erroArquivo = this.mensagemDoErro(erro, 'O servidor recusou este PDF.');
      }
    });
  }

  onSubmit(): void {
    this.submetido = true;
    this.limparMensagens();

    if (!this.arquivoSelecionado || !this.arquivoValidado || this.uploadForm.invalid) {
      this.mensagemErro = 'Selecione uma ata e um PDF válido para continuar.';
      return;
    }

    const ataId = Number(this.uploadForm.controls.ataId.value);
    if (!Number.isInteger(ataId) || ataId <= 0) {
      this.mensagemErro = 'Selecione uma ata válida.';
      return;
    }

    this.processando = true;
    this.arquivos.uploadParaAta(ataId, this.arquivoSelecionado).subscribe({
      next: () => {
        this.processando = false;
        this.mensagemSucesso = 'PDF enviado e vinculado à ata com sucesso.';
        this.carregarAtasSemArquivo();
        this.uploadForm.reset();
        this.removerArquivo();
      },
      error: erro => {
        this.processando = false;
        this.mensagemErro = this.mensagemDoErro(erro, 'Não foi possível enviar o arquivo.');
      }
    });
  }

  removerArquivo(event?: Event): void {
    event?.stopPropagation();
    ++this.sequenciaValidacao;
    this.arquivoSelecionado = null;
    this.arquivoValidado = false;
    this.erroArquivo = '';
    if (this.fileInput) this.fileInput.nativeElement.value = '';
  }

  cancelar(): void {
    this.router.navigate(['/atas/listar']);
  }

  private limparMensagens(): void {
    this.mensagemErro = '';
    this.mensagemSucesso = '';
  }

  private mensagemDoErro(erro: { error?: { mensagem?: string } | string }, padrao: string): string {
    const corpo = erro.error;
    return typeof corpo === 'string' ? corpo : corpo?.mensagem || padrao;
  }
}