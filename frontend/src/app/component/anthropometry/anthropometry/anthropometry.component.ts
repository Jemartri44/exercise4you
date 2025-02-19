import { Component, OnInit, ViewChild } from '@angular/core';
import { HeaderComponent } from '../../../shared/header/header.component';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { Router } from '@angular/router';
import { DataRecordComponent } from '../data-record/data-record.component';
import { CommonModule } from '@angular/common';
import { ImcIntroductionComponent } from '../introduction/imc-introduction/imc-introduction.component';
import { IccIntroductionComponent } from '../introduction/icc-introduction/icc-introduction.component';
import { WaistCircumferenceIntroductionComponent } from '../introduction/waist-circumference-introduction/waist-circumference-introduction.component';
import { IdealWeightIntroductionComponent } from '../introduction/ideal-weight-introduction/ideal-weight-introduction.component';
import { SkinFoldsIntroductionComponent } from '../introduction/skin-folds-introduction/skin-folds-introduction.component';
import { ImcEndingComponent } from '../ending/imc-ending/imc-ending.component';
import { IccEndingComponent } from '../ending/icc-ending/icc-ending.component';
import { WaistCircumferenceEndingComponent } from '../ending/waist-circumference-ending/waist-circumference-ending.component';
import { IdealWeightEndingComponent } from '../ending/ideal-weight-ending/ideal-weight-ending.component';
import { SkinFoldsEndingComponent } from '../ending/skin-folds-ending/skin-folds-ending.component';
import { ImcSessionComponent } from '../session/imc-session/imc-session.component';
import { IccSessionComponent } from '../session/icc-session/icc-session.component';
import { WaistCircumferenceSessionComponent } from '../session/waist-circumference-session/waist-circumference-session.component';
import { IdealWeightSessionComponent } from '../session/ideal-weight-session/ideal-weight-session.component';
import { SkinFoldsSessionComponent } from '../session/skin-folds-session/skin-folds-session.component';
import { Observable, catchError, map, of, startWith } from 'rxjs';
import { AnthropometryGeneralData } from '../../../model/anthropometry/anthropometry-general-data';
import { AnthropometryAllData } from '../../../model/anthropometry/anthropometry-all-data';
import { AnthropometryData } from '../../../model/anthropometry/anthropometry-data';
import { AnthropometryService } from '../../../services/anthropometry/anthropometry.service';
import { ImcAllSessionsComponent } from '../all-sessions/imc-all-sessions/imc-all-sessions.component';
import { IccAllSessionsComponent } from '../all-sessions/icc-all-sessions/icc-all-sessions.component';
import { WaistCircumferenceAllSessionsComponent } from '../all-sessions/waist-circumference-all-sessions/waist-circumference-all-sessions.component';
import { IdealWeightAllSessionsComponent } from '../all-sessions/ideal-weight-all-sessions/ideal-weight-all-sessions.component';
import { SkinFoldsAllSessionsComponent } from '../all-sessions/skin-folds-all-sessions/skin-folds-all-sessions.component';

@Component({
  selector: 'app-anthropometry',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    NavbarComponent,
    FooterComponent,
    DataRecordComponent,
    ImcIntroductionComponent,
    IccIntroductionComponent,
    WaistCircumferenceIntroductionComponent,
    IdealWeightIntroductionComponent,
    SkinFoldsIntroductionComponent,
    ImcSessionComponent,
    IccSessionComponent,
    WaistCircumferenceSessionComponent,
    IdealWeightSessionComponent,
    SkinFoldsSessionComponent,
    ImcEndingComponent,
    IccEndingComponent,
    WaistCircumferenceEndingComponent,
    IdealWeightEndingComponent,
    SkinFoldsEndingComponent,
    ImcAllSessionsComponent,
    IccAllSessionsComponent,
    WaistCircumferenceAllSessionsComponent,
    IdealWeightAllSessionsComponent,
    SkinFoldsAllSessionsComponent
  ],
  templateUrl: './anthropometry.component.html',
  styleUrl: './anthropometry.component.css'
})
export class AnthropometryComponent implements OnInit {

  title: string = "Antropometría";
  subtitle: string = "";
  option: string = "";
  sessions: string[] = [];
  session: string = "";
  allSessionsData: AnthropometryAllData;
  @ViewChild(DataRecordComponent) dataRecordComponent: DataRecordComponent;
  @ViewChild('calculator') calculator: ImcSessionComponent | IccSessionComponent | WaistCircumferenceSessionComponent | IdealWeightSessionComponent //| SkinFoldsSessionComponent | undefined;
  anthropometryState: Observable<{
    appState: string;
    dataState: string;
    saveState: string;
    error?: string;
    data?: AnthropometryData | AnthropometryAllData | null;
  }> | undefined;

  constructor( protected router: Router, private anthropometryService: AnthropometryService ) { }

  ngOnInit(): void {
    this.setSubtitle(this.router.url.split('/')[3]);
    this.anthropometryState = this.anthropometryService.getAnthropometryGeneralData( this.router.url.split('/')[3], this.router.url.split('/')[2]).pipe(
      map((anthropometryGeneralData: AnthropometryGeneralData) => {
        if(anthropometryGeneralData === undefined ) {
          throw new Error('No se ha podido obtener la información del cuestionario');
        }
        let sessions: string[] = [];
        sessions.push("Sesión " + anthropometryGeneralData.today.number + " - " + anthropometryGeneralData.today.date + " (hoy)");
        for (let session of anthropometryGeneralData.sessions) {
          sessions.push("Sesión " + session.number + " - " + session.date);
        }
        this.sessions = sessions;
        this.session = "Sesión " + anthropometryGeneralData.today.number + " - " + anthropometryGeneralData.today.date + " (hoy)";
        return ({ appState: 'LOADED', dataState: 'LOADED', saveState: 'LOADED', data: anthropometryGeneralData.data })
      }),
      startWith({ appState: 'LOADING', dataState: 'LOADING', saveState: 'LOADING'}),
      catchError((error) => {
        console.error(error);
        return of({ appState: 'ERROR', dataState: 'ERROR', saveState: 'ERROR', error: error.message})
      })
    );
  }

  getSessionData(session: string) {
    this.anthropometryState = this.anthropometryService.getAnthropometryData( this.router.url.split('/')[3], this.router.url.split('/')[2], session).pipe(
      map((data: AnthropometryData) => {
        if(data === undefined || data === null) {
          throw new Error('No se ha podido obtener la información del cuestionario');
        }
        return ({ appState: 'LOADED', dataState: 'LOADED', saveState: 'LOADED', data: data })
      }),
      startWith({ appState: 'LOADED', dataState: 'LOADING', saveState: 'LOADING'}),
      catchError((error) => {
        console.error(error);
        return of({ appState: 'ERROR', dataState: 'ERROR', saveState: 'ERROR', error: error.message})
      })
    );
  }

  getAllSessionsData() {
    this.anthropometryState = this.anthropometryService.getAllAnthropometryData( this.router.url.split('/')[3], this.router.url.split('/')[2]).pipe(
      map((data: AnthropometryAllData) => {
        if(data === undefined || data === null) {
          throw new Error('No se ha podido obtener la información del cuestionario');
        }
        this.allSessionsData = data;
        return ({ appState: 'LOADED', dataState: 'LOADED', saveState: 'LOADED', data: data })
      }),
      startWith({ appState: 'LOADED', dataState: 'LOADING', saveState: 'LOADING'}),
      catchError((error) => {
        console.error(error);
        return of({ appState: 'ERROR', dataState: 'ERROR', saveState: 'ERROR', error: error.message})
      })
    );
  }

  selectedOption($option: string) {
    this.session = $option;
    if ($option === "allSessions") {
      this.getAllSessionsData();
    } else {
      this.getSessionData($option.split(' ')[1]);
    }
  }

  save() {
    if(this.calculator === undefined) {
      throw new Error('No se ha podido obtener la información del cuestionario');
    }
    this.calculator.changesSaved = false;
    let data = this.calculator.getData();
    console.debug(data)
    if(data === null) {
      return;
    }
    console.debug(data);
    this.anthropometryState = this.anthropometryService.saveData(this.router.url.split('/')[3], this.router.url.split('/')[2], this.session.split(' ')[1] ,data).pipe(
      map(() => {
        this.calculator.changesSaved = true;
        return { appState: 'LOADED', dataState: 'LOADED', saveState: 'LOADED' };
      }),
      startWith({ appState: 'LOADED', dataState: 'LOADED', saveState: 'LOADING'}),
      catchError((error) => {
        console.error(error);
        return of({ appState: 'LOADED', dataState: 'LOADED', saveState: 'ERROR', error: error.message})
      })
    );
  }

  setSubtitle(currentOption: string) {
    switch (currentOption) {
      case "IMC":
        this.subtitle = "Índice de masa corporal";
        break;
      case "ICC":
        this.subtitle = "Índice cintura-cadera";
        break;
      case "circunferencia-cintura":
        this.subtitle = "Circunferencia de la cintura";
        break;
      case "peso-ideal":
        this.subtitle = "Peso ideal";
        break;
      case "medici%C3%B3n-pliegues-cut%C3%A1neos":
        this.subtitle = "Medición de pliegues cutáneos";
        break;
      default:
        this.subtitle = "";
        break;
    }
  }

}
