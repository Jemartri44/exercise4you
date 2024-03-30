import { CommonModule } from '@angular/common';
import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import { Router } from '@angular/router';

declare let $:any;

@Component({
  selector: 'app-session-buttons',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './session-buttons.component.html',
  styleUrl: './session-buttons.component.css'
})
export class SessionButtonsComponent {
  @Input() buttons: string[] = [];
  @Input() session: number = 0;
  @Input() date: string = "";
  @ViewChild('modalRepeatQuestionnaire') modalRepeatQuestionnaire: ElementRef;

  constructor( private router: Router ) { }

  goToCompleteQuestionnaire(session: number) {
    console.debug("Redirecting to complete questionnaire "+ this.router.url.split('/')[3] + " session " + session);
    console.debug("Redirecting to: " + '/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + session + '/completar');
    this.router.navigate(['/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + '/' + session + '/completar']);
  }

  goToAnswers(session: number) {
    console.debug("Redirecting to see answers of "+ this.router.url.split('/')[3] + " session " + session);
    this.router.navigate(['/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + '/' + session + '/ver-respuestas']);
  }

  goToSeeReport(session: number) {
    console.debug("Redirecting to see report of "+ this.router.url.split('/')[3] + " session " + session);
    this.router.navigate(['/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + '/' + session + '/ver-informe']);
  }

  goToRepeatQuestionnaire(session: number) {
    console.debug("Redirecting to see answers of "+ this.router.url.split('/')[3] + " session " + session);
    this.router.navigate(['/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + '/' + session + '/repetir']);
  }

}
