import { AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { WaistCircumference } from '../../../../model/anthropometry/waist-circumference';
import { AnthropometryService } from '../../../../services/anthropometry/anthropometry.service';
import { AnthropometryData } from '../../../../model/anthropometry/anthropometry-data';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-waist-circumference-session',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './waist-circumference-session.component.html',
  styleUrl: './waist-circumference-session.component.css'
})
export class WaistCircumferenceSessionComponent implements AfterViewInit, OnInit {
  @Input() data: AnthropometryData | null | undefined;
  @Input() editable: boolean = true;
  @ViewChild('waistCircumference') waistCircumference: ElementRef;
  @ViewChild('risk') risk: ElementRef;
  error: string = "";
  gender: boolean;

  constructor( private anthropometryService: AnthropometryService) { }

  ngOnInit(): void {
    if (this.data) {
      let waistCircumference = this.data as WaistCircumference;
      if(waistCircumference.gender == undefined){
        throw new Error ("No se pudo calcular el índice cintura-cadera")
      }
      this.gender = waistCircumference.gender;
    }
  }

  ngAfterViewInit(): void {
    if (this.data) {
      let waistCircumference = this.data as WaistCircumference;
      if(waistCircumference.data) {
        this.waistCircumference.nativeElement.value = waistCircumference.data.waistCircumference;
        this.calculate();
      } else {
        this.waistCircumference.nativeElement.value = "";
      }
    }
  }

  calculate(): boolean {
    let waistCircumference = +this.waistCircumference.nativeElement.value;
    if(waistCircumference) {
      waistCircumference = AnthropometryService.round(+this.waistCircumference.nativeElement.value, 1);
      this.waistCircumference.nativeElement.value = waistCircumference
    }
    if (!this.validate()) return false;
    if (waistCircumference) {
      let solutions = this.anthropometryService.calculateWaistCircumference(waistCircumference, this.gender);

      this.risk.nativeElement.value = solutions.risk;
    }
    return true;
  }

  validate(): boolean {
    const waistCircumference = +this.waistCircumference.nativeElement.value;
    if (waistCircumference) {
      if (waistCircumference < 0 || waistCircumference >= 1000) {
        this.error = "Introduzca una circunferencia de cintura válida";
        return false;
      }
    }
    this.error = "";
    return true;
  }

  getData(): WaistCircumference | null {
    if(!this.calculate()) return null;
    return {
      data: {
        waistCircumference: +this.waistCircumference.nativeElement.value
      }
    };
  }
}
