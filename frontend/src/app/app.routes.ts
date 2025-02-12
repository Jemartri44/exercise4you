import { Routes } from '@angular/router';
import { LoginComponent } from './component/auth/login/login.component';
import { RegisterComponent } from './component/auth/register/register.component';
import { VerificationComponent } from './component/auth/verification/verification.component';
import { PatientListComponent } from './component/patient/patient-list/patient-list.component';
import { PatientNewComponent } from './component/patient/patient-new/patient-new.component';
import { authGuard } from './guard/auth.guard';
import { PatientInfoComponent } from './component/patient/patient-info/patient-info.component';
import { QuestionnaireListComponent } from './component/questionnaire/questionnaire-list/questionnaire-list.component';
import { QuestionnaireCompleteComponent } from './component/questionnaire/questionnaire-complete/questionnaire-complete.component';
import { QuestionnaireAnswersComponent } from './component/questionnaire/questionnaire-answers/questionnaire-answers.component';
import { AnthropometryComponent } from './component/anthropometry/anthropometry/anthropometry.component';
import { ForgottenPasswordComponent } from './component/auth/forgotten-password/forgotten-password.component';
import { ChangePasswordComponent } from './component/auth/change-password/change-password.component';
import { PatientEditComponent } from './component/patient/patient-edit/patient-edit.component';
import { PrivacyPolicyComponent } from './component/auth/privacy-policy/privacy-policy.component';
import { TestListComponent } from './component/tests/test-list/test-list.component';
import { ObjectiveListComponent } from './component/objectives/objective-list/objective-list.component';
import { ObjectiveCompleteComponent } from './component/objectives/objective-complete/objective-complete.component';
import { SeeObjectivesComponent } from './component/objectives/see-objectives/see-objectives.component';
import { PrescriptionListComponent } from './component/prescriptions/prescription-list/prescription-list.component';
import { PrescriptionCompleteComponent } from './component/prescriptions/prescription-complete/prescription-complete.component';
import { SeePrescriptionsComponent } from './component/prescriptions/see-prescriptions/see-prescriptions.component';

export const routes: Routes = [
    { path: 'login', component: LoginComponent, canActivate: [authGuard] },
    { path: 'register', component: RegisterComponent, canActivate: [authGuard] },
    { path: 'confirmar-registro', component: VerificationComponent, canActivate: [authGuard] },
    { path: 'solicitar-cambio-contrasena', component: ForgottenPasswordComponent, canActivate: [authGuard] },
    { path: 'cambiar-contrasena', component: ChangePasswordComponent, canActivate: [authGuard] },
    { path: 'politica-de-privacidad', component: PrivacyPolicyComponent, canActivate: [authGuard] },

    { path: 'pacientes', component: PatientListComponent, canActivate: [authGuard] },
    { path: 'pacientes/nuevo', component: PatientNewComponent, canActivate: [authGuard]},
    { path: 'pacientes/:id', component: PatientInfoComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/editar', component: PatientEditComponent, canActivate: [authGuard]},

    { path: 'pacientes/:id/APALQ', component: QuestionnaireListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/APALQ/:session/completar', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/APALQ/:session/repetir', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/APALQ/:session/ver-respuestas', component: QuestionnaireAnswersComponent, canActivate: [authGuard] },

    { path: 'pacientes/:id/IPAQ', component: QuestionnaireListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/IPAQ/:session/completar', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/IPAQ/:session/repetir', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/IPAQ/:session/ver-respuestas', component: QuestionnaireAnswersComponent, canActivate: [authGuard] },

    { path: 'pacientes/:id/IPAQ-E', component: QuestionnaireListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/IPAQ-E/:session/completar', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/IPAQ-E/:session/repetir', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/IPAQ-E/:session/ver-respuestas', component: QuestionnaireAnswersComponent, canActivate: [authGuard] },

    { path: 'pacientes/:id/CMTCEF', component: QuestionnaireListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/CMTCEF/:session/completar', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/CMTCEF/:session/repetir', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/CMTCEF/:session/ver-respuestas', component: QuestionnaireAnswersComponent, canActivate: [authGuard] },

    { path: 'pacientes/:id/PAR-Q', component: QuestionnaireListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/PAR-Q/:session/completar', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/PAR-Q/:session/repetir', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/PAR-Q/:session/ver-respuestas', component: QuestionnaireAnswersComponent, canActivate: [authGuard] },

    { path: 'pacientes/:id/ePARmed-X', component: QuestionnaireListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/ePARmed-X/:session/completar', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/ePARmed-X/:session/repetir', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/ePARmed-X/:session/ver-respuestas', component: QuestionnaireAnswersComponent, canActivate: [authGuard] },

    { path: 'pacientes/:id/SF-36', component: QuestionnaireListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/SF-36/:session/completar', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/SF-36/:session/repetir', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/SF-36/:session/ver-respuestas', component: QuestionnaireAnswersComponent, canActivate: [authGuard] },

    { path: 'pacientes/:id/PedsQL', component: QuestionnaireListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/PedsQL/:session/completar', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/PedsQL/:session/repetir', component: QuestionnaireCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/PedsQL/:session/ver-respuestas', component: QuestionnaireAnswersComponent, canActivate: [authGuard] },

    { path: 'pacientes/:id/IMC', component: AnthropometryComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/ICC', component: AnthropometryComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/circunferencia-cintura', component: AnthropometryComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/peso-ideal', component: AnthropometryComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/medición-pliegues-cutáneos', component: AnthropometryComponent, canActivate: [authGuard] },

    { path: 'pacientes/:id/resistencia-cardiorrespiratoria', component: TestListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/fuerza-resistencia', component: TestListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/flexibilidad', component: TestListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/neuromuscular', component: TestListComponent, canActivate: [authGuard] },

    { path: 'pacientes/:id/objetivos', component: ObjectiveListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/objetivos/:session/completar', component: ObjectiveCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/objetivos/:session/ver-objetivos', component: SeeObjectivesComponent, canActivate: [authGuard] },

    { path: 'pacientes/:id/prescripciones', component: PrescriptionListComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/prescripciones/:session/completar', component: PrescriptionCompleteComponent, canActivate: [authGuard] },
    { path: 'pacientes/:id/prescripciones/:session/ver-prescripciones', component: SeePrescriptionsComponent, canActivate: [authGuard] },
    
    { path: '', redirectTo: '/pacientes', pathMatch: 'full'},
    { path: '**', redirectTo: '/pacientes', pathMatch: 'full'}
 
];
