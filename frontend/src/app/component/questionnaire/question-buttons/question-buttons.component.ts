import { CommonModule } from '@angular/common';
import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';

@Component({
  selector: 'app-question-buttons',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './question-buttons.component.html',
  styleUrl: './question-buttons.component.css'
})
export class QuestionButtonsComponent implements OnInit {

  @Input() type: string;
  @Input() options: string[];
  @ViewChild('hours') hours: ElementRef;
  @ViewChild('minutes') minutes: ElementRef;
  @ViewChild('answer') answer: ElementRef;
  show: boolean;
  selected: boolean[] = [];
  selectedOption: number | undefined;
  error: string = "";

  constructor() {  }

  ngOnInit(): void {
    this.selected = [];
    this.selectedOption = undefined;
    if (this.type === 'days') {
      this.options = ['0', '1', '2', '3', '4', '5', '6', '7'];
      this.selected = [false, false, false, false, false, false, false, false];
    }
    if (this.type === 'yesOrNo') {
      this.options = ['Sí', 'No'];
      this.selected = [false, false];
    }
    if (this.type === 'multipleChoice') {
      for(let i = 0; i < this.options.length; i++){
        this.selected.push(false);
      }
    }
    if (this.type === 'agreementLevel') {
      this.options = ['Totalmente de acuerdo', 'Mayormente de acuerdo', 'Ni de acuerdo ni en desacuerdo', 'Mayormente en desacuerdo', 'Totalmente en desacuerdo'];
      this.selected = [false, false, false, false, false];
    } 
    if (this.type === 'writtenAnswer') {
      if(this.answer !== undefined) {
        this.answer.nativeElement.value = '';
      }
    }
    if (this.type === 'time') {
      this.selected = [false, false];
      if(this.hours !== undefined) {
        this.hours.nativeElement.value = '';
      }
      if(this.minutes !== undefined) {
        this.minutes.nativeElement.value = '';
      }
    }
  }

  selectOption(index: number){
    if(this.selectedOption !== undefined){
      this.selected[this.selectedOption] = false;
    }
    this.selected[index] = true;
    this.selectedOption = index;
  }

  notSure() {
    this.selectedOption = 0;
    this.selected[1] = false;
    this.selected[0] = true;
  }

  sure() {
    this.selectedOption = 1;
    this.selected[0] = false;
    this.selected[1] = true;
  }

  checkAnswer(): string | undefined{
    this.error = "";
    if(this.type == 'days') {
      if(this.selectedOption !== undefined) {
        return this.options[this.selectedOption] + " días";
      }
      this.error = "Seleccione una opción";
    }
    if(this.type == 'yesOrNo' || this.type == 'multipleChoice' || this.type == 'agreementLevel') {
      if(this.selectedOption !== undefined) {
        return this.options[this.selectedOption];
      }
      this.error = "Seleccione una opción";
    }
    if(this.type == 'time') {
      if(this.selectedOption === undefined) {
        this.error = "Intrdoduzca una cantidad de tiempo o seleccione 'No sé/No estoy seguro'";
        return;
      }
      if(this.selectedOption === 0) {
        return "No sé/No estoy seguro";
      }
      if((this.hours.nativeElement.value === '' && this.minutes.nativeElement.value !== '')) {
        return 0 + ' horas, ' + this.minutes.nativeElement.value + ' minutos';
      }
      if((this.hours.nativeElement.value !== '' && this.minutes.nativeElement.value === '')) {
        return this.hours.nativeElement.value + ' horas, ' + 0 + ' minutos';
      }
      if(this.hours.nativeElement.value === '' && this.minutes.nativeElement.value === '') {
        this.error = "Intrdoduzca una cantidad de tiempo o seleccione 'No sé/No estoy seguro'";
        return;
      }
      return this.hours.nativeElement.value + ' horas, ' + this.minutes.nativeElement.value + ' minutos';
    }
    if(this.type == 'writtenAnswer') {
      if(this.answer.nativeElement.value !== '') {
        return this.answer.nativeElement.value;
      }
      this.error = "Escriba una respuesta";
    }
    return;
  }
  
  public restart(type: string, options: string[]) {
    if(options !== null && options !== undefined) {
      this.options = options;
    }
    this.type = type;
    this.ngOnInit();
  }

  change() {
    if (this.hours.nativeElement.value < 0) {
      this.hours.nativeElement.value = 23;
    } else if (this.hours.nativeElement.value > 23) {
      this.hours.nativeElement.value = 0;
    } else if (this.minutes.nativeElement.value < 0) {
      this.minutes.nativeElement.value = 59;
    } else if (this.minutes.nativeElement.value > 59) {
      this.minutes.nativeElement.value = 0;
    }
    this.sure();
  }
}
