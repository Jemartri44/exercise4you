import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { HeaderComponent } from '../../../shared/header/header.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { QuestionnaireService } from '../../../services/questionnaire/questionnaire.service';
import { Observable, catchError, map, of, startWith } from 'rxjs';
import { QuestionnaireInfo } from '../../../model/questionnaires/questionnaire-info';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { QuestionButtonsComponent } from '../question-buttons/question-buttons.component';

declare let $:any;

@Component({
  selector: 'app-questionnaire-complete',
  standalone: true,
  imports: [ HeaderComponent, NavbarComponent, FooterComponent, CommonModule, ReactiveFormsModule, QuestionButtonsComponent ],
  templateUrl: './questionnaire-complete.component.html',
  styleUrl: './questionnaire-complete.component.css'
})
export class QuestionnaireCompleteComponent implements OnInit{

  title: string = '';
  question: QuestionnaireInfo["question"];
  alert: QuestionnaireInfo["alert"] | undefined = undefined;
  alreadyExists: boolean = false;
  @ViewChild('modalAlert') modalAlert: ElementRef;
  @ViewChild('modalAlreadyExists') modalAlreadyExists: ElementRef;
  @ViewChild('modalRepeat') modalRepeat: ElementRef;
  @ViewChild('questionButtons') questionButtons: QuestionButtonsComponent;
  questionnaireState: Observable<{
    appState: string;
    questionState: string;
    appData?: any;
    appError?: string;
  }> | undefined;
  questionnaireForm = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    password: ['', [Validators.required, Validators.maxLength(128), Validators.minLength(8)]]
  });
  modalShowed: boolean = true;

  constructor( private router: Router, private questionnaireService: QuestionnaireService, private formBuilder: FormBuilder ) { }

  ngOnInit() {
    this.setTitle();
    if(this.router.url.split('/')[5] === 'repetir') {
      this.repeatQuestionnaire();
      //console.debug('pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + '/' + this.router.url.split('/')[4] + '/completar');
      //this.router.navigateByUrl('/RefreshComponent', { skipLocationChange: true }).then(() => {
      //  this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + '/' + this.router.url.split('/')[4] + '/completar']); 
      return;
    }
    this.questionnaireState = this.questionnaireService.getQuestionnaireInfo( this.router.url.split('/')[3], this.router.url.split('/')[2], this.router.url.split('/')[4]).pipe(
      map((questionnaireInfo: QuestionnaireInfo) => {
        if(questionnaireInfo === undefined ) {
          throw new Error('No se ha podido obtener la información del cuestionario');
        }
        this.question = questionnaireInfo.question;
        if(questionnaireInfo.question.code === "end") {
          return ({ appState: 'END', questionState: 'END', appData: questionnaireInfo })
        }
        this.alreadyExists = questionnaireInfo.alreadyExists;
        this.modalShowed = false;
        this.alert = questionnaireInfo.alert;
        return ({ appState: 'LOADED', questionState: 'LOADED', appData: questionnaireInfo })
      }),
      startWith({ appState: 'LOADING', questionState: 'LOADING' }),
      catchError((error) => {
        console.error(error);
        return of({ appState: 'ERROR', questionState: 'ERROR', appError: error.message})
      })
    );
  }

  ngAfterViewInit() {
    if(this.router.url.split('/')[5] === 'repetir') {
      return;
    }
    this.questionnaireState?.subscribe((state) => {
      if(state.appState === 'LOADED' && state.questionState === 'LOADED' && !this.modalShowed) {
        if(this.alreadyExists) {
          this.showModal("alreadyExists");
        } else {
          this.showModal("alert");
        }
      }
    });
  }

  setTitle() {
    let questionnaire = this.router.url.split('/')[3];
    switch(questionnaire){
      case 'IPAQ':
        this.title = 'Cuestionario internacional de actividad física (IPAQ)';
        break;
    }
  }

  showModal(modal: string) {
    this.modalShowed = false;
    if(modal === "alreadyExists") {
      $(this.modalAlreadyExists.nativeElement).modal('show');
    }
    if(modal === "alert") {
      $(this.modalAlert.nativeElement).modal('show');
    }
    if(modal === "repeat") {
      $(this.modalRepeat.nativeElement).modal('show');
    }
  }

  submit() {
    let answer = this.questionButtons.checkAnswer();
    if(answer === undefined) {
      return;
    }
    console.debug(answer);
    this.questionnaireState = this.questionnaireService.getNextQuestion( this.router.url.split('/')[3], this.router.url.split('/')[2], this.router.url.split('/')[4], this.question.code, this.question.question, answer).pipe(
      map((question: QuestionnaireInfo["question"]) => {
        console.debug(question);
        if(question === undefined ) {
          throw new Error('No se ha podido obtener la información del cuestionario');
        }
        this.question = question;
        if(question.code === "end") {
          return ({ appState: 'END', questionState: 'END', appData: question })
        }
        this.questionButtons.restart(this.question.type, this.question.options);
        return ({ appState: 'LOADED', questionState: 'LOADED', appData: question })
      }),
      startWith({ appState: 'LOADED', questionState: 'LOADING' }),
      catchError((error) => {
        console.error(error);
        return of({ appState: 'ERROR', questionState: 'ERROR', appError: error.message})
      })
    );
  }

  repeatQuestionnaire() {
    this.modalShowed = true;
    this.questionnaireState = this.questionnaireService.repeatQuestionnaire( this.router.url.split('/')[3], this.router.url.split('/')[2], this.router.url.split('/')[4]).pipe(
      map((questionnaireInfo: QuestionnaireInfo) => {
        if(questionnaireInfo === undefined ) {
          throw new Error('No se ha podido obtener la información del cuestionario');
        }
        this.alert = questionnaireInfo.alert;
        this.question = questionnaireInfo.question;
        this.alreadyExists = questionnaireInfo.alreadyExists;
        this.modalShowed = false;
        this.showModal("alert");
        return ({ appState: 'LOADED', questionState: 'LOADED', appData: questionnaireInfo })
      }),
      startWith({ appState: 'LOADING', questionState: 'LOADING' }),
      catchError((error) => {
        console.error(error);
        return of({ appState: 'ERROR', questionState: 'ERROR', appError: error.message})
      })
    );
  }

  end() {
    this.questionnaireState = this.questionnaireService.getNextQuestion( this.router.url.split('/')[3], this.router.url.split('/')[2], this.router.url.split('/')[4], this.question.code, "", "").pipe(
      map((question: QuestionnaireInfo["question"]) => {
        console.debug("Redirecting to: " + '/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] );
        this.router.navigateByUrl('pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3]);
        return ({ appState: 'END', questionState: 'END', appData: question })
      }),
      startWith({ appState: 'END', questionState: 'LOADING' }),
      catchError((error) => {
        console.error(error);
        return of({ appState: 'ERROR', questionState: 'ERROR', appError: error.message})
      })
    );
  }
}
