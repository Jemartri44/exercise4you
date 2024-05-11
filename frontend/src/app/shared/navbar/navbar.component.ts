import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {

  constructor( protected router: Router ) { }

  goToGeneral() {
    console.debug("Redirecting to general");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2]]);
  }

  goToPatientList() {
    console.debug("Redirecting to patient list");
    this.router.navigate(['/pacientes']);
  }

  goToApalq() {
    console.debug("Redirecting to apalq");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/APALQ']);
  }

  goToIpaq() {
    console.debug("Redirecting to ipaq");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/IPAQ']);
  }

  goToIpaqe() {
    console.debug("Redirecting to ipaq-e");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/IPAQ-E']);
  }

  goToCmtcef() {
    console.debug("Redirecting to cmtcef");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/CMTCEF']);
  }

  goToEparmed() {
    console.debug("Redirecting to eparmed");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/ePARmed-X']);
  }

  goToIMC() {
    console.debug("Redirecting to IMC");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/IMC']);
  }

  goToICC() {
    console.debug("Redirecting to ICC");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/ICC']);
  }

  goToWaistCircumference() {
    console.debug("Redirecting to waist circumference");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/circunferencia-cintura']);
  }

  goToIdealWeight() {
    console.debug("Redirecting to ideal weight");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/peso-ideal']);
  }

  goToSkinFolds() {
    console.debug("Redirecting to skinfold measurement");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/medición-pliegues-cutáneos']);
  }
}
