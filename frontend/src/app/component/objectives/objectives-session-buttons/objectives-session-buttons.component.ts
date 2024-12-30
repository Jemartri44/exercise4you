import { CommonModule } from '@angular/common';
import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { PdfService } from '../../../services/pdf/pdf.service';

declare let $:any;

@Component({
  selector: 'app-objectives-session-buttons',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './objectives-session-buttons.component.html',
  styleUrl: './objectives-session-buttons.component.css'
})
export class ObjectivesSessionButtonsComponent {
  @Input() buttons: string[] = [];
    @Input() session: number = 0;
    @Input() date: string = "";
    @ViewChild('modalPdfViewer') modalPdfViewer: ElementRef;
    @ViewChild('modalRepeat') modalRepeat: ElementRef;
    loading_pdf: boolean = false;
  
    constructor( private router: Router, private pdfService: PdfService ) { }
  
    goToSetObjectives(session: number) {
      console.debug("Redirecting to set objectives "+ this.router.url.split('/')[3] + " session " + session);
      console.debug("Redirecting to: " + '/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + session + '/completar');
      this.router.navigate(['/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + '/' + session + '/completar']);
    }
  
    goToAnswers(session: number) {
      console.debug("Redirecting to see answers of "+ this.router.url.split('/')[3] + " session " + session);
      this.router.navigate(['/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + '/' + session + '/ver-objetivos']);
    }
  
    goToSeeReport(session: number) {
      console.debug("Redirecting to see report of "+ this.router.url.split('/')[3] + " session " + session);
      this.router.navigate(['/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + '/' + session + '/ver-informe']);
    }
  
    showModal(modal: string) {
      if(modal === "pdfViewer") {
        $(this.modalPdfViewer.nativeElement).modal('show');
      }
      if(modal === "repeat") {
        $(this.modalRepeat.nativeElement).modal('show');
      }
    }
  
    openPdf(session: number) {
      this.loading_pdf = true;
      this.pdfService.getPdf(this.router.url.split('/')[2], this.router.url.split('/')[3], session).subscribe(response => {
        this.loading_pdf = false;
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
