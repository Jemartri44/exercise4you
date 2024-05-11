import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { AnthropometryData } from '../../model/anthropometry/anthropometry-data';
import { HttpClient } from '@angular/common/http';
import { AnthropometryAllData } from '../../model/anthropometry/anthropometry-all-data';
import { AnthropometryGeneralData } from '../../model/anthropometry/anthropometry-general-data';
import { SkinFolds } from '../../model/anthropometry/skin-folds';

@Injectable({
  providedIn: 'root'
})
export class AnthropometryService {

  constructor( private http:HttpClient ) { }

  getAnthropometryGeneralData(calculator: string, patientId: string): Observable<AnthropometryGeneralData> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/anthropometry/" + calculator;
    return this.http.get<AnthropometryGeneralData>(url)
  }

  getAnthropometryData(calculator: string, patientId: string, session: string): Observable<AnthropometryData> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/anthropometry/" + calculator + "/get-session/" + session;
    return this.http.get<AnthropometryData>(url)
  }

  getAllAnthropometryData(calculator: string, patientId: string): Observable<AnthropometryAllData> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/anthropometry/" + calculator + "/all-sessions";
    return this.http.get<AnthropometryAllData>(url)
  }

  saveData(calculator: string, patientId: string, session: string, data: AnthropometryData): Observable<AnthropometryGeneralData> {
    let url = environment.apiUrl+"/pacientes/" + patientId + "/anthropometry/" + calculator + "/save-data/" + session;
    return this.http.post<AnthropometryGeneralData>(url, data)
  }

  round(value: number, decimals: number): number {
    if(decimals == 0) return Math.round(value + Number.EPSILON);
    let factor = Math.pow(10, decimals);
    return Math.round((value + Number.EPSILON) * factor) / factor;
  }

  calculateImc(weight: number, height: number): {imc: number, imcp: number, category: string, risk: string} {
    let imc = this.round(weight / (height * height), 1);
    let imcp = this.round(imc / 25, 1);
    let category = this.getImcCategory(imc);
    let risk = this.getImcRisk(imc);
    return {imc, imcp, category, risk};
  }

  getImcCategory(imc: number): string {
    if (imc < 18.5) return "Infrapeso";
    if (imc < 25) return "Normal";
    if (imc < 30) return "Sobrepeso";
    if (imc < 35) return "Obesidad grado 1";
    if (imc < 40) return "Obesidad grado 2";
    return "Obesidad grado 3";
  }

  getImcRisk(imc: number): string {
    if (imc < 18.5) return "-";
    if (imc < 25) return "Promedio";
    if (imc < 30) return "Aumentado";
    if (imc < 35) return "Moderado";
    if (imc < 40) return "Severo";
    return "Muy severo";
  }

  calculateIcc(waistCircumference: number, hipCircumference: number, gender: boolean): {icc: number, category: string, risk: string} {
    let icc = this.round(waistCircumference / hipCircumference, 2);
    let category = this.getIccCategory(icc, gender);
    let risk = this.getIccRisk(icc, gender);
    return {icc, category, risk};
  }

  getIccCategory(icc: number, gender: boolean): string {
    if(gender) {
      if (icc > 1) return "Obesidad androide"
      return "Obesidad ginecoide"
    }
    if (icc > 0.85) return "Obesidad androide"
    return "Obesidad ginecoide"
  }

  getIccRisk(icc: number, gender: boolean): string {
    if(gender) {
      if (icc > 1) return "Alto"
      if (icc < 0.95) return "Bajo"
      return "Moderado"
    }
    if (icc > 0.85) return "Alto"
    if (icc < 0.8) return "Bajo"
    return "Moderado"
  }

  calculateWaistCircumference(waistCircumference: number, gender: boolean): {risk: string} {
    let risk = this.getWaistCircumferenceRisk(waistCircumference, gender);
    return {risk};
  }

  getWaistCircumferenceRisk(waistCircumference: number, gender: boolean): string {
    if(gender) {
      if (waistCircumference > 102) return "Alto"
      if (waistCircumference < 94) return "Bajo"
      return "Moderado"
    }
    if (waistCircumference > 88) return "Alto"
    if (waistCircumference < 80) return "Bajo"
    return "Moderado"
  }

  calculateIdealWeight(formula: string, weight: number, height: number, gender: boolean): {idealWeight: number, difference: number} {
    let idealWeight;
    console.debug("Formula: " + formula + ", weight: " + weight + ", height" + height);
    switch (formula) {
      case "Fórmula de Lorentz":
        if(gender) {
          idealWeight = this.round((height - 100) - ((height - 150) / 4), 1);
          return {idealWeight, difference: this.round(weight - idealWeight, 1)};
        }
        idealWeight = this.round((height - 100) - ((height - 150) / 2), 1);
        return {idealWeight, difference: this.round(weight - idealWeight, 1)};
      case "Fórmula de la Metropolitan Life Insurance Company":
        idealWeight = this.round(50 + (0.75 * (height - 150)), 1);
        return {idealWeight, difference: this.round(weight - idealWeight, 1)};
      default:
        throw new Error("Fórmula no reconocida");
    }
  }

  calculateSkinFolds(formula1: string, formula2: string, foldsSum:number, weight: number, height: number, gender: boolean, age: number | undefined): {density: number, fatMassPercentage: number, fatMass: number, fatFreeMass: number, fatLevel: string, leanMass: number} {
    if(age === undefined) throw new Error("No se pudo calcular.");
    console.debug("Formula1: " + formula1 + ", formula2: " + formula2 + ", foldsSum: " + foldsSum + ", weight: " + weight + ", height" + height + ", gender: " + gender + ", age" + age);
    let density, fatMassPercentage, fatMass, fatFreeMass, fatLevel, leanMass;
    switch (formula1) {
      case "Fórmula de Jackson-Pollock de 3 pliegues":
        if(gender) {
          density = this.round(1.10938 - (0.0008267 * foldsSum) + (0.0000016 * foldsSum * foldsSum) - (0.0002574 * age), 4);
        } else {
          density = this.round(1.0994921 - (0.0009929 * foldsSum) + (0.0000023 * foldsSum * foldsSum) - (0.0001392 * age), 4);
        }
        break;
      case "Fórmula de Jackson-Pollock de 7 pliegues":
        if(gender) {
          density = this.round(1.112 - (0.00043499 * foldsSum) + (0.00000055 * foldsSum * foldsSum) - (0.00028826 * age), 4);
        } else {
          density = this.round(1.097 - (0.00046971 * foldsSum) + (0.00000056 * foldsSum * foldsSum) - (0.00012828 * age), 4);
        }
        break;
      case "Fórmula de Durnin-Womersley":
        let c = 0;
        let m = 0;
        if(gender) {
          if(age >= 17) {
            c = 1.1620;
            m = 0.0630;
          }
          if(age >= 20) {
            c = 1.1631;
            m = 0.0632;
          }
          if(age >= 30) {
            c = 1.1422;
            m = 0.0544;
          }
          if(age >= 40) {
            c = 1.1620;
            m = 0.0700;
          }
          if(age >= 50) {
            c = 1.1715;
            m = 0.0779;
          }
        } else {
          if(age >= 16) {
            c = 1.1549;
            m = 0.0678;
          }
          if(age >= 20) {
            c = 1.1599;
            m = 0.0717;
          }
          if(age >= 30) {
            c = 1.1423;
            m = 0.0632;
          }
          if(age >= 40) {
            c = 1.1333;
            m = 0.0645;
          }
          if(age >= 50) {
            c = 1.1339;
            m = 0.0645;
          }
        }
        density = this.round(c - (m * Math.log10(foldsSum)), 4);
        break;
      case "Fórmula de Boileau et al.":
        density = 0;
        break;
      default:
        throw new Error("Fórmula no reconocida");
      }
    if(age >= 18) {
      if(age <= 60) { // FÓRMULA DE SIRI
        fatMassPercentage = this.round(((4.95 / density) - 4.5) * 100, 1);
      } else { // FÓRMULA DE BROZEK
        fatMassPercentage = this.round(((4.57 / density) - 4.14) * 100, 1);
      }
    } else if (gender){
      fatMassPercentage = this.round((1.35 * foldsSum) - (0.012 * Math.pow(foldsSum,2)) - 4.4, 1);
    } else {
      fatMassPercentage = this.round((1.35 * foldsSum) - (0.012 * Math.pow(foldsSum,2)) - 2.4, 1);
    }
    fatMass = this.round(fatMassPercentage * weight / 100, 1);
    fatFreeMass = this.round(weight - fatMass, 1);
    fatLevel = this.getSkinFoldsFatLevel(fatMassPercentage, gender, age);
    if(gender) {
      if(formula2 == "Fórmula de James") {
        leanMass = this.round((1.1 * weight) - (128 * Math.pow(weight,2) / Math.pow(height,2)), 1);
      } else if(formula2 == "Fórmula de Hume") {
        leanMass = this.round((0.32810 * weight) + (0.33929 * height) - 29.5336, 1);
      } else {
        throw new Error("Fórmula no reconocida");
      }
    } else if(formula2 == "Fórmula de James"){
      leanMass = this.round((1.07 * weight) - (148 * Math.pow(weight,2) / Math.pow(height,2)), 1);
    } else if(formula2 == "Fórmula de Hume") {
      leanMass = this.round((0.29569 * weight) + (0.41813 * height) - 43.2933, 1);
    } else {
      throw new Error("Fórmula no reconocida");
    }
    return {density, fatMassPercentage, fatMass, fatFreeMass, fatLevel, leanMass};
  }

  private girls: number[][] = [
    []
  ]

  getSkinFoldsFatLevel(fatMassPercentage: number, gender: boolean, age: number): string {
    return "Por implementar";
  }
}
