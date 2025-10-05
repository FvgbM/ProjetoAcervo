import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AutenticacaoService } from '../autenticacao.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
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