import { AfterViewInit, Component, ElementRef, Input, ViewChild } from '@angular/core';
import { Imc } from '../../../../model/anthropometry/imc';
import { CommonModule } from '@angular/common';
import { AnthropometryData } from '../../../../model/anthropometry/anthropometry-data';
import { AnthropometryService } from '../../../../services/anthropometry/anthropometry.service';

@Component({
  selector: 'app-imc-session',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './imc-session.component.html',
  styleUrl: './imc-session.component.css'
})
export class ImcSessionComponent implements AfterViewInit {
  @Input() data: AnthropometryData | null | undefined;
  @Input() editable: boolean = true;
  @ViewChild('weight') weight: ElementRef;
  @ViewChild('height') height: ElementRef;
  @ViewChild('imc') imc: ElementRef;
  @ViewChild('imcp') imcp: ElementRef;
  @ViewChild('category') category: ElementRef;
  @ViewChild('risk') risk: ElementRef;
  error: string = "";
  changesSaved: boolean = false;


  constructor( private anthropometryService: AnthropometryService) { }

  ngAfterViewInit(): void {
    if (this.data) {
      let imc = this.data as Imc;
      if(imc.data) {
        this.weight.nativeElement.value = imc.data.weight;
        this.height.nativeElement.value = imc.data.height;
        this.calculate();
      } else {
        this.weight.nativeElement.value = "";
        this.height.nativeElement.value = "";
      }
    }
  }

  calculate(): boolean {
    this.changesSaved = false;
    let weight = +this.weight.nativeElement.value;
    let height = +this.height.nativeElement.value;
    if(weight) {
      weight = AnthropometryService.round(+this.weight.nativeElement.value, 2);
      this.weight.nativeElement.value = weight
    }
    if(height) {
      height = AnthropometryService.round(+this.height.nativeElement.value, 2);
      this.height.nativeElement.value = height
    }
    console.debug(this.validate())
    if (!this.validate()) return false;
    if (weight && height) {
      let solutions = this.anthropometryService.calculateImc(weight, height);

      this.imc.nativeElement.value = solutions.imc;
      this.imcp.nativeElement.value = solutions.imcp;
      this.category.nativeElement.value = solutions.category;
      this.risk.nativeElement.value = solutions.risk;
    }
    return true;
  }

  validate(): boolean {
    const weight = +this.weight.nativeElement.value;
    const height = +this.height.nativeElement.value;
    console.debug(weight)
    if (weight != null && weight != undefined) {
      if (weight <= 0 || weight >= 1000) {
        this.error = "Introduzca un peso válido";
        return false;
      }
    }
    if (height != null && height != undefined) {
      if (height <= 0) {
        this.error = "Introduzca una altura válida";
        return false;
      }
      if (height >= 3) {
        this.error = "Introduzca la altura en metros. Ejemplo: 1.75";
        return false;
      }
    }
    this.error = "";
    return true;
  }

  getData(): Imc | null {
    if(!this.calculate()) return null;
    return {
      data: {
        weight: +this.weight.nativeElement.value,
        height: +this.height.nativeElement.value
      }
    };
  }
}
