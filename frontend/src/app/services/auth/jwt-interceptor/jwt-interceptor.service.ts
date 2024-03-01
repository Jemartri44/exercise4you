import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../auth.service';

@Injectable({
  providedIn: 'root'
})
export class JwtInterceptorService implements HttpInterceptor{

  constructor(private authService:AuthService) { }
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let token:String= sessionStorage.getItem("token") || "";
    if(req.url.includes('login') || req.url.includes('register')){
      return next.handle(req);
    }
    if(token != ""){
      if(!req.url.includes('refresh-token')){
        this.authService.refreshToken();
        console.debug("req.url" + req.url);
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
