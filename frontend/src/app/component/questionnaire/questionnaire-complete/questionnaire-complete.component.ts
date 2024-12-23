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
import { AnthropometryService } from '../../../services/anthropometry/anthropometry.service';

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
  alertList: QuestionnaireInfo["alertList"] | undefined = undefined;
  currentAlert: number = 0;
  alreadyExists: boolean = false;
  buttonOptions: string[] = [];
  error: string = "";
  weightState: string = "";
  percentage: number = 0;
  @ViewChild('modalAlert') modalAlert: ElementRef;
  @ViewChild('modalWeight') modalWeight: ElementRef;
  @ViewChild('modalAlreadyExists') modalAlreadyExists: ElementRef;
  @ViewChild('modalRepeat') modalRepeat: ElementRef;
  @ViewChild('questionButtons') questionButtons: QuestionButtonsComponent;
  @ViewChild('weight') weight: ElementRef;
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

  constructor( public router: Router, private questionnaireService: QuestionnaireService, private formBuilder: FormBuilder ) { }

  ngOnInit() {
    this.setTitle();
    if(this.router.url.split('/')[5] === 'repetir') {
      this.repeatQuestionnaire(); 
      return;
    }
    this.questionnaireState = this.questionnaireService.getQuestionnaireInfo( this.router.url.split('/')[3], this.router.url.split('/')[2], this.router.url.split('/')[4]).pipe(
      map((questionnaireInfo: QuestionnaireInfo) => {
        if(questionnaireInfo === undefined ) {
          throw new Error('No se ha podido obtener la información del cuestionario');
        }
        this.question = questionnaireInfo.question;
        if(questionnaireInfo.question.code.includes("end") || questionnaireInfo.question.code.includes("End") ) {
          return ({ appState: 'END', questionState: 'END', appData: questionnaireInfo })
        }
        this.setOptions(this.question.type, this.question.options);
        this.alreadyExists = questionnaireInfo.alreadyExists;
        this.modalShowed = false;
        this.alertList = questionnaireInfo.alertList;
        this.calculatePercentage(this.question.introduction, this.question.question);
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
          this.showNextAlert(true);
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
      case 'CMTCEF':
        this.title = 'Cuestionario del modelo transteórico del cambio de ejercicio físico';
        break;
      case 'PAR-Q':
        this.title = 'Cuestionario de aptitud para la actividad física para todos (PAR-Q+)';
        break;
      case 'ePARmed-X':
        this.title = 'Examen médico electrónico de aptitud para la actividad física (ePARmed-X+)';
        break;
      case 'IPAQ-E':
        this.title = 'Cuestionario internacional de actividad física en personas mayores (IPAQ-E)';
        break;
      case 'APALQ':
        this.title = 'Cuestionario de evaluación de los niveles de actividad física (APALQ)';
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
    if(modal === "weight") {
      $(this.modalWeight.nativeElement).modal('show');
    }
    if(modal === "repeat") {
      $(this.modalRepeat.nativeElement).modal('show');
    }
  }

  showNextAlert(first: boolean = false) {
    first ? this.currentAlert = 0 : this.currentAlert++;
    if(this.alertList == undefined) {
      return;
    }

    if(this.currentAlert < this.alertList?.length) {
      this.alertList[this.currentAlert].title === "PESO DEL PACIENTE" ? this.showModal("weight") : this.showModal("alert");
    } else if(this.alertList.length === 1) {
      this.currentAlert = 0;
      this.showModal("alert");
    }
  }

  submit() {
    let answer = this.questionButtons.checkAnswer();
    if(answer === undefined) {
      return;
    }
    this.questionnaireState = this.questionnaireService.getNextQuestion( this.router.url.split('/')[3], this.router.url.split('/')[2], this.router.url.split('/')[4], this.question.code, this.question.question, answer).pipe(
      map((question: QuestionnaireInfo["question"]) => {
        if(question === undefined ) {
          throw new Error('No se ha podido obtener la información del cuestionario');
        }
        this.question = question;
        if(question.code === "end") {
          return ({ appState: 'END', questionState: 'END', appData: question })
        }
        this.setOptions(this.question.type, this.question.options);
        this.questionButtons.restart(this.question.type, this.question.options);
        this.calculatePercentage(this.question.introduction, this.question.question);
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
        this.alertList = questionnaireInfo.alertList;
        this.question = questionnaireInfo.question;
        this.alreadyExists = questionnaireInfo.alreadyExists;
        this.modalShowed = false;
        this.setOptions(this.question.type, this.question.options);
        this.showNextAlert(true);
        this.percentage = 0;
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
        console.debug("Redirecting to: " + '/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] )
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

  setOptions(type: string, options: string[]) {
    if(type === 'days') {
      this.buttonOptions = ['0', '1', '2', '3', '4', '5', '6', '7'];
    }
    if(type === 'yesOrNo') {
      this.buttonOptions = ['Sí', 'No'];
    }
    if(type === 'multipleChoice') {
      this.buttonOptions = options
    }
    if(type === 'agreementLevel') {
      this.buttonOptions = ['Totalmente de acuerdo', 'Mayormente de acuerdo', 'Ni de acuerdo ni en desacuerdo', 'Mayormente en desacuerdo', 'Totalmente en desacuerdo'];
    }
  }

  goBack() {
    console.debug("Redirecting to: " + '/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3]);
    this.router.navigateByUrl('pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3]);
  }

  saveWeight() {
    if (!this.validateWeight()) {
      return;
    }
    this.weightState = "LOADING";
    this.error = "";
    let weight = this.weight.nativeElement.value;
    this.questionnaireService.setWeight(this.router.url.split('/')[3], this.router.url.split('/')[2], this.router.url.split('/')[4], weight).subscribe((response: boolean) => {
      if (response) {
        $(this.modalWeight.nativeElement).modal('hide');
        this.showNextAlert();
      } else {
        this.error = "No se ha podido guardar el peso";
        this.weightState = "ERROR";
      }
    });
  }

  validateWeight() : boolean{
    let weight = +this.weight.nativeElement.value;
    if (weight == undefined || weight == null) {
      this.error = "Introduzca un peso";
      this.weightState = "ERROR";
      return false;
    }
    weight = AnthropometryService.round(weight, 2);
    this.weight.nativeElement.value = weight;
    if (weight <= 0 || weight >= 1000) {
      this.error = "Introduzca un peso válido";
      this.weightState = "ERROR";
      return false;
    }
    this.weightState = "";
    this.error = "";
    return true;
  }

  calculatePercentage(introduction: string, question: string) {
    if(introduction == "No realizo ejercicio físico de forma regular..."){
      let totalQuestions = 23;
      switch(question){
        case "Porque creo que no lo necesito.":
          this.percentage = Math.floor(100*1/totalQuestions);
          break;
        case "Porque no tengo acceso a un lugar adecuado.":
          this.percentage = Math.floor(100*2/totalQuestions);
          break;
        case "Y estoy satisfecho de ser una persona sedentaria.":
          this.percentage = Math.floor(100*3/totalQuestions);
          break;
        case "Pero estoy pensando que debería intentar comenzar un programa de ejercicio físico regular en los próximos 6 meses.":
          this.percentage = Math.floor(100*4/totalQuestions);
          break;
        case "Pero me gustaría probar con alguna actividad física.":
          this.percentage = Math.floor(100*5/totalQuestions);
          break;
        case "Pero ya he establecido un día y una hora para empezar a hacer ejercicio físico regular en las próximas semanas.":
          this.percentage = Math.floor(100*6/totalQuestions);
          break;
        case "Y no tengo pensado hacerlo.":
          this.percentage = Math.floor(100*7/totalQuestions);
          break;
        case "Pero creo que merece la pena realizarlo.":
          this.percentage = Math.floor(100*8/totalQuestions);
          break;
        case "Pero ya sé dónde voy a realizar ejercicio físico (gimnasio, parque, pista polideportiva,…).":
          this.percentage = Math.floor(100*9/totalQuestions);
          break;
        case "Porque no puedo contar con mi entorno (familia, amigos, …).":
          this.percentage = Math.floor(100*10/totalQuestions);
          break;
        case "Pero quería comenzar a tener una vida más activa.":
          this.percentage = Math.floor(100*11/totalQuestions);
          break;
        case "Porque no tengo energía (no me apetece).":
          this.percentage = Math.floor(100*12/totalQuestions);
          break;
        case "Pero me he planteado si sería capaz de hacerlo.":
          this.percentage = Math.floor(100*13/totalQuestions);
          break;
        case "Pero he estado pensando en que quizá quiera empezar.":
          this.percentage = Math.floor(100*14/totalQuestions);
          break;
        case "Pero lo considero importante.":
          this.percentage = Math.floor(100*15/totalQuestions);
          break;
        case "Pero he estado pensando en la posibilidad de comenzar a hacerlo.":
          this.percentage = Math.floor(100*16/totalQuestions);
          break;
        case "Y no me preocupa.":
          this.percentage = Math.floor(100*17/totalQuestions);
          break;
        case "Pero ya me estoy preparando para empezar a hacer ejercicio en grupo en las siguientes semanas.":
          this.percentage = Math.floor(100*18/totalQuestions);
          break;
        case "Pero ya he quedado con un amigo para empezar a hacer ejercicio dentro de las próximas semanas.":
          this.percentage = Math.floor(100*19/totalQuestions);
          break;
        case "Pero ya he estado llamando a amigos para encontrar a alguien con quien empezar a hacer ejercicio en las próximas semanas.":
          this.percentage = Math.floor(100*20/totalQuestions);
          break;
        case "Porque no lo considero importante.":
          this.percentage = Math.floor(100*21/totalQuestions);
          break;
        case "Porque no tengo tiempo.":
          this.percentage = Math.floor(100*22/totalQuestions);
          break;
      }
    }
    if(introduction == "Realizo ejercicio físico de forma regular..."){
      let totalQuestions = 10;
      switch(question){
        case "He tenido éxito en hacer ejercicio regularmente y tengo pensado continuar.":
          this.percentage = Math.floor(100*1/totalQuestions);
          break;
        case "Recientemente he empezado a hacer ejercicio regular.":
          this.percentage = Math.floor(100*2/totalQuestions);
          break;
        case "Llevo mucho tiempo haciendo ejercicio físico regular y pretendo seguir así.":
          this.percentage = Math.floor(100*3/totalQuestions);
          break;
        case "He conseguido seguir haciendo ejercicio físico durante los últimos seis meses.":
          this.percentage = Math.floor(100*4/totalQuestions);
          break;
        case "He empezado a hacer ejercicio regular y tengo pensado continuar.":
          this.percentage = Math.floor(100*5/totalQuestions);
          break;
        case "He empezado a hacer ejercicio con regularidad en los últimos seis meses.":
          this.percentage = Math.floor(100*6/totalQuestions);
          break;
        case "Por fin hago ejercicio con regularidad.":
          this.percentage = Math.floor(100*7/totalQuestions);
          break;
        case "Llevo tiempo haciendo ejercicio y tengo pensado continuar.":
          this.percentage = Math.floor(100*8/totalQuestions);
          break;
        case "He completado más de seis meses haciendo ejercicio físico regular.":
          this.percentage = Math.floor(100*9/totalQuestions);
          break;
      }
    }
  }
}
