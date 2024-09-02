import { AfterContentInit, AfterViewChecked, AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { AnthropometryData } from '../../../../model/anthropometry/anthropometry-data';
import { AnthropometryService } from '../../../../services/anthropometry/anthropometry.service';
import { SkinFolds } from '../../../../model/anthropometry/skin-folds';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-skin-folds-session',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './skin-folds-session.component.html',
  styleUrl: './skin-folds-session.component.css'
})
export class SkinFoldsSessionComponent implements AfterViewInit, OnInit{
  @Input() data: AnthropometryData | null | undefined;
  @Input() editable: boolean = true;
  // Data
  @ViewChild('weight') weight: ElementRef;
  @ViewChild('height') height: ElementRef;
  // Skin folds
  @ViewChild('bicipitalFold') bicipitalFold: ElementRef;
  @ViewChild('pectoralFold') pectoralFold: ElementRef;
  @ViewChild('midaxillaryFold') midaxillaryFold: ElementRef;
  @ViewChild('tricipitalFold') tricipitalFold: ElementRef;
  @ViewChild('subscapularFold') subscapularFold: ElementRef;
  @ViewChild('abdominalFold') abdominalFold: ElementRef;
  @ViewChild('suprailiacFold') suprailiacFold: ElementRef;
  @ViewChild('anteriorThighFold') anteriorThighFold: ElementRef;
  // Adult formulas
  @ViewChild('jackson3') jackson3: ElementRef;
  @ViewChild('jackson7') jackson7: ElementRef;
  @ViewChild('durnin') durnin: ElementRef;
  // Child formulas
  @ViewChild('boileau') boileau: ElementRef;
  // Lean mass formulas
  @ViewChild('james') james: ElementRef;
  @ViewChild('hume') hume: ElementRef;
  // Results
  @ViewChild('density') density: ElementRef;
  @ViewChild('fatMass') fatMass: ElementRef;
  @ViewChild('fatMassPercentage') fatMassPercentage: ElementRef;
  @ViewChild('fatFreeMass') fatFreeMass: ElementRef;
  @ViewChild('fatLevel') fatLevel: ElementRef;
  @ViewChild('leanMass') leanMass: ElementRef;
  error: string = "";
  formula1: string = "";
  formula2: string = "";
  gender: boolean;
  age: number

  constructor( private anthropometryService: AnthropometryService) {  }

  ngOnInit(): void {
    if (this.data) {
      console.debug(this.data);
      let skinFolds = this.data as SkinFolds;
      if(skinFolds.gender == undefined) {
        throw new Error ("No se pudo recuperar la información sobre el paciente");
      }
      this.gender = skinFolds.gender;
      if(skinFolds.age == undefined) {
        throw new Error ("No se pudo recuperar la información sobre el paciente");
      }
      this.age = skinFolds.age;
      if(skinFolds.data != undefined) {
        if(skinFolds.data.formula1 && skinFolds.data.formula2) {
          this.formula1 = skinFolds.data.formula1;
          this.formula2 = skinFolds.data.formula2;
          return;
        }
      }
      if(this.age < 18) {
        this.formula1 = "Fórmula de Boileau et al.";
      } else { 
        this.formula1 = "Fórmula de Jackson-Pollock de 3 pliegues";
      }
      this.formula2 = "Fórmula de James";
    }
  }

  ngAfterViewInit(): void {
    let skinFolds = this.data as SkinFolds;
    if(skinFolds.data) {
      this.setParams(skinFolds);
      if(skinFolds.data.formula1) {
        this.chooseFormula1(skinFolds.data.formula1, false);
      } else {
        this.chooseFormula1(undefined, false);
      }
      if(skinFolds.data.formula2) {
        this.chooseFormula2(skinFolds.data.formula2, false);
      } else {
        this.chooseFormula2("Fórmula de James", false);
      }
    } else {
      this.chooseFormula1(undefined, false);
      this.chooseFormula2("Fórmula de James", false);
    }
  }


  calculate(throwError:boolean = true): boolean {
    this.roundParams();
    if (!this.validate(throwError)) return false;
    let foldsSum: number;
    switch(this.formula1) {
      case "Fórmula de Jackson-Pollock de 3 pliegues":
        if (this.gender) {
          foldsSum = +this.anteriorThighFold.nativeElement.value + +this.abdominalFold.nativeElement.value + +this.pectoralFold.nativeElement.value;
        } else {
          foldsSum = +this.anteriorThighFold.nativeElement.value + +this.tricipitalFold.nativeElement.value + +this.suprailiacFold.nativeElement.value;
        }
        break;
      case "Fórmula de Jackson-Pollock de 7 pliegues":
        foldsSum = +this.pectoralFold.nativeElement.value + +this.midaxillaryFold.nativeElement.value + +this.tricipitalFold.nativeElement.value + +this.subscapularFold.nativeElement.value + +this.abdominalFold.nativeElement.value + +this.suprailiacFold.nativeElement.value + +this.anteriorThighFold.nativeElement.value;
        break;
      case "Fórmula de Durnin-Womersley":
        foldsSum = +this.suprailiacFold.nativeElement.value + +this.subscapularFold.nativeElement.value + +this.bicipitalFold.nativeElement.value + +this.tricipitalFold.nativeElement.value;
        break;
      case "Fórmula de Boileau et al.":
        foldsSum = +this.tricipitalFold.nativeElement.value + +this.subscapularFold.nativeElement.value;
        break;
      default:
        throw new Error("Fórmula no válida");
    }
    let solutions = this.anthropometryService.calculateSkinFolds(this.formula1, this.formula2, foldsSum, this.weight.nativeElement.value, this.height.nativeElement.value, this.gender, this.age);
    this.setSolutions(solutions);
    return true;
  }

  validate(throwError: boolean = true): boolean {
    this.error = "";
    const weight = +this.weight.nativeElement.value;
    const height = +this.height.nativeElement.value;
    if (!weight || !height) {
      if(throwError){this.error = 'Complete los campos requeridos'}
      return false;
    }
    if (weight != undefined) {
      if (weight <= 0 || weight >= 1000) {
        if(throwError){this.error = "Introduzca un peso válido"}
        return false;
      }
    }
    if (height != undefined) {
      if (height < 10 || height >= 300) {
        if(throwError){this.error = "Introduzca una altura válida"}
        return false;
      }
    }
    let bicipitalFold, pectoralFold, midaxillaryFold, tricipitalFold, subscapularFold, abdominalFold, suprailiacFold, anteriorThighFold: number;
    switch(this.formula1) {
      case "Fórmula de Jackson-Pollock de 3 pliegues":
        if(this.gender) {
          anteriorThighFold = +this.anteriorThighFold.nativeElement.value
          abdominalFold = +this.abdominalFold.nativeElement.value
          pectoralFold = +this.pectoralFold.nativeElement.value
          if (!anteriorThighFold || !abdominalFold || !pectoralFold) { if(throwError){this.error = 'Complete los campos requeridos'}; return false; }
          if (anteriorThighFold < 0 || anteriorThighFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue de muslo anterior válido"}; return false; }
          if (abdominalFold < 0 || abdominalFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue abdominal válido"}; return false; }
          if (pectoralFold < 0 || pectoralFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue pectoral válido"}; return false; }
        } else {
          suprailiacFold = +this.suprailiacFold.nativeElement.value
          tricipitalFold = +this.tricipitalFold.nativeElement.value
          anteriorThighFold = +this.anteriorThighFold.nativeElement.value
          if (!suprailiacFold || !tricipitalFold || !anteriorThighFold) { if(throwError){this.error = 'Complete los campos requeridos'}; return false; }
          if (suprailiacFold < 0 || suprailiacFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue suprailíaco válido"}; return false; }
          if (tricipitalFold < 0 || tricipitalFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue tricipital válido"}; return false; }
          if (anteriorThighFold < 0 || anteriorThighFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue de muslo anterior válido"}; return false; }
        }
        break;
      case "Fórmula de Jackson-Pollock de 7 pliegues":
        pectoralFold = +this.pectoralFold.nativeElement.value
        midaxillaryFold = +this.midaxillaryFold.nativeElement.value
        tricipitalFold = +this.tricipitalFold.nativeElement.value
        subscapularFold = +this.subscapularFold.nativeElement.value
        abdominalFold = +this.abdominalFold.nativeElement.value
        suprailiacFold = +this.suprailiacFold.nativeElement.value
        anteriorThighFold = +this.anteriorThighFold.nativeElement.value
        if (!pectoralFold || !midaxillaryFold || !tricipitalFold || !subscapularFold || !abdominalFold || !suprailiacFold || !anteriorThighFold) { if(throwError){this.error = 'Complete los campos requeridos'}; return false; }
        if (pectoralFold < 0 || pectoralFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue pectoral válido"}; return false; }
        if (midaxillaryFold < 0 || midaxillaryFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue axilar medio válido"}; return false; }
        if (tricipitalFold < 0 || tricipitalFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue tricipital válido"}; return false; }
        if (subscapularFold < 0 || subscapularFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue subescapular válido"}; return false; }
        if (abdominalFold < 0 || abdominalFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue abdominal válido"}; return false; }
        if (suprailiacFold < 0 || suprailiacFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue suprailíaco válido"}; return false; }
        if (anteriorThighFold < 0 || anteriorThighFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue de muslo anterior válido"}; return false; }
        break;
      case "Fórmula de Durnin-Womersley":
        suprailiacFold = +this.suprailiacFold.nativeElement.value
        subscapularFold = +this.subscapularFold.nativeElement.value
        bicipitalFold = +this.bicipitalFold.nativeElement.value
        tricipitalFold = +this.tricipitalFold.nativeElement.value
        if (!suprailiacFold || !subscapularFold || !bicipitalFold || !tricipitalFold) { if(throwError){this.error = 'Complete los campos requeridos'}; return false; }
        if (suprailiacFold < 0 || suprailiacFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue suprailíaco válido"}; return false; }
        if (subscapularFold < 0 || subscapularFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue subescapular válido"}; return false; }
        if (bicipitalFold < 0 || bicipitalFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue bicipital válido"}; return false; }
        if (tricipitalFold < 0 || tricipitalFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue tricipital válido"}; return false; }
        break;
      case "Fórmula de Boileau et al.":
        tricipitalFold = this.tricipitalFold.nativeElement.value
        subscapularFold = this.subscapularFold.nativeElement.value
        if (!tricipitalFold || !subscapularFold) { if(throwError){this.error = 'Complete los campos requeridos'}; return false; }
        if (tricipitalFold < 0 || tricipitalFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue tricipital válido"}; return false; }
        if (subscapularFold < 0 || subscapularFold >= 1000) { if(throwError){this.error = "Introduzca un pliegue subescapular válido"}; return false; }
        break;
      default:
        throw new Error("Fórmula no válida");
    }
    this.error = "";
    return true;
  }

  getData(): SkinFolds | null {
    if(!this.calculate(true)) return null;
    switch(this.formula1) {
      case "Fórmula de Jackson-Pollock de 3 pliegues":
        if(this.gender) {
          return {
            data: {
              formula1: this.formula1,
              formula2: this.formula2,
              weight: +this.weight.nativeElement.value,
              height: +this.height.nativeElement.value,
              anteriorThighFold: +this.anteriorThighFold.nativeElement.value,
              abdominalFold: +this.abdominalFold.nativeElement.value,
              pectoralFold: +this.pectoralFold.nativeElement.value
            }
          };
        } else {
          return {
            data: {
              formula1: this.formula1,
              formula2: this.formula2,
              weight: +this.weight.nativeElement.value,
              height: +this.height.nativeElement.value,
              anteriorThighFold: +this.anteriorThighFold.nativeElement.value,
              tricipitalFold: +this.tricipitalFold.nativeElement.value,
              suprailiacFold: +this.suprailiacFold.nativeElement.value
            }
          };
        }
      case "Fórmula de Jackson-Pollock de 7 pliegues":
          return {
            data: {
              formula1: this.formula1,
              formula2: this.formula2,
              weight: +this.weight.nativeElement.value,
              height: +this.height.nativeElement.value,
              pectoralFold: +this.pectoralFold.nativeElement.value,
              midaxillaryFold: +this.midaxillaryFold.nativeElement.value,
              tricipitalFold: +this.tricipitalFold.nativeElement.value,
              subscapularFold: +this.subscapularFold.nativeElement.value,
              abdominalFold: +this.abdominalFold.nativeElement.value,
              suprailiacFold: +this.suprailiacFold.nativeElement.value,
              anteriorThighFold: +this.anteriorThighFold.nativeElement.value
            }
          };
      case "Fórmula de Durnin-Womersley":
        return {
          data: {
            formula1: this.formula1,
            formula2: this.formula2,
            weight: +this.weight.nativeElement.value,
            height: +this.height.nativeElement.value,
            suprailiacFold: +this.suprailiacFold.nativeElement.value,
            subscapularFold: +this.subscapularFold.nativeElement.value,
            bicipitalFold: +this.bicipitalFold.nativeElement.value,
            tricipitalFold: +this.tricipitalFold.nativeElement.value
          }
        };
      case "Fórmula de Boileau et al.":
        return {
          data: {
            formula1: this.formula1,
            formula2: this.formula2,
            weight: +this.weight.nativeElement.value,
            height: +this.height.nativeElement.value,
            tricipitalFold: +this.tricipitalFold.nativeElement.value,
            subscapularFold: +this.subscapularFold.nativeElement.value
          }
        };
      default:
        return null;
    }
  }

  chooseFormula1(formula: string | undefined, throwError:boolean = true) {
    switch (formula) {
      case "Fórmula de Jackson-Pollock de 3 pliegues":
        if(!this.editable){
          this.jackson7.nativeElement.disabled = true;
          this.durnin.nativeElement.disabled = true;
        }
        this.jackson3.nativeElement.checked = true;
        this.jackson7.nativeElement.checked = false;
        this.durnin.nativeElement.checked = false;
        this.formula1 = formula;
        break;
      case "Fórmula de Jackson-Pollock de 7 pliegues":
        if(!this.editable){
          this.jackson3.nativeElement.disabled = true;
          this.durnin.nativeElement.disabled = true;
        }
        this.jackson3.nativeElement.checked = false;
        this.jackson7.nativeElement.checked = true;
        this.durnin.nativeElement.checked = false;
        this.formula1 = formula;
        break;
      case "Fórmula de Durnin-Womersley":
        if(!this.editable){
          this.jackson3.nativeElement.disabled = true;
          this.jackson7.nativeElement.disabled = true;
        }
        this.jackson3.nativeElement.checked = false;
        this.jackson7.nativeElement.checked = false;
        this.durnin.nativeElement.checked = true;
        this.formula1 = formula;
        break;
      case "Fórmula de Boileau et al.":
        this.boileau.nativeElement.checked = true;
        this.formula1 = formula;
        break;
      case undefined:
        if(this.age < 18) {
          this.boileau.nativeElement.checked = true;
          this.formula1 = "Fórmula de Boileau et al.";
          break;
        } else {
          if(!this.editable){
            this.jackson7.nativeElement.disabled = true;
            this.durnin.nativeElement.disabled = true;
          }
          this.jackson3.nativeElement.checked = true;
          this.jackson7.nativeElement.checked = false;
          this.durnin.nativeElement.checked = false;
          this.formula1 = "Fórmula de Jackson-Pollock de 3 pliegues";
          break;
        }
      default:
        throw new Error("Fórmula no válida");
    }
    if(this.validate(throwError)) { this.calculate(throwError); }
  }

  chooseFormula2(formula: string | undefined, throwError:boolean = true) {
    console.debug("Choosing formula 2")
    switch (formula) {
      case "Fórmula de James":
        if(!this.editable){
          this.hume.nativeElement.disabled = true;
        }
        this.james.nativeElement.checked = true;
        this.hume.nativeElement.checked = false;
        this.formula2 = formula;
        break;
      case "Fórmula de Hume":
        if(!this.editable){
          this.james.nativeElement.disabled = true;
        }
        console.debug("HOLA hume" + this.age)
        this.james.nativeElement.checked = false;
        this.hume.nativeElement.checked = true;
        console.debug("hume?" + this.hume.nativeElement.checked)
        this.formula2 = formula;
        break;
      case undefined:
        if(!this.editable){
          this.hume.nativeElement.disabled = true;
        }
        this.james.nativeElement.checked = true;
        this.hume.nativeElement.checked = false;
        this.formula2 = "Fórmula de James";
        break;
      default:
        throw new Error("Fórmula no válida");
    }
    if(this.validate(throwError)) { this.calculate(throwError); }
  }

  setParams(data: any) {
    if(data == null || data == undefined) return;
    if(data.data.weight) {
      this.weight.nativeElement.value = data.data.weight;
    }
    if(data.data.height) {
      this.height.nativeElement.value = data.data.height;
    }
    switch(this.formula1) {
      case "Fórmula de Jackson-Pollock de 3 pliegues":
        if(data.data.anteriorThighFold != undefined) {
          this.anteriorThighFold.nativeElement.value = data.data.anteriorThighFold;
        }
        if(this.gender) {
          if(data.data.abdominalFold != undefined) {
            this.abdominalFold.nativeElement.value = data.data.abdominalFold;
          }
          if(data.data.pectoralFold != undefined) {
            this.pectoralFold.nativeElement.value = data.data.pectoralFold;
          }
        } else {
          if(data.data.tricipitalFold != undefined) {
            this.tricipitalFold.nativeElement.value = data.data.tricipitalFold;
          }
          if(data.data.suprailiacFold != undefined) {
            this.suprailiacFold.nativeElement.value = data.data.suprailiacFold;
          }
        }
        break;
      case "Fórmula de Jackson-Pollock de 7 pliegues":
        if(data.data.pectoralFold != undefined) {
          this.pectoralFold.nativeElement.value = data.data.pectoralFold;
        }
        if(data.data.midaxillaryFold != undefined) {
          this.midaxillaryFold.nativeElement.value = data.data.midaxillaryFold;
        }
        if(data.data.tricipitalFold != undefined) {
          this.tricipitalFold.nativeElement.value = data.data.tricipitalFold;
        }
        if(data.data.subscapularFold != undefined) {
          this.subscapularFold.nativeElement.value = data.data.subscapularFold;
        }
        if(data.data.abdominalFold != undefined) {
          this.abdominalFold.nativeElement.value = data.data.abdominalFold;
        }
        if(data.data.suprailiacFold != undefined) {
          this.suprailiacFold.nativeElement.value = data.data.suprailiacFold;
        }
        if(data.data.anteriorThighFold != undefined) {
          this.anteriorThighFold.nativeElement.value = data.data.anteriorThighFold;
        }
        break;
      case "Fórmula de Durnin-Womersley":
        if(data.data.suprailiacFold != undefined) {
          this.suprailiacFold.nativeElement.value = data.data.suprailiacFold;
        }
        if(data.data.subscapularFold != undefined) {
          this.subscapularFold.nativeElement.value = data.data.subscapularFold;
        }
        if(data.data.bicipitalFold != undefined) {
          this.bicipitalFold.nativeElement.value = data.data.bicipitalFold;
        }
        if(data.data.tricipitalFold != undefined) {
          this.tricipitalFold.nativeElement.value = data.data.tricipitalFold;
        }
        break;
      case "Fórmula de Boileau et al.":
        if(data.data.tricipitalFold != undefined) {
          this.tricipitalFold.nativeElement.value = data.data.tricipitalFold;
        }
        if(data.data.subscapularFold != undefined) {
          this.subscapularFold.nativeElement.value = data.data.subscapularFold;
        }
        break;
      default:
        throw new Error("Fórmula no válida");
    }
  }

  setSolutions(solutions: {density: number, fatMass: number, fatMassPercentage: number, fatFreeMass: number, fatLevel: string, leanMass: number}) {
    this.density.nativeElement.value = solutions.density;
    this.fatMass.nativeElement.value = solutions.fatMass;
    this.fatMassPercentage.nativeElement.value = solutions.fatMassPercentage;
    this.fatFreeMass.nativeElement.value = solutions.fatFreeMass;
    this.fatLevel.nativeElement.value = solutions.fatLevel;
    this.leanMass.nativeElement.value = solutions.leanMass;
  }

  roundParams() {
    if(this.weight.nativeElement.value == "" || this.height.nativeElement.value == "") { return; }
    this.weight.nativeElement.value = AnthropometryService.round(+this.weight.nativeElement.value, 1);
    this.height.nativeElement.value = AnthropometryService.round(+this.height.nativeElement.value, 1);
    switch(this.formula1) {
      case "Fórmula de Jackson-Pollock de 3 pliegues":
        if(this.gender) {
          if(this.anteriorThighFold.nativeElement.value == "" || this.abdominalFold.nativeElement.value == "" || this.pectoralFold.nativeElement.value == "") { return; }
          this.anteriorThighFold.nativeElement.value = AnthropometryService.round(+this.anteriorThighFold.nativeElement.value, 0);
          this.abdominalFold.nativeElement.value = AnthropometryService.round(+this.abdominalFold.nativeElement.value, 0);
          this.pectoralFold.nativeElement.value = AnthropometryService.round(+this.pectoralFold.nativeElement.value, 0);
        } else {
          if(this.tricipitalFold.nativeElement.value == "" || this.suprailiacFold.nativeElement.value == "" || this.anteriorThighFold.nativeElement.value == "") { return; }
          this.anteriorThighFold.nativeElement.value = AnthropometryService.round(+this.anteriorThighFold.nativeElement.value, 0);
          this.tricipitalFold.nativeElement.value = AnthropometryService.round(+this.tricipitalFold.nativeElement.value, 0);
          this.suprailiacFold.nativeElement.value = AnthropometryService.round(+this.suprailiacFold.nativeElement.value, 0);
        }
        break;
      case "Fórmula de Jackson-Pollock de 7 pliegues":
        if(this.pectoralFold.nativeElement.value == "" || this.midaxillaryFold.nativeElement.value == "" || this.tricipitalFold.nativeElement.value == "" || this.subscapularFold.nativeElement.value == "" || this.abdominalFold.nativeElement.value == "" || this.suprailiacFold.nativeElement.value == "" || this.anteriorThighFold.nativeElement.value == "") { return; }
        this.pectoralFold.nativeElement.value = AnthropometryService.round(+this.pectoralFold.nativeElement.value, 0);
        this.midaxillaryFold.nativeElement.value = AnthropometryService.round(+this.midaxillaryFold.nativeElement.value, 0);
        this.tricipitalFold.nativeElement.value = AnthropometryService.round(+this.tricipitalFold.nativeElement.value, 0);
        this.subscapularFold.nativeElement.value = AnthropometryService.round(+this.subscapularFold.nativeElement.value, 0);
        this.abdominalFold.nativeElement.value = AnthropometryService.round(+this.abdominalFold.nativeElement.value, 0);
        this.suprailiacFold.nativeElement.value = AnthropometryService.round(+this.suprailiacFold.nativeElement.value, 0);
        this.anteriorThighFold.nativeElement.value = AnthropometryService.round(+this.anteriorThighFold.nativeElement.value, 0);
        break;
      case "Fórmula de Durnin-Womersley":
        if(this.suprailiacFold.nativeElement.value == "" || this.subscapularFold.nativeElement.value == "" || this.bicipitalFold.nativeElement.value == "" || this.tricipitalFold.nativeElement.value == "") { return; }
        this.suprailiacFold.nativeElement.value = AnthropometryService.round(+this.suprailiacFold.nativeElement.value, 0);
        this.subscapularFold.nativeElement.value = AnthropometryService.round(+this.subscapularFold.nativeElement.value, 0);
        this.bicipitalFold.nativeElement.value = AnthropometryService.round(+this.bicipitalFold.nativeElement.value, 0);
        this.tricipitalFold.nativeElement.value = AnthropometryService.round(+this.tricipitalFold.nativeElement.value, 0);
        break;
      case "Fórmula de Boileau et al.":
        if(this.tricipitalFold.nativeElement.value == "" || this.subscapularFold.nativeElement.value == "") { return; }
        this.tricipitalFold.nativeElement.value = AnthropometryService.round(+this.tricipitalFold.nativeElement.value, 0);
        this.subscapularFold.nativeElement.value = AnthropometryService.round(+this.subscapularFold.nativeElement.value, 0);
        break;
      default:
        throw new Error("Fórmula no válida");
    }
  }

  roundParam(param: string, throwError: boolean = true) {
    switch(param) {
      case "weight":
        if(this.weight.nativeElement.value == "") { return; }
        this.weight.nativeElement.value = AnthropometryService.round(+this.weight.nativeElement.value, 1);
        break;
      case "height":
        if(this.height.nativeElement.value == "") { return; }
        this.height.nativeElement.value = AnthropometryService.round(+this.height.nativeElement.value, 1);
        break;
      case "bicipitalFold":
        if(this.bicipitalFold.nativeElement.value == "") { return; }
        this.bicipitalFold.nativeElement.value = AnthropometryService.round(+this.bicipitalFold.nativeElement.value, 0);
        break;
      case "pectoralFold":
        if(this.pectoralFold.nativeElement.value == "") { return; }
        this.pectoralFold.nativeElement.value = AnthropometryService.round(+this.pectoralFold.nativeElement.value, 0);
        break;
      case "midaxillaryFold":
        if(this.midaxillaryFold.nativeElement.value == "") { return; }
        this.midaxillaryFold.nativeElement.value = AnthropometryService.round(+this.midaxillaryFold.nativeElement.value, 0);
        break;
      case "tricipitalFold":
        if(this.tricipitalFold.nativeElement.value == "") { return; }
        this.tricipitalFold.nativeElement.value = AnthropometryService.round(+this.tricipitalFold.nativeElement.value, 0);
        break;
      case "subscapularFold":
        if(this.subscapularFold.nativeElement.value == "") { return; }
        this.subscapularFold.nativeElement.value = AnthropometryService.round(+this.subscapularFold.nativeElement.value, 0);
        break;
      case "abdominalFold":
        if(this.abdominalFold.nativeElement.value == "") { return; }
        this.abdominalFold.nativeElement.value = AnthropometryService.round(+this.abdominalFold.nativeElement.value, 0);
        break;
      case "suprailiacFold":
        if(this.suprailiacFold.nativeElement.value == "") { return; }
        this.suprailiacFold.nativeElement.value = AnthropometryService.round(+this.suprailiacFold.nativeElement.value, 0);
        break;
      case "anteriorThighFold":
        if(this.anteriorThighFold.nativeElement.value == "") { return; }
        this.anteriorThighFold.nativeElement.value = AnthropometryService.round(+this.anteriorThighFold.nativeElement.value, 0);
        break;
      default:
        throw new Error("Parámetro no válido");
    }
    if(this.validate(throwError)) {
      this.calculate(throwError);
    }
  }

  formulaClick(formula:string) {
    if(!this.editable) return;
    switch(formula) {
      case "Fórmula de Jackson-Pollock de 3 pliegues":
        this.chooseFormula1(formula);
        break;
      case "Fórmula de Jackson-Pollock de 7 pliegues":
        this.chooseFormula1(formula);
        break;
      case "Fórmula de Durnin-Womersley":
        this.chooseFormula1(formula);
        break;
      case "Fórmula de Boileau et al.":
        this.chooseFormula1(formula);
        break;
      case "Fórmula de James":
        this.chooseFormula2(formula);
        break;
      case "Fórmula de Hume":
        this.chooseFormula2(formula);
        break;
      default:
        throw new Error("Fórmula no válida");
    }
  }
}
