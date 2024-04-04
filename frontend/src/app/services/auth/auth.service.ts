import { Injectable } from '@angular/core';
import { LoginRequest } from './loginRequest';
import { RegisterRequest } from './registerRequest';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, map, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment.development';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Router } from '@angular/router';

const helper = new JwtHelperService();

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  loggedIn:boolean = false;

  constructor( private http:HttpClient, private router:Router ) {
    this.checkToken();
  }

  onInit():void{
    
  }

  login(credentials:LoginRequest):Observable<any>{
    return this.http.post<any>(environment.hostUrl + "/auth/login", credentials).pipe(
      tap(userData => {
        sessionStorage.setItem("token", userData.token);
        this.loggedIn = true;
      }),
      map((userData) => userData.token)
    )
  }

  register(credentials:RegisterRequest):Observable<any>{
    return this.http.post<any>(environment.hostUrl + "/auth/register", credentials).pipe(
      tap(userData => {
        sessionStorage.setItem("token", userData.token);
        this.loggedIn = true;
      }),
      map((userData) => userData.token)
    )
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
    if (expired) {
      if (this.router.url == '/login' || this.router.url == '/login?expired=true' || this.router.url == '/register') {
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
}
