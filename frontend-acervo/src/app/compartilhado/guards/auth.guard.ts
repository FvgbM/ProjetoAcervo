import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AutenticacaoService } from 'src/app/autenticacao/autenticacao.service';

export const authGuard: CanActivateFn = (route, state) => {
  const autenticacaoService = inject(AutenticacaoService);
  const router = inject(Router);
  
  if (!autenticacaoService.estaLogado()) {
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }
  
  const expectedRole = route.data['papel'];
  if (expectedRole) {
    const usuario = autenticacaoService.getUsuario();
    if (usuario.papel !== expectedRole) {
      router.navigate(['/acesso-negado']);
      return false;
    }
  }
  
  return true;
};

