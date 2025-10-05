
export interface Ata {
  id?: number;
  numeroAta: number;
  dataReuniao: string;
  pauta: string;
  participantes: string;
  criadoPor?: {
    id: number;
    nome: string;
    email: string;
  };
  criadoEm?: string;
  status: 'ATIVA' | 'EXCLUIDA' | 'RASCUNHO';
}

export interface AtaDTO {
  dataReuniao: string;
  pauta: string;
  participantes: string;
}