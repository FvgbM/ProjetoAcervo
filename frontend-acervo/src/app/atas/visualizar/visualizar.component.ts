import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AtaService } from '../services/ata.service';
import { ArquivoService } from '../../arquivo/arquivo.service';
import { AutenticacaoService } from 'src/app/autenticacao/autenticacao.service';
import { Ata } from 'src/app/models/ata.model';

@Component({
  selector: 'app-visualizar-ata',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './visualizar.component.html',
  styleUrls: ['./visualizar.component.css']
})
export class VisualizarComponent implements OnInit, OnDestroy {
  ata?: Ata;
  pdfUrl?: SafeResourceUrl;
  carregando = true;
  carregandoPdf = false;
  erro = '';
  erroPdf = '';
  private objectUrl?: string;

  constructor(
    private readonly ataService: AtaService,
    private readonly arquivoService: ArquivoService,
    private readonly sanitizer: DomSanitizer,
    public readonly autenticacaoService: AutenticacaoService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void { this.carregarAta(); }
  ngOnDestroy(): void { this.limparPdf(); }

  carregarAta(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id <= 0) {
      this.erro = 'ID da ata inválido.';
      this.carregando = false;
      return;
    }

    this.ataService.buscarPorId(id).subscribe({
      next: (ata: Ata) => {
        this.ata = ata;
        this.carregando = false;
        if (ata.possuiArquivo) this.carregarPdf(id);
      },
      error: (erro: HttpErrorResponse) => {
        this.erro = erro.status === 404 ? 'Ata não encontrada.' : 'Não foi possível carregar a ata.';
        this.carregando = false;
      }
    });
  }

  private carregarPdf(ataId: number): void {
    this.carregandoPdf = true;
    this.erroPdf = '';
    this.limparPdf();

    this.arquivoService.download(ataId).subscribe({
      next: blob => {
        this.objectUrl = URL.createObjectURL(blob);
        this.pdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.objectUrl);
        this.carregandoPdf = false;
      },
      error: (erro: HttpErrorResponse) => {
        this.erroPdf = erro.status === 403
          ? 'Você não possui permissão para visualizar este PDF.'
          : 'Não foi possível carregar o PDF vinculado a esta ata.';
        this.carregandoPdf = false;
      }
    });
  }

  private limparPdf(): void {
    if (this.objectUrl) URL.revokeObjectURL(this.objectUrl);
    this.objectUrl = undefined;
    this.pdfUrl = undefined;
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
