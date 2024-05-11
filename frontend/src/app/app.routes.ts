import { Routes } from '@angular/router';
import { LoginComponent } from './component/auth/login/login.component';
import { RegisterComponent } from './component/auth/register/register.component';
import { PatientListComponent } from './component/patient/patient-list/patient-list.component';
import { authGuard } from './guard/auth.guard';
import { PatientInfoComponent } from './component/patient/patient-info/patient-info.component';
import { QuestionnaireListComponent } from './component/questionnaire/questionnaire-list/questionnaire-list.component';
import { QuestionnaireCompleteComponent } from './component/questionnaire/questionnaire-complete/questionnaire-complete.component';
import { QuestionnaireAnswersComponent } from './component/questionnaire/questionnaire-answers/questionnaire-answers.component';
import { AnthropometryComponent } from './component/anthropometry/anthropometry/anthropometry.component';

export const routes: Routes = [
    { path: 'login', component: LoginComponent, canActivate: [authGuard] },
    { path: 'register', component: RegisterComponent, canActivate: [authGuard] },
    { path: 'pacientes', component: PatientListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id', component: PatientInfoComponent, canActivate: [authGuard] },

    { path: 'pacientes/:id/IPAQ', component: QuestionnaireListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/IPAQ/:session/completar', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/IPAQ/:session/repetir', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/IPAQ/:session/ver-respuestas', component: QuestionnaireAnswersComponent, canActivate: [authGuard] },

    { path: 'pacientes/:id/ePARmed-X', component: QuestionnaireListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/ePARmed-X/:session/completar', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/ePARmed-X/:session/repetir', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/ePARmed-X/:session/ver-respuestas', component: QuestionnaireAnswersComponent, canActivate: [authGuard] },

    { path: 'pacientes/:id/IMC', component: AnthropometryComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/ICC', component: AnthropometryComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/circunferencia-cintura', component: AnthropometryComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/peso-ideal', component: AnthropometryComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/medición-pliegues-cutáneos', component: AnthropometryComponent, canActivate: [authGuard] },
    //{ path: 'manual', component: ManualComponent, canActivate: [authGuard] },
    { path: '', redirectTo: '/pacientes', pathMatch: 'full'},
    { path: '**', redirectTo: '/pacientes', pathMatch: 'full'}
 
];
