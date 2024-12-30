import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { HeaderComponent } from '../../../shared/header/header.component';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { Router } from '@angular/router';
import { PatientService } from '../../../services/patient/patient.service';
import { Patient } from '../../../model/patient/patient';
import { ObjectivesService } from '../../../services/objectives/objectives.service';
import { Objective } from '../../../model/objectives/objective';
import { ObjectiveRequest } from '../../../model/objectives/objective-request';

declare let $:any;

@Component({
  selector: 'app-objective-complete',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    NavbarComponent,
    FooterComponent
  ],
  templateUrl: './objective-complete.component.html',
  styleUrl: './objective-complete.component.css'
})
export class ObjectiveCompleteComponent implements OnInit{
  populationGroup: string = "";
  askPregnant: boolean = false;
  hasDisease: boolean = false;
  diseaseGroup: string = "Enfermedades cardiovasculares";
  selectedObjectives: {
    possibleObjectives: string[],
    possibleTests: string[],
    objective: string,
    range: string,
    testOrQuestionnaire: string
  }[] = [];
  objectiveSelectionState: string = "";
  objectivesLeft: string[] = [];
  objectivesUsed: string[] = [];
  objectives: Objective[];

  @ViewChild('modalRepeat') modalRepeat: ElementRef;
  patient: Patient;
  patientState: string = 'LOADING';

  constructor( protected router:Router, private patientService:PatientService, private objectivesService:ObjectivesService ) { }

  ngOnInit(): void {
    let id = this.router.url.split('/')[2];
    this.patientService.getPatient(id).subscribe({
      next: (patient: Patient) => {
        if(patient === undefined ) {
          throw new Error('No se ha podido obtener la información del paciente');
        }
        if(patient.age < 18) {
          this.populationGroup = "Niños y Adolescentes";
        } else if (patient.age >= 18 && patient.age < 65) {
          this.populationGroup = "Adultos";
          if(patient.gender === "Femenino") {
            this.askPregnant = true;
          }
        } else{
          this.populationGroup = "Mayores";
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
    if(pregnant) {
      this.populationGroup = "Embarazadas";
    } else {
      this.populationGroup = "Adultos";
    }
  }

  checkHasDisease() {
    let hasDiseases = (<HTMLInputElement>document.getElementById('hasDisease')).value === "Sí" ? true : false;
    if(hasDiseases) {
      this.hasDisease = true;
    } else {
      this.hasDisease = false;
    }
  }

  checkDiseaseGroup() {
    this.diseaseGroup = (<HTMLInputElement>document.getElementById('diseaseGroup')).value;
  }

  getPossibleObjectives() {
    let id = this.router.url.split('/')[2];
    this.objectiveSelectionState = 'LOADING';
    this.objectivesService.getPossibleObjectives(id, this.populationGroup, (<HTMLInputElement>document.getElementById('disease')).value).subscribe({
      next: (possibleObjectives: Objective[]) => {
        this.objectives = possibleObjectives;
        for(let objective of possibleObjectives) {
          if(!this.objectivesLeft.includes(objective.objective)){
            this.objectivesLeft.push(objective.objective);
          }
        }
        this.objectivesUsed = [];
        this.newObjective();
        this.objectiveSelectionState = "LOADED";
      },
      error: (err) => {
        console.error('Error fetching patient:', err);
        this.patientState = 'ERROR';
      }
    });
  }

  newObjective() {
    if(this.objectivesLeft.length === 0) {
      throw new Error('No quedan objetivos posibles');
    }
    let possibleTests: string[] = [];
    for(let objective of this.objectives) {
      if(objective.objective === this.objectivesLeft[0]) {
        if(!possibleTests.includes(objective.testOrQuestionnaire)){
          possibleTests.push(objective.testOrQuestionnaire);
        }
      }
    }

    let range: string = "";
    if(this.selectedObjectives.length === 0) {
      range = "Conservador";
    } else {
      range = this.selectedObjectives[this.selectedObjectives.length - 1].range;
    }

    let objectivesLeftCloned:string[] = [];
    this.objectivesLeft.forEach(val => objectivesLeftCloned.push(val));
    this.objectivesUsed.push(this.objectivesLeft[0]);

    this.selectedObjectives.push({
      possibleObjectives: objectivesLeftCloned,
      possibleTests: possibleTests,
      objective: objectivesLeftCloned[0],
      range: range,
      testOrQuestionnaire: possibleTests[0]
    });

    this.objectivesLeft.shift();
    this.updateObjectives();
  }

  updateObjectives() {
    console.debug(this.objectivesLeft);
    for(let selectedObjective of this.selectedObjectives) {
      let possibleObjectivesCloned:string[] = [];
      this.objectives.forEach(val => !possibleObjectivesCloned.includes(val.objective) ? possibleObjectivesCloned.push(val.objective):null);
      for(let objectiveUsed of this.objectivesUsed) {
        if(objectiveUsed !== selectedObjective.objective) {
          possibleObjectivesCloned.splice(possibleObjectivesCloned.indexOf(objectiveUsed, 0), 1);
        }
      }
      selectedObjective.possibleObjectives = possibleObjectivesCloned;
    }
  }

  selectObjective(objective: string) {
    this.selectedObjectives[this.selectedObjectives.length - 1].objective = objective;
  }

  deleteObjective(i: number) {
    this.objectivesLeft.push(this.selectedObjectives[i].objective);
    this.objectivesUsed.splice(this.objectivesUsed.indexOf(this.selectedObjectives[i].objective, 0), 1);
    this.selectedObjectives.splice(i, 1);
    this.updateObjectives();
  }

  checkObjective(i: number) {
    let objectiveChosen = (<HTMLInputElement>document.getElementById('objective'+i)).value;
    if(objectiveChosen==this.selectedObjectives[i].objective){
      return;
    }
    console.debug("START");
    console.debug("Objectives left")
    console.debug(this.objectivesLeft);
    this.objectivesLeft.push(this.selectedObjectives[i].objective);
    console.debug(this.objectivesLeft);
    console.debug("Objectives used")
    console.debug(this.objectivesUsed);
    this.objectivesUsed.splice(this.objectivesUsed.indexOf(this.selectedObjectives[i].objective, 0), 1);
    console.debug(this.objectivesUsed);
    let possibleTests: string[] = [];
    for(let objective of this.objectives) {
      if(objective.objective === objectiveChosen) {
        if(!possibleTests.includes(objective.testOrQuestionnaire)){
          possibleTests.push(objective.testOrQuestionnaire);
        }
      }
    }
    let objectivesLeftCloned:string[] = [];
    this.objectivesLeft.forEach(val => objectivesLeftCloned.push(val));
    this.objectivesUsed.push(objectiveChosen);
    console.debug(this.objectivesUsed);

    this.selectedObjectives[i] = {
      possibleObjectives: objectivesLeftCloned,
      possibleTests: possibleTests,
      objective: objectiveChosen,
      range: this.selectedObjectives[i].range,
      testOrQuestionnaire: possibleTests[0]
    };

    this.objectivesLeft.splice(this.objectivesLeft.indexOf(objectiveChosen, 0), 1);
    console.debug(this.objectivesLeft);
    this.updateObjectives();
  }

  checkRange(i: number) {
    let range = (<HTMLInputElement>document.getElementById('range'+i)).value;
    if(range==this.selectedObjectives[i].range){
      return;
    }
    this.selectedObjectives[i].range = range;
  }

  setObjectives() {
    let id = this.router.url.split('/')[2];
    let nSession = this.router.url.split('/')[4];
    this.objectiveSelectionState = 'ENDING';
    let objectives: ObjectiveRequest[] = [];
    for(let i = 0; i < this.selectedObjectives.length; i++) {
      objectives.push({
        populationGroup: this.populationGroup,
        disease: (<HTMLInputElement>document.getElementById('disease')).value,
        objective: (<HTMLInputElement>document.getElementById('objective'+i)).value,
        range: (<HTMLInputElement>document.getElementById('range'+i)).value,
        testOrQuestionnaire: (<HTMLInputElement>document.getElementById('test'+i)).value
      });
    }
    this.objectivesService.setObjectives(id, nSession, objectives).subscribe({
      next: (possibleObjectives: Objective[]) => {
        this.objectiveSelectionState = "ENDED";
      },
      complete: () => {
        this.router.navigateByUrl('pacientes/' + id + '/objetivos/' + nSession + '/ver-objetivos');
      },
      error: (err) => {
        console.error('Error fetching patient:', err);
        this.patientState = 'ERROR';
      }
    });
  }

  showModal(modal: string) {
    if(modal === "repeat") {
      $(this.modalRepeat.nativeElement).modal('show');
    }
  }

}
