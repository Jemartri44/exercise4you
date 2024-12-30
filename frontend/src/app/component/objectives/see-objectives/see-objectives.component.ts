import { Component, OnInit } from '@angular/core';
import { HeaderComponent } from '../../../shared/header/header.component';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { catchError, map, Observable, of, startWith } from 'rxjs';
import { Router } from '@angular/router';
import { ObjectivesService } from '../../../services/objectives/objectives.service';
import { CommonModule } from '@angular/common';
import { ObjectivesResponse } from '../../../model/objectives/objectives-response';

@Component({
  selector: 'app-see-objectives',
  standalone: true,
  imports: [ HeaderComponent, NavbarComponent, FooterComponent, CommonModule ],
  templateUrl: './see-objectives.component.html',
  styleUrl: './see-objectives.component.css'
})
export class SeeObjectivesComponent implements OnInit {
  objectivesState: Observable<{
    appState: string;
    appData?: any;
    appError?: string;
  }> | undefined;

  constructor( protected router: Router, private objectivesService: ObjectivesService ) { }

  ngOnInit() {
    this.objectivesState = this.objectivesService.getObjectives( this.router.url.split('/')[2], this.router.url.split('/')[4]).pipe(
      map((objectives: ObjectivesResponse) => {
        if(objectives === undefined ) {
          throw new Error('No se ha podido obtener la información del cuestionario');
        }
        return ({ appState: 'LOADED', appData: objectives })
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
