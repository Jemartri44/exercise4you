import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { HeaderComponent } from '../../../shared/header/header.component';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { CommonModule } from '@angular/common';
import { Prescription } from '../../../model/prescriptions/prescription';
import { Router } from '@angular/router';
import { Patient } from '../../../model/patient/patient';
import { PatientService } from '../../../services/patient/patient.service';
import { PrescriptionsService } from '../../../services/prescriptions/prescriptions.service';
import { PrescriptionRequest } from '../../../model/prescriptions/prescription-request';


@Component({
  selector: 'app-prescription-complete',
  standalone: true,
  imports: [ HeaderComponent, NavbarComponent, FooterComponent, CommonModule ],
  templateUrl: './prescription-complete.component.html',
  styleUrl: './prescription-complete.component.css'
})

export class PrescriptionCompleteComponent {
  populationGroup: string = "";
  askPregnant: boolean = false;
  hasDisease: boolean = false;
  diseaseGroup: string = "Enfermedades cardiovasculares";
  hasParkinson: boolean = false;
  selectedPrescriptions: {
    exercise: string,
    possibleExercises: string[],
    modality: string,
    possibleModalities: string[],
    frequency: string,
    possibleFrequencies: string[],
    intensity: string,
    possibleIntensities: string[],
    time: string,
    possibleTimes: string[],
    type: string,
    possibleTypes: string[],
    volume: string,
    possibleVolumes: string[]
  }[] = [];
  prescriptionSelectionState: string = "";
  prescriptionsLeft: string[][] = [];
  prescriptions: Prescription[];

  @ViewChild('modalRepeat') modalRepeat: ElementRef;
  patient: Patient;
  patientState: string = 'LOADING';

  constructor( protected router:Router, private patientService:PatientService, private prescriptionsService:PrescriptionsService ) { }

  ngOnInit(): void {
    let id = this.router.url.split('/')[2];
    this.patientService.getPatient(id).subscribe({
      next: (patient: Patient) => {
        if(patient === undefined ) {
          throw new Error('No se ha podido obtener la información del paciente');
        }
        if(patient.age < 18) {
          this.populationGroup = "Niños y adolescentes (5-17 años)";
        } else if (patient.age >= 18 && patient.age < 65) {
          this.populationGroup = "Adultos (18-65 años)";
          if(patient.gender === "Femenino") {
            this.askPregnant = true;
          }
        } else{
          this.populationGroup = "Mayores (> 65 años)";
        }
        this.patient = patient;
        this.patientState = 'LOADED';
      },
      error: (err) => {
        console.error('Error fetching patient:', err);
        this.patientState = 'ERROR';
      }
    });
  }

  checkPregnant() {
    let pregnant = (<HTMLInputElement>document.getElementById('pregnant')).value === "Sí" ? true : false;
    if((this.populationGroup == "Embarazadas" && pregnant) || (this.populationGroup !== "Embarazadas" && !pregnant)) {
      return;
    }
    this.hasParkinson = false;
    if(pregnant) {
      this.populationGroup = "Embarazadas";
    } else {
      this.populationGroup = "Adultos (18-65 años)";
    }
  }

  checkHasDisease() {
    let hasDiseases = (<HTMLInputElement>document.getElementById('hasDisease')).value === "Sí" ? true : false;
    if((this.hasDisease && hasDiseases)) {
      return;
    }
    this.hasParkinson = false;
    if(hasDiseases) {
      this.hasDisease = true;
    } else {
      this.hasDisease = false;
    }
    
  }

  checkDiseaseGroup() {
    let diseaseGroup = (<HTMLInputElement>document.getElementById('diseaseGroup')).value;
    if(this.diseaseGroup === diseaseGroup) {
      return;
    }
    this.diseaseGroup = (<HTMLInputElement>document.getElementById('diseaseGroup')).value;
    this.hasParkinson = false;
  }

  checkHasParkinson() {
    let hasAlzheimer = (<HTMLInputElement>document.getElementById('disease')).value === "Parkinson" ? true : false;
    if(hasAlzheimer) {
      this.hasParkinson = true;
    } else {
      this.hasParkinson = false;
    }
  }

  getPossiblePrescriptions() {
    let id = this.router.url.split('/')[2];
    this.prescriptionSelectionState = 'LOADING';
    this.prescriptionsService.getPossiblePrescriptions(id, this.populationGroup, (<HTMLInputElement>document.getElementById('disease')).value, (<HTMLInputElement>document.getElementById('level')).value).subscribe({
      next: (possiblePrescriptions: Prescription[]) => {
        this.prescriptions = possiblePrescriptions;
        for(let prescription of possiblePrescriptions) {
          if(!this.prescriptionsLeft.some(p => p[0] === prescription.exercise && p[1] === prescription.modality)){
            this.prescriptionsLeft.push([prescription.exercise, prescription.modality]);
          }
        }
        this.newPrescription();
        this.prescriptionSelectionState = "LOADED";
      },
      error: (err) => {
        console.error('Error fetching patient:', err);
        this.patientState = 'ERROR';
      }
    });
  }

  newPrescription() {
    if(this.prescriptionsLeft.length === 0) {
      throw new Error('No quedan más ejercicios disponibles');
    }
    let exercise = this.prescriptionsLeft[0][0];
    let modality = this.prescriptionsLeft[0][1];
    let possibleExercises: string[] = [];
    let possibleModalities: string[] = [];
    for(let prescription of this.prescriptionsLeft) {
      if(!possibleExercises.includes(prescription[0])) {
        possibleExercises.push(prescription[0]);
      }
      if(prescription[0] === exercise && !possibleModalities.includes(prescription[1])) {
        possibleModalities.push(prescription[1]);
      }
    }

    this.selectedPrescriptions.push({
      exercise: exercise,
      possibleExercises: possibleExercises,
      modality: modality,
      possibleModalities: possibleModalities,
      frequency: String(),
      possibleFrequencies: [],
      intensity: String(),
      possibleIntensities: [],
      time: String(),
      possibleTimes: [],
      type: String(),
      possibleTypes: [],
      volume: String(),
      possibleVolumes: [],
    });
    this.prescriptionsLeft.shift();
    let exerciseAvailable = false;
    for(let prescription of this.prescriptionsLeft) {
      exerciseAvailable = prescription[0] == exercise || exerciseAvailable;
    }
    for(let prescription of this.selectedPrescriptions) {
      if(prescription.exercise === exercise && prescription.modality !== modality) {
        prescription.possibleModalities.splice(prescription.possibleModalities.indexOf(modality, 0), 1);
      }else if(prescription.exercise !== exercise && prescription.possibleExercises.includes(exercise) && !exerciseAvailable) {
        prescription.possibleExercises.splice(prescription.possibleExercises.indexOf(exercise, 0), 1);
      }
    }

    this.updateParams(this.selectedPrescriptions.length - 1);
  }

  checkExercise(i: number) {
    let exercise = (<HTMLInputElement>document.getElementById('exercise'+i)).value;
    let oldExercise = this.selectedPrescriptions[i].exercise;
    if(exercise==oldExercise){    // If nothing has changed, return
      return;
    }
    // We search for a new modality for the new exercise
    let modality: string = "";
    let oldModality = this.selectedPrescriptions[i].modality;
    for(let prescriptionLeft of this.prescriptionsLeft) {
      if(prescriptionLeft[0] === exercise) {
        modality = prescriptionLeft[1];
        break;
      }
    }
    // We update the prescriptionsLeft array
    let toDelete: string[] = [];
    for(let prescriptionLeft of this.prescriptionsLeft) {
      if(prescriptionLeft[0] === exercise && prescriptionLeft[1] === modality) {
        toDelete = prescriptionLeft;
      }
    }
    this.prescriptionsLeft.splice(this.prescriptionsLeft.indexOf(toDelete, 0), 1);
    this.prescriptionsLeft.push([oldExercise, oldModality]);
    // We check if the new exercise will still be available (still has other modalities left)
    // and get the possible modalities for the new exercise
    let exerciseAvailable = false;
    let possibleModalities: string[] = [modality];
    for(let prescription of this.prescriptionsLeft) {
      if(prescription[0] == exercise) {
        exerciseAvailable = true;
        possibleModalities.push(prescription[1]);
      }
    }
    // We update the possible exercises and modalities for the all prescriptions
    for(let prescription of this.selectedPrescriptions) {
      if(prescription.exercise === oldExercise) { // Old exercise
        if(prescription.modality === oldModality) {  //     Old modality
          prescription.possibleModalities = possibleModalities;
        } else {                                  //     Other modality
          prescription.possibleModalities.push(oldModality);
        }
      }
      if(prescription.exercise === exercise) { // Other prescriptions with the new exercise
        prescription.possibleModalities.splice(prescription.possibleModalities.indexOf(modality, 0), 1);
        if(!prescription.possibleExercises.includes(exercise)) {
          prescription.possibleExercises.push(exercise);
        }
      } else if(prescription.exercise !== exercise && prescription.possibleExercises.includes(exercise) && !exerciseAvailable) {
        if(prescription.exercise === oldExercise && prescription.modality === oldModality) {
          continue;
        }
        prescription.possibleExercises.splice(prescription.possibleExercises.indexOf(exercise, 0), 1);
      }
      if((prescription.exercise !== oldExercise || prescription.modality !== oldModality) && !prescription.possibleExercises.includes(oldExercise)) { // Other prescriptions with other exercise or modality
        prescription.possibleExercises.push(oldExercise);
      }
    }
    this.selectedPrescriptions[i].exercise = exercise;
    this.selectedPrescriptions[i].modality = modality;

    this.updateParams(i);
  }

  checkModality(i: number) {
    let modality = (<HTMLInputElement>document.getElementById('modality'+i)).value;
    let exercise = (<HTMLInputElement>document.getElementById('exercise'+i)).value;
    let oldModality = this.selectedPrescriptions[i].modality;
    if(modality==oldModality){
      return;
    }
    let toDelete: string[] = [];
    for(let prescriptionLeft of this.prescriptionsLeft) {
      if(prescriptionLeft[0] === exercise && prescriptionLeft[1] === modality) {
        toDelete = prescriptionLeft;
      }
    }
    this.prescriptionsLeft.splice(this.prescriptionsLeft.indexOf(toDelete, 0), 1);
    this.prescriptionsLeft.push([exercise, oldModality]);
    for(let prescription of this.selectedPrescriptions) {
      if(prescription.exercise === exercise && prescription.modality !== oldModality) {
        prescription.possibleModalities.splice(prescription.possibleModalities.indexOf(modality, 0), 1);
        prescription.possibleModalities.push(oldModality);
      }
    }
    this.selectedPrescriptions[i].modality = modality;

    this.updateParams(i);
  }

  deletePrescription(i: number) {
    this.prescriptionsLeft.push([this.selectedPrescriptions[i].exercise, this.selectedPrescriptions[i].modality]);
    let exercise = this.selectedPrescriptions[i].exercise;
    let modality = this.selectedPrescriptions[i].modality;
    this.selectedPrescriptions.splice(i, 1);
    for(let prescription of this.selectedPrescriptions) {
      if(!prescription.possibleExercises.includes(exercise)) {
        prescription.possibleExercises.push(exercise);
      }
      if(prescription.exercise === exercise && !prescription.possibleModalities.includes(modality)) {
        prescription.possibleModalities.push(modality);
      }
    }
  }

  updateParams(i: number) {
    let exercise = this.selectedPrescriptions[i].exercise;
    let modality = this.selectedPrescriptions[i].modality;
    let possibleFrequencies: string[] = [];
    for(let prescription of this.prescriptions) {
      if(prescription.exercise === exercise && prescription.modality === modality && !possibleFrequencies.includes(prescription.frequency)) {
        possibleFrequencies.push(prescription.frequency);
      }
    }
    this.selectedPrescriptions[i].possibleFrequencies = possibleFrequencies;
    this.selectedPrescriptions[i].frequency = possibleFrequencies[0];
    this.checkFrequency(i, "", true);
  }

  checkFrequency(i: number, frequency: string = "", force: boolean = false) {
    frequency = force ? this.selectedPrescriptions[i].frequency : frequency;
    if(!force){
      if(frequency==this.selectedPrescriptions[i].frequency){
        return;
      }
      this.selectedPrescriptions[i].frequency = frequency;
    }
    let exercise = this.selectedPrescriptions[i].exercise;
    let modality = this.selectedPrescriptions[i].modality;
    let possibleIntensities: string[] = [];
    for(let prescription of this.prescriptions) {
      if(prescription.exercise === exercise && prescription.modality === modality && prescription.frequency === frequency && !possibleIntensities.includes(prescription.intensity)) {
        possibleIntensities.push(prescription.intensity);
      }
    }
    this.selectedPrescriptions[i].possibleIntensities = possibleIntensities;
    this.selectedPrescriptions[i].intensity = possibleIntensities[0];
    this.checkIntensity(i, "", true);
  }

  checkIntensity(i: number, intensity: string = "", force: boolean = false) {
    intensity = force ? this.selectedPrescriptions[i].intensity : intensity;
    if(!force){
      if(intensity==this.selectedPrescriptions[i].intensity){
        return;
      }
      this.selectedPrescriptions[i].intensity = intensity;
    }
    let exercise = this.selectedPrescriptions[i].exercise;
    let modality = this.selectedPrescriptions[i].modality;
    let frequency = this.selectedPrescriptions[i].frequency;
    let possibleTimes: string[] = [];
    for(let prescription of this.prescriptions) {
      if(prescription.exercise === exercise && prescription.modality === modality && prescription.frequency === frequency && prescription.intensity === intensity && !possibleTimes.includes(prescription.time)) {
        possibleTimes.push(prescription.time);
      }
    }
    this.selectedPrescriptions[i].possibleTimes = possibleTimes;
    this.selectedPrescriptions[i].time = possibleTimes[0];
    this.checkTime(i, "", true);
  }

  checkTime(i: number, time: string = "", force: boolean = false) {
    time = force ? this.selectedPrescriptions[i].time : time;
    if(!force){
      if(time==this.selectedPrescriptions[i].time){
        return;
      }
      this.selectedPrescriptions[i].time = time;
    }
    let exercise = this.selectedPrescriptions[i].exercise;
    let modality = this.selectedPrescriptions[i].modality;
    let frequency = this.selectedPrescriptions[i].frequency;
    let intensity = this.selectedPrescriptions[i].intensity;
    let possibleTypes: string[] = [];
    for(let prescription of this.prescriptions) {
      if(prescription.exercise === exercise && prescription.modality === modality && prescription.frequency === frequency && prescription.intensity === intensity && prescription.time === time && !possibleTypes.includes(prescription.type)) {
        possibleTypes.push(prescription.type);
      }
    }
    this.selectedPrescriptions[i].possibleTypes = possibleTypes;
    this.selectedPrescriptions[i].type = possibleTypes[0];
    this.checkType(i, "", true);
  }

  checkType(i: number, type:string = "", force: boolean = false) {
    type = force ? this.selectedPrescriptions[i].type : type;
    if(!force){
      if(type==this.selectedPrescriptions[i].type){
        return;
      }
      this.selectedPrescriptions[i].type = type;
    }
    let exercise = this.selectedPrescriptions[i].exercise;
    let modality = this.selectedPrescriptions[i].modality;
    let frequency = this.selectedPrescriptions[i].frequency;
    let intensity = this.selectedPrescriptions[i].intensity;
    let time = this.selectedPrescriptions[i].time;
    let possibleVolumes: string[] = [];
    for(let prescription of this.prescriptions) {
      if(prescription.exercise === exercise && prescription.modality === modality && prescription.frequency === frequency && prescription.intensity === intensity && prescription.time === time && prescription.type === type && !possibleVolumes.includes(prescription.volume)) {
        possibleVolumes.push(prescription.volume);
      }
    }
    this.selectedPrescriptions[i].possibleVolumes = possibleVolumes;
    this.selectedPrescriptions[i].volume = possibleVolumes[0];
    this.checkVolume(i, "", true);
  }

  checkVolume(i: number, volume: string = "", force: boolean = false) {
    volume = force ? this.selectedPrescriptions[i].volume : volume;
    if(volume==this.selectedPrescriptions[i].volume && !force){
      return;
    }
    this.selectedPrescriptions[i].volume = volume;
  }

  addLineBreaks(text: string, length: number = 100) {
    let words = text.split(' ');
    let lines = [];
    let line = "";
    for(let word in words) {
      if(line.length + words[word].length < length) {
        if(line.length > 0) {
          line += " ";
        }
        line += words[word];
      } else {
        lines.push(line);
        line = words[word] + " ";
      }
    }
    return lines.join('<br>').replace(/(\r\n|\n|\r)/gm, "<br>");
  }

  setPrescriptions() {
    let id = this.router.url.split('/')[2];
    let nSession = this.router.url.split('/')[4];
    this.prescriptionSelectionState = 'ENDING';
    let prescriptions: PrescriptionRequest[] = [];
    for(let prescription of this.selectedPrescriptions) {
      prescriptions.push({
        populationGroup: (<HTMLInputElement>document.getElementById('populationGroup')).value,
        disease: (<HTMLInputElement>document.getElementById('disease')).value,
        level: (<HTMLInputElement>document.getElementById('level')).value,
        exercise: prescription.exercise,
        modality: prescription.modality,
        frequency: prescription.frequency,
        intensity: prescription.intensity,
        time: prescription.time,
        type: prescription.type,
        volume: prescription.volume
      });
    }
    this.prescriptionsService.setPrescriptions(id, nSession, prescriptions).subscribe({
      next: (possiblePrescriptions: Prescription[]) => {
        this.prescriptionSelectionState = "ENDED";
      },
      complete: () => {
        this.router.navigateByUrl('pacientes/' + id + '/prescripciones/' + nSession + '/ver-prescripciones');
      },
      error: (err) => {
        console.error('Error fetching patient:', err);
        this.patientState = 'ERROR';
      }
    });
  }
}
