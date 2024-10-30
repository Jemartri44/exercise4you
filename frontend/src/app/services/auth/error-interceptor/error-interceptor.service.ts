import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, filter, map, throwError } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class ErrorInterceptorService implements HttpInterceptor{

  constructor(private router:Router) { }
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    console.debug('Petición interceptada');
    return next.handle(req).pipe(
      catchError(error => {
        console.debug('Error en la respuesta del servidor');
        console.debug(error);
        if (error instanceof HttpErrorResponse) {
          switch (error.status) {
            case 0:
              console.error('Se ha producido un error en la conexión con el servidor')
              return throwError(() => Error(error.error));
            case 401:
              console.error(error.error)
              if(error.error == "Bad credentials"){
                return throwError(() => Error('Usuario o contraseña incorrectos'));
              }
              if(error.error == "Invalid token"){
                return throwError(() => Error('Token no válido'));
              }
              if(error.error == "Expired token"){
                return throwError(() => Error('Enlace expirado. Por favor, solicite uno nuevo.'));
              }
              return throwError(() => Error(error.error));
            case 403:
              console.debug('Se ha recibido el código de error: ' + error.status + ' ' + error.error + ' ' + error.message);
              return throwError(() => Error('La sesión ha expirado. Por favor, inicie sesión nuevamente.', error.error));
            case 409:
              console.error('Se ha recibido el código de error: ' + error.status + ' ' + error.error + ' ' + error.message);
              return throwError(() => Error(error.error));
            default:
              console.error('Se ha recibido el código de error: ' + error.status + ' ' + error.error + ' ' + error.message);
              return throwError(() => new Error('Algo falló. Por favor inténtelo de nuevo.'));
          }
        }
        return throwError(() => new Error('Se ha recibido el código de error: ', error));
      })
    )
  }
}
