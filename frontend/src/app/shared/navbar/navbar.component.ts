import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {

  constructor(private router: Router) { }

  goToPatientList() {
    console.debug("Redirecting to patient list");
    this.router.navigate(['/pacientes']);
  }

  goToIpaq() {
    console.debug("Redirecting to ipaq");
    this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/IPAQ']);
  }
}