import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { HeaderComponent } from '../../../shared/header/header.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { PatientService } from '../../../services/patient/patient.service';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { PatientPage } from '../../../model/patient/patient-page';
import { Observable, catchError, map, of, startWith } from 'rxjs';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-patient-list',
  standalone: true,
  imports: [ HeaderComponent, FooterComponent, RouterModule, FormsModule, CommonModule ],
  templateUrl: './patient-list.component.html',
  styleUrl: './patient-list.component.css'
})
export class PatientListComponent implements OnInit {

  hasPatients: boolean = false;
  @ViewChild('search') searchBox: ElementRef;
  search:string = "";
  errorMessage: string = "";
  displayedColumns: string[] = ['id', 'name', 'surnames', 'gender', 'birthdate'];
  patientsState: Observable<{
    appState: string;
    appData?: PatientPage;
    appError?: string;
  }> | undefined;

  constructor(private patientService: PatientService, private router: Router) { }

  ngOnInit(): void {
    this.patientsState = this.patientService.getPatients("").pipe(
      map((patientPage: PatientPage) => {
        this.hasPatients = patientPage.totalElements > 0;
        return ({ appState: 'LOADED', appData: patientPage })
      }),
      startWith({ appState: 'LOADING' }),
      catchError((error) => {
        return of({ appState: 'ERROR', appError: error.message})
      })
    );
  }

  goToPage(name?:string, page?: number, data?: PatientPage) {
    name = this.searchBox.nativeElement.value
    if(!name) {
      name = "";
    }
    if(!page) {
      page = 0;
    }else {
      page = page;
    }
    this.patientsState = this.patientService.getPatients(name, page).pipe(
      map((patientPage: PatientPage) => {
        return ({ appState: 'LOADED', appData: patientPage })
      }),
      startWith({ appState: 'PAGINATING', appData: data }),
      catchError((error) => {
        return of({ appState: 'ERROR', appError: error.message})
      })
    );
  }

  goToPatient(id: string) {
    console.debug("Redirecting to patient with id: " + id);
    this.router.navigate(['/pacientes/' + id]);
  }

  goToNewPatient() {
    console.debug("Redirecting to new patient");
    this.router.navigate(['/pacientes/nuevo']);
  }

  goToNext(patientPage?: PatientPage) {
    if(patientPage) {
      this.goToPage(undefined, (this.wholeDiv(patientPage.number, 5) * 5 + 5), patientPage);
    }
  }

  goToPrev(patientPage?: PatientPage) {
    if(patientPage) {
      this.goToPage(undefined, this.wholeDiv(patientPage.number, 5)* 5 - 1, patientPage);
    }
  }

  // Returns true if the previous button should be shown (if the current page is not within the first 5 pages), else returns false
  showPrev(patientPage: PatientPage | undefined) {
    if (patientPage) {
      return patientPage.number/5 >= 1;
    }
    return false;
  }

  // Returns true if the next button should be shown (if the current page is not within the last 5 pages), else returns false
  showNext(patientPage: PatientPage | undefined) {
    if (patientPage) {
      return this.wholeDiv(patientPage.number, 5) != this.wholeDiv(patientPage.totalPages - 1, 5);
    }
    return false;
  }

  getPageIndex(patientPage: PatientPage | undefined) {
    if (patientPage) {
      return "Mostrando "+(patientPage.size * patientPage.number + 1) + "-" + 
        Math.min((patientPage.size * (patientPage.number + 1)),patientPage.totalElements) + " de " + patientPage.totalElements;
    }
    return "";
  }

  getPages(patientPage: PatientPage | undefined) {
    if (patientPage) {
      let toReturn = [];
      for (let i = this.wholeDiv(patientPage.number, 5) * 5; i < patientPage.totalPages && i < this.wholeDiv(patientPage.number, 5) * 5 + 5; i++) {
        toReturn.push(i+1);
      }
      return toReturn;
    }
    return undefined;
  }

  private wholeDiv(a: number, b:number) {
    return (a - a % b) / b;
  }
}
