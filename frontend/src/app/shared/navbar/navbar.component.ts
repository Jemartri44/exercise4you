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

  goToPatientList() {
    console.debug("Redirecting to patient list");
    this.router.navigate(['/pacientes']);
  }

  goToIpaq() {
    console.debug("Redirecting to ipaq");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/IPAQ']);
  }

  goToEparmed() {
    console.debug("Redirecting to eparmed");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/ePARmed-X']);
  }
}
