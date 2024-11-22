import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PdfService {

  constructor(private http:HttpClient) { }

  getPdf(id: string, pdfType: string, session: number) {
    return this.http.get(environment.apiUrl + "/pdf/" + id + "/" + pdfType + "/" + session, {observe: 'response', responseType: 'blob'});
  }

  getManual() {
    return this.http.get(environment.apiUrl + "/manual", {observe: 'response', responseType: 'blob'});
  }
}
