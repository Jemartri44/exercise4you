import { Component, Inject } from '@angular/core';
import { HeaderComponent } from '../../../shared/header/header.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { FormBuilder, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import {MatDatepickerIntl, MatDatepickerModule} from '@angular/material/datepicker';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {DateAdapter, MAT_DATE_LOCALE} from '@angular/material/core';
import {provideMomentDateAdapter} from '@angular/material-moment-adapter';
import { PatientService } from '../../../services/patient/patient.service';
import { NewPatientRequest } from '../../../services/patient/newPatientRequest';
import 'moment/locale/es';
import moment from 'moment';


@Component({
  selector: 'app-patient-new',
  standalone: true,
  providers: [     {provide: MAT_DATE_LOCALE, useValue: 'es'},
    provideMomentDateAdapter(),
  ],
  imports: [ HeaderComponent, FooterComponent, ReactiveFormsModule, CommonModule, MatFormFieldModule, MatInputModule, MatDatepickerModule, FormsModule ],
  templateUrl: './patient-new.component.html',
  styleUrl: './patient-new.component.css'
})
export class PatientNewComponent {
  newPatientError: string='';
  newPatientState: string='';
  currentDate = new Date();

  constructor(
    private patientService: PatientService,
    private formBuilder: FormBuilder,
    private router:Router,
    private _adapter: DateAdapter<any>,
    private _intl: MatDatepickerIntl,
    @Inject(MAT_DATE_LOCALE) private _locale: string
  ) {  }

  newPatientForm = this.formBuilder.group({
    name: ['', [Validators.required, Validators.maxLength(32)]],
    surnames: ['', [Validators.required, Validators.maxLength(32)]],
    birthdate: ['', [Validators.required, Validators.maxLength(10),]],
    gender: ['', [Validators.required]]
  });

  addNewPatient(){
    if(!this.newPatientForm.valid){
      this.newPatientError = "Por favor, rellene todos los campos obligatorios correctamente";
      return;
    }
    this.newPatientState = 'LOADING';
    let patientRequest = this.newPatientForm.value as NewPatientRequest;
    if (patientRequest.birthdate) {
      patientRequest.birthdate = moment(patientRequest.birthdate).format('YYYY-MM-DD');
    }
    this.patientService.addNewPatient(patientRequest).subscribe({
      next: (newPatientData) => {console.log(newPatientData);},
      error: (errorData) => {
        this.newPatientState = 'ERROR';
        console.error(errorData);
        this.newPatientError = errorData.message;
      },
      complete: () => {
        this.newPatientState = '';
        console.info("Paciente añadido correctamente");
        this.router.navigateByUrl('/pacientes');
        this.newPatientForm.reset();
      }
    });
  }

  goToPatients(){
    console.debug("Redirigiendo a la lista de pacientes");
    this.router.navigateByUrl('/pacientes');
  }

  get name(){
    return this.newPatientForm.controls.name;
  }

  get surnames(){
    return this.newPatientForm.controls.surnames;
  }

  get birthdate(){
    return this.newPatientForm.controls.birthdate;
  }

  get gender(){
    return this.newPatientForm.controls.gender;
  }
}
