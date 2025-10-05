import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './header/header.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, HeaderComponent],
  template: `
    <app-header></app-header>
    <main>
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    main {
      padding: 20px;
    }
  `]
})
export class AppComponent {
  title = 'Sistema Acervo';

  constructor() {}

  ngOnInit() {
    console.log('AppComponent inicializado!');
    console.log('Rota atual:', window.location.pathname);
  }
}