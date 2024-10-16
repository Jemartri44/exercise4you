import { Component, OnInit } from '@angular/core';
import { HeaderComponent } from '../../../shared/header/header.component';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, startWith } from 'rxjs';
import { QuestionnaireService } from '../../../services/questionnaire/questionnaire.service';
import { QuestionnaireAnswers } from '../../../model/questionnaires/questionnaire-answers';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-questionnaire-answers',
  standalone: true,
  imports: [ HeaderComponent, NavbarComponent, FooterComponent, CommonModule ],
  templateUrl: './questionnaire-answers.component.html',
  styleUrl: './questionnaire-answers.component.css'
})
export class QuestionnaireAnswersComponent implements OnInit {
  title: string = '';
  answersState: Observable<{
    appState: string;
    appData?: any;
    appError?: string;
  }> | undefined;

  constructor( private router: Router, private questionnaireService: QuestionnaireService ) { }

  ngOnInit() {
    this.setTitle();
    this.answersState = this.questionnaireService.getQuestionnaireAnswers( this.router.url.split('/')[3], this.router.url.split('/')[2], this.router.url.split('/')[4]).pipe(
      map((questionnaireAnswers: QuestionnaireAnswers) => {
        if(questionnaireAnswers === undefined ) {
          throw new Error('No se ha podido obtener la información del cuestionario');
        }
        return ({ appState: 'LOADED', appData: questionnaireAnswers })
      }),
      startWith({ appState: 'LOADING', questionState: 'LOADING' }),
      catchError((error) => {
        console.error(error);
        return of({ appState: 'ERROR', questionState: 'ERROR', appError: error.message})
      })
    );
  }

  setTitle() {
    let questionnaire = this.router.url.split('/')[3];
    switch(questionnaire){
      case 'APALQ':
        this.title = 'Cuestionario de evaluación de los niveles de actividad física (APALQ)';
        break;
      case 'IPAQ':
        this.title = 'Cuestionario internacional de actividad física (IPAQ)';
        break;
      case 'IPAQ-E':
        this.title = 'Cuestionario internacional de actividad física en personas mayores (IPAQ-E)';
        break;
      case 'CMTCEF':
        this.title = 'Cuestionario del modelo transteórico del cambio de ejercicio físico';
        break;
      case 'PAR-Q':
        this.title = 'Cuestionario de aptitud para la actividad física para todos (PAR-Q+)';
        break;
      case 'ePARmed-X':
        this.title = 'Examen médico electrónico de aptitud para la actividad física (ePARmed-X+)';
        break;
    }
  }

  volver(){
    let routeParts = this.router.url.split('/');
    let route = routeParts[1] + '/' + routeParts[2] + '/' + routeParts[3];
    this.router.navigate([route])
  }
}
