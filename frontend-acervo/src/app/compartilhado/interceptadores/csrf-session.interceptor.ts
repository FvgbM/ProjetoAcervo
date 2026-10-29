import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environment';

@Injectable()
export class CsrfSessionInterceptor implements HttpInterceptor {
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (!request.url.startsWith(environment.apiUrl)) return next.handle(request);
    let headers = request.headers;
    if (!['GET', 'HEAD', 'OPTIONS'].includes(request.method)) {
      const token = document.cookie.split('; ').find(item => item.startsWith('XSRF-TOKEN='))?.split('=')[1];
      if (token) headers = headers.set('X-XSRF-TOKEN', decodeURIComponent(token));
    }
    return next.handle(request.clone({ withCredentials: true, headers }));
  }
}