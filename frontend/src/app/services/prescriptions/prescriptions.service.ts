import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PrescriptionsListInfo } from '../../model/prescriptions/prescriptions-list-info';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.prod';
import { PossiblePrescriptionsRequest } from '../../model/prescriptions/possible-prescriptions-request';
import { PrescriptionRequest } from '../../model/prescriptions/prescription-request';
import { Prescription } from '../../model/prescriptions/prescription';
import { PrescriptionsResponse } from '../../model/prescriptions/prescriptions-response';

@Injectable({
  providedIn: 'root'
})
export class PrescriptionsService {

  constructor(private http:HttpClient) { }

  getPrescriptionsListInfo(patientId:string): Observable<PrescriptionsListInfo> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/prescripciones";
    return this.http.get<PrescriptionsListInfo>(url)
  }

  getPossiblePrescriptions(patientId:string, populationGroup:string, disease:string, level:string): Observable<Prescription[]> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/posibles-prescripciones";
    let request = {populationGroup: populationGroup, disease: disease, level: level} as PossiblePrescriptionsRequest;
    return this.http.post<Prescription[]>(url, request);
  }

  setPrescriptions(patientId:string, nSession:string, prescriptions:PrescriptionRequest[]): Observable<any> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/prescripciones/" + nSession + "/establecer-prescripciones";
    return this.http.post<any>(url, prescriptions);
  }

  getPrescriptions(patientId:string, nSession:string): Observable<PrescriptionsResponse> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/prescripciones/" + nSession + "/ver-prescripciones";
    return this.http.get<PrescriptionsResponse>(url);
  }
}
