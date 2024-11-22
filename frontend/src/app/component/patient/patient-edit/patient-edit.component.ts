import { Component, ElementRef, Inject, OnInit, ViewChild } from '@angular/core';
import { HeaderComponent } from '../../../shared/header/header.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { FormBuilder, ReactiveFormsModule, Validators, FormsModule, FormControl } from '@angular/forms';
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
import { Observable, map, startWith, catchError, of, endWith, tap } from 'rxjs';
import { Patient } from '../../../model/patient/patient';
import { NavbarComponent } from "../../../shared/navbar/navbar.component";


@Component({
  selector: 'app-patient-edit',
  standalone: true,
  providers: [     {provide: MAT_DATE_LOCALE, useValue: 'es'},
    provideMomentDateAdapter(),
  ],
  imports: [HeaderComponent, FooterComponent, ReactiveFormsModule, CommonModule, MatFormFieldModule, MatInputModule, MatDatepickerModule, FormsModule, NavbarComponent],
  templateUrl: './patient-edit.component.html',
  styleUrl: './patient-edit.component.css'
})
export class PatientEditComponent implements OnInit{
  editPatientError: string='';
  editPatientState: string='';
  currentDate = new Date();
  errorMessage: string = "";
  patientState: Observable<{
    appState: string;
    appData?: Patient;
    appError?: string;
  }> | undefined;
  patientName: string;
  patientSurnames: string;
  patientGender: string;
  patientBirthdate: FormControl;
  @ViewChild('nameInput') nameInput: ElementRef;
  @ViewChild('surnamesInput') surnamesInput: ElementRef;
  @ViewChild('birthdateInput') birthdateInput: ElementRef;
  @ViewChild('genderInput') genderInput: ElementRef;

  constructor(
    private patientService: PatientService,
    private formBuilder: FormBuilder,
    private router:Router,
    private _adapter: DateAdapter<any>,
    private _intl: MatDatepickerIntl,
    @Inject(MAT_DATE_LOCALE) private _locale: string
  ) {  }

  ngOnInit(): void {
    let id = this.router.url.split('/')[2];
    this.patientState = this.patientService.getPatient(id).pipe(
      map((patient: Patient) => {
        this.patientName = patient.name;
        this.patientSurnames = patient.surnames;
        this.patientBirthdate = new FormControl(patient.birthdate);
        this.patientGender = patient.gender;
        this.editPatientForm.controls.name.markAsTouched();
        this.editPatientForm.controls.surnames.markAsTouched();
        this.editPatientForm.controls.birthdate.markAsTouched();
        this.editPatientForm.controls.birthdate.setValue(patient.birthdate.toString());
        this.editPatientForm.controls.gender.markAsTouched();
        this.editPatientForm.controls.gender.setValue(patient.gender);
        return ({ appState: 'LOADED', appData: patient })
      }),
      startWith({ appState: 'LOADING' }),
      catchError((error) => {
        return of({ appState: 'ERROR', appError: error.message})
      })
    );
  }

  editPatientForm = this.formBuilder.group({
    name: ['', [Validators.required, Validators.maxLength(32)]],
    surnames: ['', [Validators.required, Validators.maxLength(32)]],
    birthdate: ['', [Validators.required, Validators.maxLength(10),]],
    gender: ['', [Validators.required]]
  });

  editPatient(){
    if(!this.editPatientForm.valid){
      this.editPatientError = "Por favor, rellene todos los campos obligatorios correctamente";
      return;
    }
    if(this.editPatientForm.value.name == this.patientName && this.editPatientForm.value.surnames == this.patientSurnames && this.editPatientForm.value.birthdate == this.patientBirthdate.value && this.editPatientForm.value.gender == this.patientGender){
      this.editPatientError = "No se han realizado cambios";
      return;
    }
    this.editPatientState = 'LOADING';
    let patientRequest = this.editPatientForm.value as NewPatientRequest;
    let day, month, year;
    this.birthdateInput.nativeElement.value.split('/')[0].length == 1 ? day = '0'+this.birthdateInput.nativeElement.value.split('/')[0] : day = this.birthdateInput.nativeElement.value.split('/')[0];
    this.birthdateInput.nativeElement.value.split('/')[1].length == 1 ? month = '0'+this.birthdateInput.nativeElement.value.split('/')[1] : month = this.birthdateInput.nativeElement.value.split('/')[1];
    year = this.birthdateInput.nativeElement.value.split('/')[2];

    let birthdate = year + '-' + month + '-' + day;
    patientRequest.birthdate = birthdate;
    this.patientService.editPatient(patientRequest, this.router.url.split('/')[2]).subscribe({
      next: (editPatientData) => {console.log(editPatientData);},
      error: (errorData) => {
        this.editPatientState = 'ERROR';
        console.error(errorData);
        this.editPatientError = errorData.message;
      },
      complete: () => {
        this.editPatientState = '';
        console.info("Paciente añadido correctamente");
        this.router.navigateByUrl('/pacientes/' + this.router.url.split('/')[2]);
        this.editPatientForm.reset();
      }
    });
  }

  goToPatient(){
    console.debug("Redirigiendo al paciente");
    this.router.navigateByUrl('/pacientes/' + this.router.url.split('/')[2]);
  }

  get name(){
    return this.editPatientForm.controls.name;
  }

  get surnames(){
    return this.editPatientForm.controls.surnames;
  }

  get birthdate(){
    return this.editPatientForm.controls.birthdate;
  }

  get gender(){
    return this.editPatientForm.controls.gender;
  }
}
