import { Injectable } from '@angular/core';
import { LoginRequest } from './loginRequest';
import { RegisterRequest } from './registerRequest';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, map, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment.development';
import { JwtHelperService } from '@auth0/angular-jwt';

const helper = new JwtHelperService();

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  loggedIn:boolean = false;

  constructor(private http:HttpClient) {
    this.checkToken();
  }

  onInit():void{
    
  }

  login(credentials:LoginRequest):Observable<any>{
    return this.http.post<any>(environment.hostUrl + "/auth/login", credentials).pipe(
      tap(userData => {
        console.debug("Login token: " + userData.token);
        sessionStorage.setItem("token", userData.token);
        this.loggedIn = true;
      }),
      map((userData) => userData.token)
    )
  }

  register(credentials:RegisterRequest):Observable<any>{
    return this.http.post<any>(environment.hostUrl + "/auth/register", credentials).pipe(
      tap(userData => {
        console.debug("Register token: " + userData.token);
        sessionStorage.setItem("token", userData.token);
        this.loggedIn = true;
      }),
      map((userData) => userData.token)
    )
  }

  refreshToken():void{
    console.debug("Refreshing token");
    this.http.get<any>(environment.hostUrl + "/auth/refresh-token").pipe(
      tap(userData => {
        console.debug("Refreshing token: " + userData.token);
        sessionStorage.setItem("token", userData.token);
        this.loggedIn = true;
      }),
      map((userData) => userData.token)
    ).subscribe();
  }

  logout():void{
    sessionStorage.removeItem("token");
    this.loggedIn = false;
  }

  private checkToken():void{
    const token = sessionStorage.getItem("token");
    helper.isTokenExpired(token) ? this.logout() : this.loggedIn = true;
  }

  get userLoggedIn():boolean{
    return this.loggedIn;
  }
}
