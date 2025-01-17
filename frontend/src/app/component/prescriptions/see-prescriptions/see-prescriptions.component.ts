import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, map, Observable, of, startWith } from 'rxjs';
import { PrescriptionsService } from '../../../services/prescriptions/prescriptions.service';
import { PrescriptionsResponse } from '../../../model/prescriptions/prescriptions-response';
import { CommonModule } from '@angular/common';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { HeaderComponent } from '../../../shared/header/header.component';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';

@Component({
  selector: 'app-see-prescriptions',
  standalone: true,
  imports: [ HeaderComponent, NavbarComponent, FooterComponent, CommonModule ],
  templateUrl: './see-prescriptions.component.html',
  styleUrl: './see-prescriptions.component.css'
})
export class SeePrescriptionsComponent {
  prescriptionsState: Observable<{
    appState: string;
    appData?: any;
    appError?: string;
  }> | undefined;

  constructor( protected router: Router, private prescriptionsService: PrescriptionsService ) { }

  ngOnInit() {
    this.prescriptionsState = this.prescriptionsService.getPrescriptions( this.router.url.split('/')[2], this.router.url.split('/')[4]).pipe(
      map((prescriptions: PrescriptionsResponse) => {
        if(prescriptions === undefined ) {
          throw new Error('No se ha podido obtener la información de la prescripción');
        }
        return ({ appState: 'LOADED', appData: prescriptions })
      }),
      startWith({ appState: 'LOADING', questionState: 'LOADING' }),
      catchError((error) => {
        console.error(error);
        return of({ appState: 'ERROR', questionState: 'ERROR', appError: error.message})
      })
    );
  }

  volver(){
    let routeParts = this.router.url.split('/');
    let route = routeParts[1] + '/' + routeParts[2] + '/' + routeParts[3];
    this.router.navigate([route])
  }
}
