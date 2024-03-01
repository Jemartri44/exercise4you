import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { PatientListComponent } from './component/patient/patient-list/patient-list.component';
import { authGuard } from './auth/guard/auth.guard';

export const routes: Routes = [
    { path: 'login', component: LoginComponent, canActivate: [authGuard] },
    { path: 'register', component: RegisterComponent, canActivate: [authGuard] },
    { path: 'pacientes', component: PatientListComponent, canActivate: [authGuard] },
    { path: '', redirectTo: '/pacientes', pathMatch: 'full'},
    { path: '**', redirectTo: '/pacientes', pathMatch: 'full'}

];
