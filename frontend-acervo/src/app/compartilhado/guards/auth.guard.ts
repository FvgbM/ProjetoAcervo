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

  const usuario = autenticacaoService.getUsuario();

  if (usuario?.status !== 'ATIVO') {
    router.navigate(['/conta-inativa']);
    return false;
  }

  if (autenticacaoService.isPrimeiroAcesso() && !state.url.includes('/trocarsenha')) {
    router.navigate(['/trocarsenha']);
    return false;
  }

  const perfilEsperado = route.data['perfil'];
  if (perfilEsperado) {
    if (usuario?.perfil !== perfilEsperado) {
      router.navigate(['/acesso-negado']);
      return false;
    }
  }
    return true;

  };
  
  