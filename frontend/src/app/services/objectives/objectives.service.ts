import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment.prod';
import { Observable } from 'rxjs';
import { ObjectivesListInfo } from '../../model/objectives/objectives-list-info';
import { Objective } from '../../model/objectives/objective';
import { PossibleObjectivesRequest } from '../../model/objectives/possible-objectives-request';
import { ObjectiveRequest } from '../../model/objectives/objective-request';
import { ObjectivesResponse } from '../../model/objectives/objectives-response';

@Injectable({
  providedIn: 'root'
})
export class ObjectivesService {

  constructor(private http:HttpClient) { }

  getObjectivesListInfo(patientId:string): Observable<ObjectivesListInfo> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/objetivos";
    return this.http.get<ObjectivesListInfo>(url)
  }

  getPossibleObjectives(patientId:string, populationGroup:string, disease:string): Observable<Objective[]> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/posibles-objetivos";
    let request = {populationGroup: populationGroup, disease: disease} as PossibleObjectivesRequest;
    console.debug(request);
    return this.http.post<Objective[]>(url, request);
  }

  setObjectives(patientId:string, nSession:string, objectives:ObjectiveRequest[]): Observable<any> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/objetivos/" + nSession + "/establecer-objetivos";
    return this.http.post<any>(url, objectives);
  }

  getObjectives(patientId:string, nSession:string): Observable<ObjectivesResponse> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/objetivos/" + nSession + "/ver-objetivos";
    return this.http.get<ObjectivesResponse>(url);
  }
}
