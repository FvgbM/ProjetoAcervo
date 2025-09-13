import { Component, OnInit } from '@angular/core';
import { ArquivoService } from '../../service/arquivo.service';

interface Arquivo {
  id: number;
  nomeOriginal: string;
  dataEnvio: string;
  enviadoPor: string;
}

@Component({
  selector: 'app-lista-arquivos',
  templateUrl: './lista-arquivos.component.html',
  styleUrls: ['./lista-arquivos.component.css']
})
export class ListaArquivosComponent implements OnInit {
  arquivos: Arquivo[] = [];
  carregando: boolean = false;
  erro: string = '';

  constructor(private arquivoService: ArquivoService) {}

  ngOnInit(): void {
    this.carregarArquivos();
  }

  carregarArquivos(): void {
    this.carregando = true;
    this.arquivoService.listarArquivos().subscribe({
      next: (response) => {
        this.arquivos = response;
        this.carregando = false;
      },
      error: (err) => {
        this.erro = 'Erro ao carregar arquivos: ' + err.error?.message || err.message;
        this.carregando = false;
      }
    });
  }

  downloadArquivo(id: number, nomeOriginal: string): void {
    this.arquivoService.downloadArquivo(id).subscribe({
      next: (response) => {
        const blob = new Blob([response.body!], { type: 'application/pdf' });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = nomeOriginal;
        link.click();
      },
      error: (err) => {
        this.erro = 'Erro ao baixar arquivo: ' + err.error?.message || err.message;
      }
    });
  }

  excluirArquivo(id: number): void {
    if (confirm('Tem certeza que deseja excluir este arquivo?')) {
      this.arquivoService.excluirArquivo(id).subscribe({
        next: () => {
          this.arquivos = this.arquivos.filter(a => a.id !== id);
        },
        error: (err) => {
          this.erro = 'Erro ao excluir arquivo: ' + err.error?.message || err.message;
        }
      });
    }
  }
}