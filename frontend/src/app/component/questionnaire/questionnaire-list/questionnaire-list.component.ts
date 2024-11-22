import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { HeaderComponent } from '../../../shared/header/header.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { SessionButtonsComponent } from '../session-buttons/session-buttons.component';
import { Observable, catchError, map, of, startWith } from 'rxjs';
import { QuestionnaireListInfo } from '../../../model/questionnaires/questionnaire-list-info';
import { QuestionnaireService } from '../../../services/questionnaire/questionnaire.service';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-ipaq',
  standalone: true,
  imports: [ HeaderComponent, FooterComponent, NavbarComponent, SessionButtonsComponent, RouterModule, CommonModule ],
  templateUrl: './questionnaire-list.component.html',
  styleUrl: './questionnaire-list.component.css'
})
export class QuestionnaireListComponent implements OnInit {
  notCompletedButtons: string[] = ['toComplete'];
  completedAndRepeteableButtons: string[] = ['toComplete', 'answered'];
  completedButtons: string[] = ['toComplete', 'answered'];
  errorMessage: string = "";
  today: any;
  questionnairesState: Observable<{
    appState: string;
    appData?: QuestionnaireListInfo;
    appError?: string;
  }> | undefined;

  constructor(private questionnaireService: QuestionnaireService, private router: Router) { }

  ngOnInit(): void {
    this.questionnairesState = this.questionnaireService.getQuestionnaireListInfo( this.router.url.split('/')[3], this.router.url.split('/')[2]).pipe(
      map((questionnaireListInfo: QuestionnaireListInfo) => {
        
        if(questionnaireListInfo === undefined ) {
          throw new Error('No se ha podido obtener la información del cuestionario');
        }
        if(!questionnaireListInfo.allEmpty) {
          questionnaireListInfo.sessions.sort((a, b) =>  a.number < b.number ? 1 : -1 );
        }
        this.today = questionnaireListInfo.today;
        return ({ appState: 'LOADED', appData: questionnaireListInfo })
      }),
      startWith({ appState: 'LOADING' }),
      catchError((error) => {
        return of({ appState: 'ERROR', appError: error.message})
      })
    );
  }


}
