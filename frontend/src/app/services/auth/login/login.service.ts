import { Injectable } from '@angular/core';
import { LoginRequest } from './loginRequest';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  constructor(private http:HttpClient) { }

  login(credentials:LoginRequest):Observable<any>{
    return this.http.get('../../../assets/data.json').pipe(
      catchError(this.handleError)
    )
  }

  private handleError(error: HttpErrorResponse){
    if(error.status == 0 ){
      console.error('Se ha producido un error en la conexión con el servidor', error.error);
    }else{
      console.error('Se ha recibido el código de error: ', error.status, error.error);
    }
    return throwError(() => new Error('Algo falló. Por favor inténtelo de nuevo.'));
  }
}
