import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.css'
})
export class NavigationComponent implements OnInit {

  leftOption: string = "";
  rightOption: string = "";

  constructor( private router:Router) { }

  ngOnInit(): void {
    this.setOptions(this.router.url.split('/')[3]);
  }

  setOptions(currentOption: string) {
    switch(currentOption) {
      case "IMC":
        this.leftOption = "";
        this.rightOption = "Índice cintura-cadera";
        break;
      case "ICC":
        this.leftOption = "Índice de masa corporal";
        this.rightOption = "Circunferencia de la cintura";
        break;
      case "circunferencia-cintura":
        this.leftOption = "Índice cintura-cadera";
        this.rightOption = "Peso ideal";
        break;
      case "peso-ideal":
        this.leftOption = "Circunferencia de la cintura";
        this.rightOption = "Medición de pliegues cutáneos";
        break;
      case "medici%C3%B3n-pliegues-cut%C3%A1neos":
        this.leftOption = "Peso ideal";
        this.rightOption = "";
        break;
      default:
        this.leftOption = "";
        this.rightOption = "";
        break;
    }
  }

  goTo (option:string) {
    switch(option) {
      case "Índice de masa corporal":
        console.debug("Redirecting to IMC");
        this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/IMC']);
        break;
      case "Índice cintura-cadera":
        console.debug("Redirecting to ICC");
        this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/ICC']);
        break;
      case "Circunferencia de la cintura":
        console.debug("Redirecting to waist circumference");
        this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/circunferencia-cintura']);
        break;
      case "Peso ideal":
        console.debug("Redirecting to ideal weight");
        this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/peso-ideal']);
        break;
      case "Medición de pliegues cutáneos":
        console.debug("Redirecting to skinfold measurement");
        this.router.navigate(['pacientes/' + this.router.url.split('/')[2] + '/medición-pliegues-cutáneos']);
        break;
      default:
        console.debug("Invalid option");
        break;
    }
  }
}
