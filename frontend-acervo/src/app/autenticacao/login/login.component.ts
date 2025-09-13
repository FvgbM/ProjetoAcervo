import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AutenticacaoService } from '../autenticacao.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="login-wrapper">
      <div class="login-container">
        <div class="login-logo">
          <img 
            src="assets/logo_Fatec.png" 
            alt="Sistema Acervo" 
            class="logo-main"
          >
        </div>
        
        <form [formGroup]="formularioLogin" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <input 
              type="email" 
              formControlName="email" 
              placeholder="seu.email@fatec.sp.gov.br" 
              required
              class="form-control"
              [class.invalid]="email?.invalid && (email?.touched || email?.dirty)"
            >
            <div *ngIf="email?.invalid && (email?.touched || email?.dirty)" class="error-message">
              <span *ngIf="email?.errors?.['required']">Email é obrigatório</span>
              <span *ngIf="email?.errors?.['email']">Email inválido</span>
            </div>
          </div>
          
          <div class="form-group">
            <input 
              type="password" 
              formControlName="senha" 
              placeholder="Sua senha" 
              required
              class="form-control"
              [class.invalid]="senha?.invalid && (senha?.touched || senha?.dirty)"
            >
            <div *ngIf="senha?.invalid && (senha?.touched || senha?.dirty)" class="error-message">
              <span *ngIf="senha?.errors?.['required']">Senha é obrigatória</span>
            </div>
          </div>
          
          <button 
            type="submit" 
            class="btn btn-primary"
            [disabled]="formularioLogin.invalid || carregando"
          >
            {{ carregando ? 'Carregando...' : 'Entrar' }}
          </button>
        </form>
        
        <div *ngIf="primeiroLogin" class="alert alert-info">
          Primeiro login detectado. Você será redirecionado para alterar sua senha.
        </div>
        
        <div *ngIf="mensagemErro" class="alert alert-error">
          {{ mensagemErro }}
        </div>
      </div>
    </div>
  `,
  styleUrls: ['./login.component.css']
})

export class LoginComponent {
  formularioLogin = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', Validators.required]
  });

  mensagemErro: string | null = null;
  primeiroLogin = false;
  carregando = false;

  constructor(
    private fb: FormBuilder,
    private autenticacaoService: AutenticacaoService,
    private router: Router
  ) {}

  onSubmit(): void {
  if (this.formularioLogin.invalid || this.carregando) {
    return;
  }

  this.carregando = true;
  this.mensagemErro = null;
  
  const email = this.formularioLogin.value.email!;
  const senha = this.formularioLogin.value.senha!;

  console.log('Enviando para o backend:');
  console.log('Email:', email);
  console.log('Senha:', senha);

  this.autenticacaoService.login(email, senha).subscribe({
    next: (resposta: any) => {
      this.carregando = false;
      console.log('Resposta do login:', resposta);
      
      if (resposta.primeiroLogin) {
        this.primeiroLogin = true;
        
        this.router.navigate(['/alterar-senha'], {
          state: {
            email: resposta.email,
            nome: resposta.nome,
            primeiroLogin: true
          }
        });
      } else {
        localStorage.setItem('usuarioAtual', JSON.stringify({
          email: resposta.email,
          papel: resposta.papel,
          nome: resposta.nome
        }));
        
        this.router.navigate(['/arquivos']);
      }
    },
    error: (erro) => {
      console.error('Erro no login:', erro);
      this.carregando = false;
      this.mensagemErro = 'Credenciais inválidas. Verifique seu email e senha.';
    }
  });
}

  get email() {
    return this.formularioLogin.get('email');
  }

  get senha() {
    return this.formularioLogin.get('senha');
  }
}