import { Component, OnInit } from '@angular/core';
import { HeaderComponent } from '../../../shared/header/header.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { PatientService } from '../../../services/patient/patient.service';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Observable, catchError, map, of, startWith } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { Patient } from '../../../model/patient/patient';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';


@Component({
  selector: 'app-patient-info',
  standalone: true,
  imports: [ HeaderComponent, FooterComponent, NavbarComponent, RouterModule, FormsModule, CommonModule ],
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

  constructor(private patientService: PatientService, private router: Router) { }

  ngOnInit(): void {
    let id = this.router.url.split('/')[2];
    this.patientState = this.patientService.getPatient(id).pipe(
      map((patient: Patient) => {
        return ({ appState: 'LOADED', appData: patient })
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
