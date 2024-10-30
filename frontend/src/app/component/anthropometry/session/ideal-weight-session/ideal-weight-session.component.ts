import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import { AnthropometryData } from '../../../../model/anthropometry/anthropometry-data';
import { AnthropometryService } from '../../../../services/anthropometry/anthropometry.service';
import { IdealWeight } from '../../../../model/anthropometry/ideal-weight';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-ideal-weight-session',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './ideal-weight-session.component.html',
  styleUrl: './ideal-weight-session.component.css'
})
export class IdealWeightSessionComponent {
  @Input() data: AnthropometryData | null | undefined;
  @Input() editable: boolean = true;
  @ViewChild('weight') weight: ElementRef;
  @ViewChild('height') height: ElementRef;
  @ViewChild('lorentz') lorentz: ElementRef;
  @ViewChild('metropolitan') metropolitan: ElementRef;
  @ViewChild('idealWeight') idealWeight: ElementRef;
  @ViewChild('difference') difference: ElementRef;
  error: string = "";
  gender: boolean;

  constructor( private anthropometryService: AnthropometryService) {  }

  ngAfterViewInit(): void {
    if (this.data) {
      console.debug(this.data);
      let idealWeight = this.data as IdealWeight;
      if(idealWeight.gender == undefined) {
        throw new Error ("No se pudo calcular el peso ideal");
      }
      if(idealWeight.data) {
        this.weight.nativeElement.value = idealWeight.data.weight;
        this.height.nativeElement.value = idealWeight.data.height;
        if(idealWeight.data.formula) {
          this.chooseFormula(idealWeight.data.formula);
        } else {
          this.chooseFormula("Fórmula de Lorentz");
        }
        this.calculate();
      } else {
        this.weight.nativeElement.value = "";
        this.height.nativeElement.value = "";
      }
    }
  }

  calculate(): boolean {
    let weight = +this.weight.nativeElement.value;
    let height = +this.height.nativeElement.value;
    if(weight) {
      weight = AnthropometryService.round(+this.weight.nativeElement.value, 1);
      this.weight.nativeElement.value = weight
    }
    if(height) {
      height = AnthropometryService.round(+this.height.nativeElement.value, 1);
      this.height.nativeElement.value = height
    }
    if (!this.validate()) return false;
    if (weight && height) {
      let solutions = this.anthropometryService.calculateIdealWeight(this.getFormula(), weight, height, this.gender);

      this.idealWeight.nativeElement.value = solutions.idealWeight + " kg";
      this.difference.nativeElement.value = solutions.difference + " kg";
    }
    return true;
  }

  validate(): boolean {
    const weight = +this.weight.nativeElement.value;
    const height = +this.height.nativeElement.value;
    if (weight) {
      if (weight < 0 || weight >= 1000) {
        this.error = "Introduzca un peso válido";
        return false;
      }
    }
    if (height) {
      if (height < 10 || height >= 300) {
        this.error = "Introduzca una altura válida";
        return false;
      }
    }
    this.error = "";
    return true;
  }

  getData(): IdealWeight | null {
    if(!this.calculate()) return null;
    return {
      data: {
        weight: +this.weight.nativeElement.value,
        height: +this.height.nativeElement.value,
        formula: this.getFormula()
      }
    };
  }

  chooseFormula(formula: string) {
    switch (formula) {
      case "Fórmula de Lorentz":
        if(!this.editable){
          this.metropolitan.nativeElement.disabled = true;
        }
        this.lorentz.nativeElement.checked = true;
        this.metropolitan.nativeElement.checked = false;
        break;
      case "Fórmula de la Metropolitan Life Insurance Company":
        if(!this.editable){
          this.lorentz.nativeElement.disabled = true;
        }
        this.metropolitan.nativeElement.checked = true;
        this.lorentz.nativeElement.checked = false;
        break;
      default:
        throw new Error("Fórmula no válida");
    }
    this.calculate();
  }

  getFormula() {
    if(this.lorentz.nativeElement.checked) {
      return "Fórmula de Lorentz";
    }
    if(this.metropolitan.nativeElement.checked) {
      return "Fórmula de la Metropolitan Life Insurance Company";
    }
    throw new Error("Fórmula no válida");
  }

  formulaClick(formula:string) {
    if(!this.editable) return;
    switch(formula) {
      case "Fórmula de Lorentz":
        this.chooseFormula(formula);
        break;
      case "Fórmula de la Metropolitan Life Insurance Company":
        this.chooseFormula(formula);
        break;
      default:
        throw new Error("Fórmula no válida");
    }
  }
}
