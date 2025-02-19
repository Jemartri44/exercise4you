import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { HeaderComponent } from '../../../shared/header/header.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { PatientService } from '../../../services/patient/patient.service';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Observable, catchError, map, of, startWith } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { Patient } from '../../../model/patient/patient';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { PatientBiometricsComponent } from "../patient-biometrics/patient-biometrics.component";
import { GeneralData } from '../../../model/patient/general-data';
import { BiometricsGeneralData } from '../../../model/patient/biometrics-general-data';


@Component({
  selector: 'app-patient-info',
  standalone: true,
  imports: [HeaderComponent, FooterComponent, NavbarComponent, RouterModule, FormsModule, CommonModule, PatientBiometricsComponent],
  templateUrl: './patient-info.component.html',
  styleUrl: './patient-info.component.css'
})
export class PatientInfoComponent implements OnInit {

  errorMessage: string = "";
  patientState: Observable<{
    appState: string;
    appData?: Patient;
    appError?: string;
  }> | undefined;
  biometricsGeneralData: BiometricsGeneralData;
  sessions: string[] = [];
  session: string = "";


  constructor(private patientService: PatientService, private router: Router) { }

  ngOnInit(): void {
    let id = this.router.url.split('/')[2];
    this.patientState = this.patientService.getPatientGeneralData(id).pipe(
      map((patientGeneralData: GeneralData) => {
        this.biometricsGeneralData = patientGeneralData.biometricsGeneralData;
        let sessions: string[] = [];
        sessions.push("Sesión " + this.biometricsGeneralData.today.number + " - " + this.biometricsGeneralData.today.date + " (hoy)");
        for (let session of this.biometricsGeneralData.sessions) {
          sessions.push("Sesión " + session.number + " - " + session.date);
        }
        this.sessions = sessions;
        this.session = "Sesión " + this.biometricsGeneralData.today.number + " - " + this.biometricsGeneralData.today.date + " (hoy)";
        return ({ appState: 'LOADED', appData: patientGeneralData.patient });
      }),
      startWith({ appState: 'LOADING' }),
      catchError((error) => {
        return of({ appState: 'ERROR', appError: error.message})
      })
    );
  }

  goToEditPatient() {
    let id = this.router.url.split('/')[2];
    this.router.navigate(['/pacientes/' + id + '/editar']);
  }

}
