import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { AutenticacaoService } from '../autenticacao.service';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-trocar-senha',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf, RouterLink],
  templateUrl: './trocarSenha.component.html',
  styleUrls: ['./trocarSenha.component.css']
})
export class TrocarSenhaComponent {
  passoAtual: 'solicitacao' | 'redefinicao' | 'primeiroLogin' = 'solicitacao';
  formularioSolicitacao = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  formularioRedefinicao = this.fb.group({
    token: ['', Validators.required],
    novaSenha: ['', [Validators.required, Validators.minLength(8)]],
    confirmarSenha: ['', Validators.required]
  });

  formularioPrimeiroLogin = this.fb.group({
    novaSenha: ['', [Validators.required, Validators.minLength(8)]],
    confirmarSenha: ['', Validators.required]
  });

  mensagemSucesso: string | null = null;
  mensagemErro: string | null = null;
  carregando = false;
  emailSolicitacao: string | null = null;
  isPrimeiroLogin: boolean = false;

  constructor(
    private fb: FormBuilder,
    private autenticacaoService: AutenticacaoService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.extras.state?.['primeiroLogin']) {
      this.isPrimeiroLogin = true;
      this.passoAtual = 'primeiroLogin';
      this.emailSolicitacao = navigation.extras.state['email'];
    }
  }
  alterarSenhaPrimeiroLogin(): void {
    if (this.formularioPrimeiroLogin.invalid) {
      this.mensagemErro = 'Preencha todos os campos';
      return;
    }

    const { novaSenha, confirmarSenha } = this.formularioPrimeiroLogin.value;

    if (novaSenha !== confirmarSenha) {
      this.mensagemErro = 'Senhas não coincidem';
      return;
    }

    this.carregando = true;
    this.autenticacaoService.trocarSenha(
      this.emailSolicitacao!,
      'admin123',
      novaSenha!
    ).subscribe({
      next: () => {
        this.mensagemSucesso = 'Senha alterada com sucesso!';
        this.mensagemErro = null;
        setTimeout(() => this.router.navigate(['/arquivos']), 2000);
      },
      error: (erro: any) => {
        console.error('Erro ao alterar senha:', erro);
        this.mensagemErro = erro.error?.erro || 'Erro ao alterar senha';
        this.carregando = false;
      }
    });
  }

  solicitarRecuperacao(): void {
    if (this.formularioSolicitacao.invalid) return;

    this.carregando = true;
    const email = this.formularioSolicitacao.value.email!;

    this.autenticacaoService.solicitarRecuperacaoSenha(email).subscribe({
      next: () => {
        this.mensagemSucesso = `Instruções enviadas para ${email}`;
        this.mensagemErro = null;
        this.emailSolicitacao = email;
        this.passoAtual = 'redefinicao';
        this.carregando = false;
      },
      error: () => {
        this.mensagemErro = 'Erro ao solicitar recuperação';
        this.carregando = false;
      }
    });
  }

  redefinirSenha(): void {
    if (this.formularioRedefinicao.invalid) {
      this.mensagemErro = 'Preencha todos os campos';
      return;
    }

    if (this.formularioRedefinicao.value.novaSenha !== this.formularioRedefinicao.value.confirmarSenha) {
      this.mensagemErro = 'Senhas não coincidem';
      return;
    }

    this.carregando = true;
    const { token, novaSenha } = this.formularioRedefinicao.value;

    this.autenticacaoService.redefinirSenha(token!, novaSenha!).subscribe({
      next: () => {
        this.mensagemSucesso = 'Senha alterada com sucesso!';
        this.mensagemErro = null;
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (erro: any) => {
        console.error('Erro ao redefinir senha:', erro);
        this.mensagemErro = 'Token inválido ou expirado';
        this.carregando = false;
      }
    });
  }
  
}