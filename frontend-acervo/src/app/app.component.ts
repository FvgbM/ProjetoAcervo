import { Component } from '@angular/core';


@Component({
  selector: 'app-root',
  template: `<app-header></app-header>
    <main class="main-content">
      <router-outlet></router-outlet>
    </main>`,
  styleUrls: ['./app.component.css']

})

export class AppComponent {
  title = 'frontend-acervo';
  }


