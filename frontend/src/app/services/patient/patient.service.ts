import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { Patient } from '../../model/patient';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  constructor(private http:HttpClient) { }

  getPatients():Observable<Patient[]>{
    
    let url = environment.apiUrl+"/pacientes";
    return this.http.get<Patient[]>(url).pipe(
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
