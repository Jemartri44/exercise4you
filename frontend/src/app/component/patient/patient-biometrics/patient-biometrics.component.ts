import { AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { PatientSessionComponent } from '../patient-session/patient-session.component';
import { CommonModule } from '@angular/common';
import { BiometricsGeneralData } from '../../../model/patient/biometrics-general-data';
import { PatientService } from '../../../services/patient/patient.service';
import { BiometricsData } from '../../../model/patient/biometrics-data';
import { Router } from '@angular/router';
import { map, startWith, catchError, of } from 'rxjs';
import { BiometricsAllData } from '../../../model/patient/biometrics-all-data';

@Component({
  selector: 'app-patient-biometrics',
  standalone: true,
  imports: [PatientSessionComponent, CommonModule],
  templateUrl: './patient-biometrics.component.html',
  styleUrl: './patient-biometrics.component.css'
})
export class PatientBiometricsComponent implements AfterViewInit {

  constructor(private biometricsService: PatientService, private router: Router) { }

  @Input() biometricsGeneralData: BiometricsGeneralData;
  @Input() sessions: string[];
  @Input() session: string;
  state: string = "LOADED";
  error: string = "";
  changesSaved: boolean = false;
  editable: boolean = true;
  tablesMode: boolean = false;

  @ViewChild('height') height: ElementRef;
  @ViewChild('weight') weight: ElementRef;
  @ViewChild('waistCircumference') waistCircumference: ElementRef;
  @ViewChild('hipCircumference') hipCircumference: ElementRef;
  @ViewChild('restingHeartRate') restingHeartRate: ElementRef;
  @ViewChild('restingRespiratoryFrequency') restingRespiratoryFrequency: ElementRef;
  @ViewChild('systolicBloodPressure') systolicBloodPressure: ElementRef;
  @ViewChild('diastolicBloodPressure') diastolicBloodPressure: ElementRef;
  @ViewChild('oxygenSaturation') oxygenSaturation: ElementRef;
  @ViewChild('glucose') glucose: ElementRef;
  @ViewChild('totalCholesterol') totalCholesterol: ElementRef;
  @ViewChild('hdlCholesterol') hdlCholesterol: ElementRef;
  @ViewChild('ldlCholesterol') ldlCholesterol: ElementRef;
  @ViewChild('triglycerides') triglycerides: ElementRef;

  elements: ElementRef[] = [];
  rounding: number[] = [1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1]

  ngAfterViewInit(): void {
    this.elements = [this.height, this.weight, this.waistCircumference, this.hipCircumference, this.restingHeartRate, this.restingRespiratoryFrequency, this.systolicBloodPressure, this.diastolicBloodPressure, this.oxygenSaturation, this.glucose, this.totalCholesterol, this.hdlCholesterol, this.ldlCholesterol, this.triglycerides];
    if(this.biometricsGeneralData.data !== undefined && this.biometricsGeneralData.data !== null) {
      this.setSessionData(this.biometricsGeneralData.data as BiometricsData);
    }
  }

  round() {
    this.error = "";
    this.changesSaved = false;
    for(let i = 0; i < this.elements.length; i++) {
      if(this.elements[i].nativeElement.value !== undefined && this.elements[i].nativeElement.value !== null && this.elements[i].nativeElement.value !== "") {
        if(this.elements[i].nativeElement.value <= 0) {
          this.elements[i].nativeElement.value = "";
        } else {
          this.elements[i].nativeElement.value = PatientService.round(+this.elements[i].nativeElement.value, this.rounding[i]);
        }
      }
    }
  }

  check(): boolean {
    let empty = true;
    for(let element of this.elements) {
      if(element.nativeElement.value !== undefined && element.nativeElement.value !== null && element.nativeElement.value !== "") {
        empty = false;
        if(+element.nativeElement.value < 0) {
          this.error = "Los valores no pueden ser negativos";
          return false;
        }
      }
    }
    if(empty) {
      this.error = "Debe rellenar al menos un campo";
      return false;
    }
    if(this.systolicBloodPressure.nativeElement.value !== undefined && this.systolicBloodPressure.nativeElement.value !== null && this.systolicBloodPressure.nativeElement.value !== "" && (this.diastolicBloodPressure.nativeElement.value === undefined || this.diastolicBloodPressure.nativeElement.value === null || this.diastolicBloodPressure.nativeElement.value === "")) {
      this.error = "Debe rellenar la presión arterial diastólica";
      return false;
    }
    if(this.diastolicBloodPressure.nativeElement.value !== undefined && this.diastolicBloodPressure.nativeElement.value !== null && this.diastolicBloodPressure.nativeElement.value !== "" && (this.systolicBloodPressure.nativeElement.value === undefined || this.systolicBloodPressure.nativeElement.value === null || this.systolicBloodPressure.nativeElement.value === "")) {
      this.error = "Debe rellenar la presión arterial sistólica";
      return false;
    }
    if(this.systolicBloodPressure.nativeElement.value !== undefined && this.systolicBloodPressure.nativeElement.value !== null && this.systolicBloodPressure.nativeElement.value !== "" && this.diastolicBloodPressure.nativeElement.value !== undefined && this.diastolicBloodPressure.nativeElement.value !== null && this.diastolicBloodPressure.nativeElement.value !== "" && +this.systolicBloodPressure.nativeElement.value < +this.diastolicBloodPressure.nativeElement.value) {
      this.error = "La presión arterial sistólica no puede ser menor que la diastólica";
      return false;
    }
    return true;
  }

  save() {
    this.round();
    if(this.check()) {
      let biometricsData: BiometricsData = {data: {}};
      if(this.height.nativeElement.value !== undefined && this.height.nativeElement.value !== null && this.height.nativeElement.value !== "") { 
        biometricsData.data.height = +this.height.nativeElement.value;
      }
      if(this.weight.nativeElement.value !== undefined && this.weight.nativeElement.value !== null && this.weight.nativeElement.value !== "") { 
        biometricsData.data.weight = +this.weight.nativeElement.value;
      }
      if(this.waistCircumference.nativeElement.value !== undefined && this.waistCircumference.nativeElement.value !== null && this.waistCircumference.nativeElement.value !== "") { 
        biometricsData.data.waistCircumference = +this.waistCircumference.nativeElement.value;
      }
      if(this.hipCircumference.nativeElement.value !== undefined && this.hipCircumference.nativeElement.value !== null && this.hipCircumference.nativeElement.value !== "") { 
        biometricsData.data.hipCircumference = +this.hipCircumference.nativeElement.value;
      }
      if(this.restingHeartRate.nativeElement.value !== undefined && this.restingHeartRate.nativeElement.value !== null && this.restingHeartRate.nativeElement.value !== "") { 
        biometricsData.data.restingHeartRate = +this.restingHeartRate.nativeElement.value;
      }
      if(this.restingRespiratoryFrequency.nativeElement.value !== undefined && this.restingRespiratoryFrequency.nativeElement.value !== null && this.restingRespiratoryFrequency.nativeElement.value !== "") { 
        biometricsData.data.restingRespiratoryFrequency = +this.restingRespiratoryFrequency.nativeElement.value;
      }
      if(this.systolicBloodPressure.nativeElement.value !== undefined && this.systolicBloodPressure.nativeElement.value !== null && this.systolicBloodPressure.nativeElement.value !== "") { 
        biometricsData.data.systolicBloodPressure = +this.systolicBloodPressure.nativeElement.value;
      }
      if(this.diastolicBloodPressure.nativeElement.value !== undefined && this.diastolicBloodPressure.nativeElement.value !== null && this.diastolicBloodPressure.nativeElement.value !== "") { 
        biometricsData.data.diastolicBloodPressure = +this.diastolicBloodPressure.nativeElement.value;
      }
      if(this.oxygenSaturation.nativeElement.value !== undefined && this.oxygenSaturation.nativeElement.value !== null && this.oxygenSaturation.nativeElement.value !== "") { 
        biometricsData.data.oxygenSaturation = +this.oxygenSaturation.nativeElement.value;
      }
      if(this.glucose.nativeElement.value !== undefined && this.glucose.nativeElement.value !== null && this.glucose.nativeElement.value !== "") { 
        biometricsData.data.glucose = +this.glucose.nativeElement.value;
      }
      if(this.totalCholesterol.nativeElement.value !== undefined && this.totalCholesterol.nativeElement.value !== null && this.totalCholesterol.nativeElement.value !== "") { 
        biometricsData.data.totalCholesterol = +this.totalCholesterol.nativeElement.value;
      }
      if(this.hdlCholesterol.nativeElement.value !== undefined && this.hdlCholesterol.nativeElement.value !== null && this.hdlCholesterol.nativeElement.value !== "") { 
        biometricsData.data.hdlCholesterol = +this.hdlCholesterol.nativeElement.value;
      }
      if(this.ldlCholesterol.nativeElement.value !== undefined && this.ldlCholesterol.nativeElement.value !== null && this.ldlCholesterol.nativeElement.value !== "") { 
        biometricsData.data.ldlCholesterol = +this.ldlCholesterol.nativeElement.value;
      }
      if(this.triglycerides.nativeElement.value !== undefined && this.triglycerides.nativeElement.value !== null && this.triglycerides.nativeElement.value !== "") { 
        biometricsData.data.triglycerides = +this.triglycerides.nativeElement.value;
      }
      this.state = "SAVING";

      this.biometricsService.saveData(this.router.url.split('/')[2], this.session.split(' ')[1], biometricsData).subscribe(
        (data: BiometricsData) => {
          this.state = "LOADED";
          this.changesSaved = true;
          this.error = "";
        },
        (error) => {
          this.state = "ERROR";
          this.error = "No se ha podido guardar la información";
          console.error(error);
        }
      )
    }
  }

  getSessionData(session: string) {
    if(this.session === this.sessions[0]) {
      this.editable = true;
    } else {
      this.editable = false;
    }
    this.state = "LOADING";
    this.biometricsService.getBiometricsData(this.router.url.split('/')[2], session).subscribe({
      next: (data) => {
        if(data === undefined || data === null) {
          throw new Error('No se ha podido obtener la información del cuestionario');
        }
        this.state = "LOADED";
        this.setSessionData(data);
      },
      error: (err) => {
        console.error("Error fetching biometrics data", err);
      }
    });
  }

  getAllSessionsData() {
    this.state = "LOADING";
    this.biometricsService.getAllBiometricsData(this.router.url.split('/')[2]).subscribe({
      next: (data) => {
        if(data === undefined || data === null) {
          throw new Error('No se ha podido obtener la información del cuestionario');
        }
        this.biometricsGeneralData.data = data;
        this.state = "LOADED";
      },
      error: (err) => {
        console.error("Error fetching biometrics data", err);
      }
    });
  }

  selectedOption($option: string) {
    this.session = $option;
    this.changesSaved = false;
    if ($option === "allSessions") {
      this.tablesMode = true;
      this.getAllSessionsData();
    } else {
      this.tablesMode = false;
      this.getSessionData($option.split(' ')[1]);
    }
  }

  setSessionData(biometricsData: BiometricsData) {
    let data = biometricsData.data;
    if(data === undefined || data === null) {
      return;
    }
    if(data.height !== undefined && data.height !== null) {
      this.height.nativeElement.value = data.height;
    } else {
      this.height.nativeElement.value = "";
    }
    if(data.weight !== undefined && data.weight !== null) {
      this.weight.nativeElement.value = data.weight;
    } else {
      this.weight.nativeElement.value = "";
    }
    if(data.waistCircumference !== undefined && data.waistCircumference !== null) {
      this.waistCircumference.nativeElement.value = data.waistCircumference;
    } else {
      this.waistCircumference.nativeElement.value = "";
    }
    if(data.hipCircumference !== undefined && data.hipCircumference !== null) {
      this.hipCircumference.nativeElement.value = data.hipCircumference;
    } else {
      this.hipCircumference.nativeElement.value = "";
    }
    if(data.restingHeartRate !== undefined && data.restingHeartRate !== null) {
      this.restingHeartRate.nativeElement.value = data.restingHeartRate;
    } else {
      this.restingHeartRate.nativeElement.value = "";
    }
    console.debug(data)
    console.debug(data.restingRespiratoryFrequency);
    if(data.restingRespiratoryFrequency !== undefined && data.restingRespiratoryFrequency !== null) {
      this.restingRespiratoryFrequency.nativeElement.value = data.restingRespiratoryFrequency;
    } else {
      this.restingRespiratoryFrequency.nativeElement.value = "";
    }
    if(data.systolicBloodPressure !== undefined && data.systolicBloodPressure !== null) {
      this.systolicBloodPressure.nativeElement.value = data.systolicBloodPressure;
    } else {
      this.systolicBloodPressure.nativeElement.value = "";
    }
    if(data.diastolicBloodPressure !== undefined && data.diastolicBloodPressure !== null) {
      this.diastolicBloodPressure.nativeElement.value = data.diastolicBloodPressure;
    } else {
      this.diastolicBloodPressure.nativeElement.value = "";
    }
    if(data.oxygenSaturation !== undefined && data.oxygenSaturation !== null) {
      this.oxygenSaturation.nativeElement.value = data.oxygenSaturation;
    } else {
      this.oxygenSaturation.nativeElement.value = "";
    }
    if(data.glucose !== undefined && data.glucose !== null) {
      this.glucose.nativeElement.value = data.glucose;
    } else {
      this.glucose.nativeElement.value = "";
    }
    if(data.totalCholesterol !== undefined && data.totalCholesterol !== null) {
      this.totalCholesterol.nativeElement.value = data.totalCholesterol;
    } else {
      this.totalCholesterol.nativeElement.value = "";
    }
    if(data.hdlCholesterol !== undefined && data.hdlCholesterol !== null) {
      this.hdlCholesterol.nativeElement.value = data.hdlCholesterol;
    } else {
      this.hdlCholesterol.nativeElement.value = "";
    }
    if(data.ldlCholesterol !== undefined && data.ldlCholesterol !== null) {
      this.ldlCholesterol.nativeElement.value = data.ldlCholesterol;
    } else {
      this.ldlCholesterol.nativeElement.value = "";
    }
    if(data.triglycerides !== undefined && data.triglycerides !== null) {
      this.triglycerides.nativeElement.value = data.triglycerides;
    } else {
      this.triglycerides.nativeElement.value = "";
    }
  }

  get biometricsAllData(): BiometricsAllData {
    return this.biometricsGeneralData.data as BiometricsAllData;
  }
}
