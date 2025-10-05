import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AutenticacaoService } from 'src/app/autenticacao/autenticacao.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const autenticacaoService = inject(AutenticacaoService);
  const router = inject(Router);
  
  const usuario = autenticacaoService.getUsuario();
  
  if (!usuario) {
    router.navigate(['/login']);
    return false;
  }
  

  const perfilPermitido = route.data['perfil'] as string[];
  if (perfilPermitido && !perfilPermitido.includes(usuario.perfil)) {
    router.navigate(['/acesso-negado']);
    return false;
  }
  
  return true;
};