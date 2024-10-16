import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { PdfService } from '../../services/pdf/pdf.service';
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {

  constructor( private router:Router, private authService:AuthService, private pdfService:PdfService ) {  }

  goToLogout() {
    console.debug("Logging out and redirecting to login")
    this.authService.logout();
  }

  openManual() {
    this.pdfService.getManual().subscribe(response => {
      if(response.body == null) {
        console.error("Error: PDF is null");
        return;
      }
      var file = new Blob([response.body], { type: 'application/pdf' });
      var fileURL = URL.createObjectURL(file);
      console.log(response.headers.keys());
      window.open(fileURL, '_blank');
    });
  }
}
