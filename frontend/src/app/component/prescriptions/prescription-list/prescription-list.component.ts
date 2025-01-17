import { Component } from '@angular/core';
import { PrescriptionsService } from '../../../services/prescriptions/prescriptions.service';
import { PrescriptionsListInfo } from '../../../model/prescriptions/prescriptions-list-info';
import { catchError, map, Observable, of, startWith } from 'rxjs';
import { Router } from '@angular/router';
import { HeaderComponent } from '../../../shared/header/header.component';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { CommonModule } from '@angular/common';
import { PrescriptionsSessionButtonsComponent } from '../prescriptions-session-buttons/prescriptions-session-buttons.component';

@Component({
  selector: 'app-prescription-list',
  standalone: true,
  imports: [ HeaderComponent, NavbarComponent, FooterComponent, CommonModule, PrescriptionsSessionButtonsComponent ],
  templateUrl: './prescription-list.component.html',
  styleUrl: './prescription-list.component.css'
})
export class PrescriptionListComponent {
    notCompletedButtons: string[] = ['toComplete'];
    completedAndRepeteableButtons: string[] = ['toComplete', 'answered'];
    completedButtons: string[] = ['toComplete', 'answered'];
    errorMessage: string = "";
    today: any;
    prescriptionsState: Observable<{
      appState: string;
      appData?: PrescriptionsListInfo;
      appError?: string;
    }> | undefined;

  constructor(private prescriptionsService: PrescriptionsService, private router: Router) { }
  
    ngOnInit(): void {
      this.prescriptionsState = this.prescriptionsService.getPrescriptionsListInfo(this.router.url.split('/')[2]).pipe(
        map((prescriptionsListInfo: PrescriptionsListInfo) => {
          
          if(prescriptionsListInfo === undefined ) {
            throw new Error('No se ha podido obtener la información de las prescripciones');
          }
          if(!prescriptionsListInfo.allEmpty) {
            prescriptionsListInfo.sessions.sort((a, b) =>  a.number < b.number ? 1 : -1 );
          }
          this.today = prescriptionsListInfo.today;
          return ({ appState: 'LOADED', appData: prescriptionsListInfo })
        }),
        startWith({ appState: 'LOADING' }),
        catchError((error) => {
          return of({ appState: 'ERROR', appError: error.message})
        })
      );
    }

}
