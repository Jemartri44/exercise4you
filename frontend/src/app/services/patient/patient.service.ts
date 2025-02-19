import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { Patient } from '../../model/patient/patient';
import { environment } from '../../../environments/environment.prod';
import { PatientPage } from '../../model/patient/patient-page';
import { NewPatientRequest } from './newPatientRequest';
import { GeneralData } from '../../model/patient/general-data';
import { BiometricsData } from '../../model/patient/biometrics-data';
import { BiometricsAllData } from '../../model/patient/biometrics-all-data';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  constructor(private http:HttpClient) { }

  // Patient personal data

  getPatients(search: string, page: number = 0, size: number = 10):Observable<PatientPage>{
    
    let url = environment.apiUrl+"/pacientes?";
    if(search != ""){
      url = url + "search=" + search;
    }
    return this.http.get<PatientPage>(url + "&page=" + page + "&size=" + size).pipe(
    )
  }

  getPatient(id: string):Observable<Patient>{
    return this.http.get<Patient>(environment.apiUrl+"/paciente/"+id).pipe(
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

  addNewPatient(newPatient: NewPatientRequest):Observable<Patient>{
    return this.http.post<Patient>(environment.apiUrl+"/pacientes/nuevo", newPatient).pipe(
      catchError(this.handleError)
    )
  }

  editPatient(newPatient: NewPatientRequest, id:string):Observable<Patient>{
    return this.http.post<Patient>(environment.apiUrl+"/paciente/" + id + "/editar", newPatient).pipe(
      catchError(this.handleError)
    )
  }

  // Patient general data (personal + biometrics)

  getPatientGeneralData(patientId: string): Observable<GeneralData> {
    let url = environment.apiUrl+"/paciente/" + patientId + "/general-data";
    return this.http.get<GeneralData>(url)
  }

  getBiometricsData(patientId: string, session: string): Observable<BiometricsData> {
    let url = environment.apiUrl+"/paciente/" + patientId + "/biometrics-data/" + session;
    return this.http.get<BiometricsData>(url).pipe(
      catchError(this.handleError)
    )
  }

  getAllBiometricsData(patientId: string): Observable<BiometricsAllData> {
    let url = environment.apiUrl+"/paciente/" + patientId + "/all-biometrics-data";
    return this.http.get<BiometricsAllData>(url)
  }

  saveData(patientId: string, session: string, data: BiometricsData): Observable<BiometricsData> {
    console.debug("DATA: ")
    console.debug(data)
    console.debug(patientId);
    console.debug(session)
    let url = environment.apiUrl+"/paciente/" + patientId + "/save-data/" + session;
    return this.http.post<BiometricsData>(url, data)
  }

  public static round(value: number, decimals: number): number {
    if(decimals == 0) return Math.round(value + Number.EPSILON);
    let factor = Math.pow(10, decimals);
    return Math.round((value + Number.EPSILON) * factor) / factor;
  }
}
