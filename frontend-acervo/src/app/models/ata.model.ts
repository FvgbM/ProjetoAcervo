export interface Ata {
  id?: number;
  numeroAta?: number;
  dataReuniao: string;
  pauta: string;
  participantes: string;
  status?: 'ATIVA' | 'EXCLUIDA' | 'RASCUNHO';
  liberadaParaAlunos?: boolean;
  criadoEm?: string;
  criadoPorNome?: string;
  possuiArquivo: boolean;
}

export interface AtaDTO {
  dataReuniao: string;
  pauta: string;
  participantes: string;
}