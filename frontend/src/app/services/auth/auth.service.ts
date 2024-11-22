import { Injectable } from '@angular/core';
import { LoginRequest } from './loginRequest';
import { RegisterRequest } from './registerRequest';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, map, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment.development';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { ChangePasswordRequest } from './changePasswordRequest';

const helper = new JwtHelperService();

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  loggedIn:boolean = false;

  constructor( private http:HttpClient, private router:Router, private location:Location ) {
    this.checkToken();
  }

  onInit():void{
    
  }

  login(credentials:LoginRequest):Observable<any>{
    return this.http.post<any>(environment.hostUrl + "/auth/login", credentials).pipe(
      tap(userData => {
        sessionStorage.setItem("token", userData.token);
        this.loggedIn = true;
      })
    )
  }

  register(credentials:RegisterRequest):Observable<any>{
    return this.http.post<any>(environment.hostUrl + "/auth/register", credentials).pipe()
  }

  resendVerificationEmail(email:string):Observable<any>{
    return this.http.post<any>(environment.hostUrl + "/auth/refresh-verification-token", email).pipe()
  }

  checkVerificationToken(token:string):Observable<any>{
    return this.http.post<any>(environment.hostUrl + "/auth/email-verification", token).pipe()
  }

  forgottenPasswordEmail(email:string):Observable<any>{
    return this.http.post<any>(environment.hostUrl + "/auth/forgotten-password", email).pipe()
  }

  changePassword(request: ChangePasswordRequest):Observable<any>{
    return this.http.post<any>(environment.hostUrl + "/auth/change-password", request).pipe()
  }

  refreshToken():void{
    this.http.get<any>(environment.hostUrl + "/auth/refresh-token").pipe(
      tap(userData => {
        sessionStorage.setItem("token", userData.token);
        this.loggedIn = true;
      }),
      map((userData) => userData.token)
    ).subscribe();
  }

  logout(expiredToken: boolean = false):void{
    sessionStorage.removeItem("token");
    this.loggedIn = false;
    expiredToken ? this.router.navigate(['/login'], { queryParams: { expired: 'true' }}) : this.router.navigate(['/login']);
  }

  checkToken():void{
    const token = sessionStorage.getItem("token");
    const expired = helper.isTokenExpired(token);
    const authRoutes = ['/login','/register','/confirmar-registro','/solicitar-cambio-contrasena','/cambiar-contrasena','/politica-de-privacidad'];
    if (expired) {
      if (authRoutes.includes(this.location.path().split('?')[0])) {
        return;
      }
      this.logout(token != null);
    } else {
      this.loggedIn = true;
    }
  }

  get userLoggedIn():boolean{
    return this.loggedIn;
  }

  alertShown():Observable<boolean>{
    console.debug("Alert shown");
    return this.http.get<boolean>(environment.hostUrl+"/auth/alert-shown")
  }

}
