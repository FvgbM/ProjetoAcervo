import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './compartilhado/guards/auth.guard';
import { roleGuard } from './compartilhado/guards/role/guard/role.guard';



  export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./autenticacao/login/login.component').then(m => m.LoginComponent) },
  { path: 'trocarsenha', loadComponent: () => import('./autenticacao/trocarSenha/trocarSenha.component').then(c => c.TrocarSenhaComponent) },
  { path: 'dashboard', canActivate: [authGuard], loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent) },
  
  { path: 'atas/listar', canActivate: [authGuard], loadComponent: () => import('./atas/listar/listar.component').then(c => c.ListarComponent) },
  { path: 'atas/visualizar/:id', canActivate: [authGuard], loadComponent: () => import('./atas/visualizar/visualizar.component').then(c => c.VisualizarComponent) },
  { path: 'atas/criar', canActivate: [authGuard, roleGuard], data: { perfil: ['COLABORADOR'] }, loadComponent: () => import('./atas/enviar/enviar.component').then(c => c.EnviarComponent) },
  { path: 'atas/editar/:id', canActivate: [authGuard, roleGuard], data: { perfil: ['COLABORADOR'] }, loadComponent: () => import('./atas/enviar/enviar.component').then(c => c.EnviarComponent) },

  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: 'dashboard' },
  
  ];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule { }
