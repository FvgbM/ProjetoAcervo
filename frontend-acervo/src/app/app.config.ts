import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { routes } from './app.routes';
import { AuthInterceptor } from './compartilhado/interceptadores/auth.interceptor';
import { CsrfSessionInterceptor } from './compartilhado/interceptadores/csrf-session.interceptor';


export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: CsrfSessionInterceptor, multi: true },
    provideHttpClient(
      withInterceptorsFromDi()
    )
  ]
};