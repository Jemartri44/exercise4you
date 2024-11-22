import { AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { Icc } from '../../../../model/anthropometry/icc';
import { AnthropometryService } from '../../../../services/anthropometry/anthropometry.service';
import { AnthropometryData } from '../../../../model/anthropometry/anthropometry-data';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-icc-session',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './icc-session.component.html',
  styleUrl: './icc-session.component.css'
})
export class IccSessionComponent implements AfterViewInit, OnInit {
  @Input() data: AnthropometryData | null | undefined;
  @Input() editable: boolean = true;
  @ViewChild('waistCircumference') waistCircumference: ElementRef;
  @ViewChild('hipCircumference') hipCircumference: ElementRef;
  @ViewChild('icc') icc: ElementRef;
  @ViewChild('category') category: ElementRef;
  @ViewChild('risk') risk: ElementRef;
  error: string = "";
  gender: boolean;

  constructor( private anthropometryService: AnthropometryService) {  }

  ngOnInit(): void {
    if (this.data) {
      let icc = this.data as Icc;
      if(icc.gender == undefined){
        throw new Error ("No se pudo calcular el índice cintura-cadera")
      }else {}
      this.gender = icc.gender;
    }
  }

  ngAfterViewInit(): void {
    if (this.data) {
      let icc = this.data as Icc;
      if(icc.data) {
        this.waistCircumference.nativeElement.value = icc.data.waistCircumference;
        this.hipCircumference.nativeElement.value = icc.data.hipCircumference;
        this.calculate();
      } else {
        this.waistCircumference.nativeElement.value = "";
        this.hipCircumference.nativeElement.value = "";
      }
    }
  }

  calculate(): boolean {
    let waistCircumference = +this.waistCircumference.nativeElement.value;
    let hipCircumference = +this.hipCircumference.nativeElement.value;
    if(waistCircumference) {
      waistCircumference = AnthropometryService.round(+this.waistCircumference.nativeElement.value, 1);
      this.waistCircumference.nativeElement.value = waistCircumference
    }
    if(hipCircumference) {
      hipCircumference = AnthropometryService.round(+this.hipCircumference.nativeElement.value, 1);
      this.hipCircumference.nativeElement.value = hipCircumference
    }
    if (!this.validate()) return false;
    if (waistCircumference && hipCircumference) {
      let solutions = this.anthropometryService.calculateIcc(waistCircumference, hipCircumference, this.gender);

      this.icc.nativeElement.value = solutions.icc;
      this.category.nativeElement.value = solutions.category;
      this.risk.nativeElement.value = solutions.risk;
    }
    return true;
  }

  validate(): boolean {
    const waistCircumference = +this.waistCircumference.nativeElement.value;
    const hipCircumference = +this.hipCircumference.nativeElement.value;
    if (waistCircumference) {
      if (waistCircumference < 0 || waistCircumference >= 1000) {
        this.error = "Introduzca una circunferencia de cintura válida";
        return false;
      }
    }
    if (hipCircumference) {
      if (hipCircumference < 0 || hipCircumference >= 1000) {
        this.error = "Introduzca una circunferencia de cadera válida";
        return false;
      }
    }
    this.error = "";
    return true;
  }

  getData(): Icc | null {
    if(!this.calculate()) return null;
    return {
      data: {
        waistCircumference: +this.waistCircumference.nativeElement.value,
        hipCircumference: +this.hipCircumference.nativeElement.value
      }
    };
  }
}
