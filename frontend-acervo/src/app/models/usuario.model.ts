export interface Usuario {
  id?: number;
  email: string;
  nome: string;
  perfil: 'ALUNO' | 'PROFESSOR' | 'COLABORADOR' | 'ADMIN';
  status: 'ATIVO' | 'INATIVO' | 'BLOQUEADO' | 'PENDENTE';
  primeiroLogin?: boolean;
  token?: string;
}

export function usuarioPodeCriarAtas(usuario: Usuario): boolean {
  return usuario.perfil === 'COLABORADOR';
}

export function usuarioPodeGerenciarUsuarios(usuario: Usuario): boolean {
  return usuario.perfil === 'COLABORADOR' || usuario.perfil === 'ADMIN';
}