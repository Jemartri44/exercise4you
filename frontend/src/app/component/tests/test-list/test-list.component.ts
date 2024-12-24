import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HeaderComponent } from '../../../shared/header/header.component';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { Router } from '@angular/router';
import { catchError, map, Observable, of, startWith } from 'rxjs';
import { Patient } from '../../../model/patient/patient';
import { PatientService } from '../../../services/patient/patient.service';

@Component({
  selector: 'app-test-list',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    NavbarComponent,
    FooterComponent,

  ],
  templateUrl: './test-list.component.html',
  styleUrl: './test-list.component.css'
})
export class TestListComponent implements OnInit {
  title: string = "Antropometría";

  patientState: Observable<{
    appState: string;
    appData?: Patient;
    appError?: string;
  }> | undefined;

  constructor( private patientService: PatientService, protected router: Router ) { }

  ngOnInit(): void {
    this.setTitle(this.router.url.split('/')[3]);
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

  setTitle(title: string) {
    console.debug(title);
    switch(title) {
      case 'resistencia-cardiorrespiratoria':
        this.title = "Pruebas de resistencia cardiorrespiratoria";
        break;
      case 'fuerza-resistencia':
        this.title = "Pruebas de fuerza-resistencia";
        break;
      case 'flexibilidad':
        this.title = "Pruebas de flexibilidad";
        break;
      case 'neuromuscular':
        this.title = "Pruebas neuromusculares";
        break;
    }
  }
}
