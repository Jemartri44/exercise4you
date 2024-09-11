import { CommonModule } from '@angular/common';
import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { PdfService } from '../../../services/pdf/pdf.service';

declare let $:any;

@Component({
  selector: 'app-session-buttons',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './session-buttons.component.html',
  styleUrl: './session-buttons.component.css'
})
export class SessionButtonsComponent {
  @Input() buttons: string[] = [];
  @Input() session: number = 0;
  @Input() date: string = "";
  @ViewChild('modalPdfViewer') modalPdfViewer: ElementRef;
  @ViewChild('modalRepeat') modalRepeat: ElementRef;

  constructor( private router: Router, private pdfService: PdfService ) { }

  goToCompleteQuestionnaire(session: number) {
    console.debug("Redirecting to complete questionnaire "+ this.router.url.split('/')[3] + " session " + session);
    console.debug("Redirecting to: " + '/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + session + '/completar');
    this.router.navigate(['/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + '/' + session + '/completar']);
  }

  goToAnswers(session: number) {
    console.debug("Redirecting to see answers of "+ this.router.url.split('/')[3] + " session " + session);
    this.router.navigate(['/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + '/' + session + '/ver-respuestas']);
  }

  goToSeeReport(session: number) {
    console.debug("Redirecting to see report of "+ this.router.url.split('/')[3] + " session " + session);
    this.router.navigate(['/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + '/' + session + '/ver-informe']);
  }

  goToRepeatQuestionnaire(session: number) {
    console.debug("Redirecting to see answers of "+ this.router.url.split('/')[3] + " session " + session);
    this.router.navigate(['/pacientes/' + this.router.url.split('/')[2] + '/' + this.router.url.split('/')[3] + '/' + session + '/repetir']);
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
    this.pdfService.getPdf(this.router.url.split('/')[2], this.router.url.split('/')[3], session).subscribe(response => {
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
