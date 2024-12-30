import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, map, startWith, catchError, of } from 'rxjs';
import { Patient } from '../../model/patient/patient';
import { PatientService } from '../../services/patient/patient.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {

  navbarState: Observable<{
    appState: string;
    appData?: Patient;
    appError?: string;
  }> | undefined;

  constructor( protected router: Router, private patientService:PatientService ) { }

  ngOnInit(): void {
    let id = this.router.url.split('/')[2];
    this.navbarState = this.patientService.getPatient(id).pipe(
      map((patient: Patient) => {
        return ({ appState: 'LOADED', appData: patient })
      }),
      startWith({ appState: 'LOADING' }),
      catchError((error) => {
        return of({ appState: 'ERROR', appError: error.message})
      })
    );
  }

  getAge(birthdate: Date | undefined): number {
    if (!birthdate) return 0;
    let today = new Date();
    let birth = new Date(birthdate);
    let age = today.getFullYear() - birth.getFullYear();
    let m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age;
  }

  goToGeneral() {
    console.debug("Redirecting to general");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2]]);
  }

  goToPatientList() {
    console.debug("Redirecting to patient list");
    this.router.navigate(['/pacientes']);
  }

  goToApalq() {
    console.debug("Redirecting to apalq");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/APALQ']);
  }

  goToIpaq() {
    console.debug("Redirecting to ipaq");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/IPAQ']);
  }

  goToIpaqe() {
    console.debug("Redirecting to ipaq-e");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/IPAQ-E']);
  }

  goToCmtcef() {
    console.debug("Redirecting to cmtcef");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/CMTCEF']);
  }

  goToParq() {
    console.debug("Redirecting to par-q");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/PAR-Q']);
  }

  goToEparmed() {
    console.debug("Redirecting to eparmed");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/ePARmed-X']);
  }

  goToIMC() {
    console.debug("Redirecting to IMC");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/IMC']);
  }

  goToICC() {
    console.debug("Redirecting to ICC");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/ICC']);
  }

  goToWaistCircumference() {
    console.debug("Redirecting to waist circumference");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/circunferencia-cintura']);
  }

  goToIdealWeight() {
    console.debug("Redirecting to ideal weight");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/peso-ideal']);
  }

  goToSkinFolds() {
    console.debug("Redirecting to skinfold measurement");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/medición-pliegues-cutáneos']);
  }

  goToCardiorespiratoryResistance() {
    console.debug("Redirecting to cardiorespiratory resistance");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/resistencia-cardiorrespiratoria']);
  }

  goToStrengthResistance() {
    console.debug("Redirecting to strength resistance");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/fuerza-resistencia']);
  }

  goToFlexibility() {
    console.debug("Redirecting to flexibility");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/flexibilidad']);
  }

  goToNeuromuscular() {
    console.debug("Redirecting to neuromuscular");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/neuromuscular']);
  }

  goToObjectives() {
    console.debug("Redirecting to objectives");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/objetivos']);
  }
}
