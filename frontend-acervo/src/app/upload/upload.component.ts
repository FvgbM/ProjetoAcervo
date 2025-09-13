import { Component } from '@angular/core';
import { ArquivoService } from '../arquivo/arquivo.service';
import { AutenticacaoService } from '../autenticacao/autenticacao.service';
import { NgIf } from '@angular/common';

@Component({
    selector: 'app-upload',
    templateUrl: './upload.component.html',
    styleUrls: ['./upload.component.css'],
    standalone: true,
    imports: [NgIf]
})
export class UploadComponent {
  arquivoSelecionado: File | null = null;
  mensagem: string = '';
  erro: string = '';

  constructor(
    private arquivoService: ArquivoService,
    private autenticacaoService: AutenticacaoService
  ) {}

  onFileSelected(event: any): void {
    this.arquivoSelecionado = event.target.files[0] as File;
  }

  uploadArquivo(): void {
    if (!this.arquivoSelecionado) {
      this.erro = 'Selecione um arquivo primeiro';
      return;
    }

    const usuario = this.autenticacaoService.getCurrentUser();
    if (!usuario) {
      this.erro = 'Usuário não autenticado';
      return;
    }

    this.arquivoService.uploadArquivo(this.arquivoSelecionado, usuario.id)
      .subscribe({
        next: (response) => {
          this.mensagem = 'Arquivo enviado com sucesso!';
          this.erro = '';
          this.arquivoSelecionado = null;
          const fileInput = document.getElementById('fileInput') as HTMLInputElement;
          if (fileInput) fileInput.value = '';
        },
        error: (err) => {
          this.erro = 'Erro ao enviar arquivo: ' + err.error?.message || err.message;
          this.mensagem = '';
        }
      });
  }
}