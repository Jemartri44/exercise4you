import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HeaderComponent } from '../../../shared/header/header.component';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { ObjectivesService } from '../../../services/objectives/objectives.service';
import { Router } from '@angular/router';
import { ObjectivesListInfo } from '../../../model/objectives/objectives-list-info';
import { catchError, map, Observable, of, startWith } from 'rxjs';
import { ObjectivesSessionButtonsComponent } from '../objectives-session-buttons/objectives-session-buttons.component';

@Component({
  selector: 'app-objective-list',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    NavbarComponent,
    FooterComponent,
    ObjectivesSessionButtonsComponent
  ],
  templateUrl: './objective-list.component.html',
  styleUrl: './objective-list.component.css'
})
export class ObjectiveListComponent implements OnInit {

    notCompletedButtons: string[] = ['toComplete'];
    completedAndRepeteableButtons: string[] = ['toComplete', 'answered'];
    completedButtons: string[] = ['toComplete', 'answered'];
    errorMessage: string = "";
    today: any;
    objectivesState: Observable<{
      appState: string;
      appData?: ObjectivesListInfo;
      appError?: string;
    }> | undefined;

  constructor(private objectiveService: ObjectivesService, private router: Router) { }
  
    ngOnInit(): void {
      this.objectivesState = this.objectiveService.getObjectivesListInfo(this.router.url.split('/')[2]).pipe(
        map((questionnaireListInfo: ObjectivesListInfo) => {
          
          if(questionnaireListInfo === undefined ) {
            throw new Error('No se ha podido obtener la información del cuestionario');
          }
          if(!questionnaireListInfo.allEmpty) {
            questionnaireListInfo.sessions.sort((a, b) =>  a.number < b.number ? 1 : -1 );
          }
          this.today = questionnaireListInfo.today;
          return ({ appState: 'LOADED', appData: questionnaireListInfo })
        }),
        startWith({ appState: 'LOADING' }),
        catchError((error) => {
          return of({ appState: 'ERROR', appError: error.message})
        })
      );
    }


}
