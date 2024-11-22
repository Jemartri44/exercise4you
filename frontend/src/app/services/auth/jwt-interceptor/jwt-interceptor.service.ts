import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../auth.service';
import { Location } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class JwtInterceptorService implements HttpInterceptor{

  constructor(private authService:AuthService, private location:Location) { }
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let token:String= sessionStorage.getItem("token") || "";
    const authRoutes = ['login','register','confirmar-registro','/solicitar-cambio-contrasena','cambiar-contrasena','politica-de-privacidad'];
    if(authRoutes.includes(this.location.path().split('/')[1].split('?')[0])){
      return next.handle(req);
    }
    if(token != ""){
      this.authService.checkToken();
      if(!req.url.includes('refresh-token')){
        this.authService.refreshToken();
      }
      req = req.clone({
        setHeaders: {
          'Content-Type': 'application/json;charset=utf-8',
          'Accept': 'application/json',
          'Authorization': 'Bearer ' + token
        }
      })
    }
    return next.handle(req);
  }
}
