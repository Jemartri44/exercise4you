import { Component } from '@angular/core';
import { Router } from '@angular/router';
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {

  constructor(private router:Router) {  }

  goToManual() {
    console.debug("Redirecting to manual")
    this.router.navigate(['/manual']);
  }

  goToAccount() {
    console.debug("Redirecting to account")
    this.router.navigate(['/account']);
  }

  goToLogout() {
    console.debug("Logging out and redirecting to login")
    this.router.navigate(['/logout']);
  }
}
