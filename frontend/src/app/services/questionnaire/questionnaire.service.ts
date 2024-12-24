import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, map, of, throwError } from 'rxjs';
import { QuestionnaireListInfo } from '../../model/questionnaires/questionnaire-list-info';
import { PatientPage } from '../../model/patient/patient-page';
import { environment } from '../../../environments/environment.prod';
import { QuestionnaireInfo } from '../../model/questionnaires/questionnaire-info';
import { QuestionnaireAnswers } from '../../model/questionnaires/questionnaire-answers';

@Injectable({
  providedIn: 'root'
})
export class QuestionnaireService {

  constructor(private http:HttpClient) { }

  getQuestionnaireListInfo(questionnaireType: string, patientId:string): Observable<QuestionnaireListInfo> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/" + questionnaireType;
    return this.http.get<QuestionnaireListInfo>(url)
  }

  getQuestionnaireInfo(questionnaireType: string, patientId: string, session: string): Observable<QuestionnaireInfo> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/" + questionnaireType + "/" + session + "/start";
    return this.http.get<QuestionnaireInfo>(url)
  }

  getNextQuestion(questionnaireType: string, patientId: string, session: string, questionCode: string, question: string, answer: string): Observable<QuestionnaireInfo["question"]> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/" + questionnaireType + "/" + session + "/next";
    return this.http.post<QuestionnaireInfo["question"]>(url, {"questionCode": questionCode, "question": question, "answer": answer})
  }

  repeatQuestionnaire(questionnaireType: string, patientId: string, session: string): Observable<QuestionnaireInfo> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/" + questionnaireType + "/" + session + "/repeat";
    return this.http.get<QuestionnaireInfo>(url)
  }

  getQuestionnaireAnswers(questionnaireType: string, patientId: string, session: string): Observable<QuestionnaireAnswers> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/" + questionnaireType + "/" + session + "/get-answers";
    return this.http.get<QuestionnaireAnswers>(url)
  }

  setWeight(questionnaireType: string, patientId: string, session: string, weight: number): Observable<boolean> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/" + questionnaireType + "/" + session + "/set-weight";
    let response = this.http.post<void>(url, weight);
    return response.pipe(
      map(() => true),
      catchError(() => of(false))
    );
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
