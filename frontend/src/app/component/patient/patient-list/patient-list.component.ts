import { Component } from '@angular/core';
import { HeaderComponent } from '../../../shared/header/header.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { Patient } from '../../../model/patient';
import { PatientService } from '../../../services/patient/patient.service';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { Router, RouterModule } from '@angular/router';

export interface PeriodicElement {
  name: string;
  position: number;
  weight: number;
  symbol: string;
}

@Component({
  selector: 'app-patient-list',
  standalone: true,
  imports: [HeaderComponent, FooterComponent, MatTableModule, RouterModule],
  templateUrl: './patient-list.component.html',
  styleUrl: './patient-list.component.css'
})
export class PatientListComponent {
  errorMessage:string="no error";
  patients!:MatTableDataSource<Patient>;
  displayedColumns: string[] = ['name', 'surnames', 'gender', 'birthdate'];

  constructor(private patientService:PatientService, private router:Router) {
    this.patientService.getPatients().subscribe({
      next: (patients) => {
        this.patients = new MatTableDataSource(patients);
      },
      error: (error) => {
        this.errorMessage = error.message;
      },
      complete: () => {
        console.debug('Petición completada');
      }
    })
  }

  goToPatient() {
    console.debug("Redirecting to patient")
    this.router.navigate(['/login']);
  }
}
