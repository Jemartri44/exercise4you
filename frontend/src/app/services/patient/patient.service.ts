import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { Patient } from '../../model/patient/patient';
import { environment } from '../../../environments/environment';
import { PatientPage } from '../../model/patient/patient-page';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  constructor(private http:HttpClient) { }

  getPatients(search: string, page: number = 0, size: number = 10):Observable<PatientPage>{
    
    let url = environment.apiUrl+"/pacientes?";
    if(search != ""){
      url = url + "search=" + search;
    }
    return this.http.get<PatientPage>(url + "&page=" + page + "&size=" + size).pipe(
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
