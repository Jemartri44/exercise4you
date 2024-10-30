import { Component, Input, OnInit } from '@angular/core';
import { AnthropometryAllData } from '../../../../model/anthropometry/anthropometry-all-data';
import { WaistCircumference } from '../../../../model/anthropometry/waist-circumference';
import { AnthropometryService } from '../../../../services/anthropometry/anthropometry.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-waist-circumference-all-sessions',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './waist-circumference-all-sessions.component.html',
  styleUrl: './waist-circumference-all-sessions.component.css'
})
export class WaistCircumferenceAllSessionsComponent implements OnInit{
  @Input() allData: AnthropometryAllData;
  sessions: string[] = [];
  data: WaistCircumference[] = [];
  results: {risk: string}[] = [];

  constructor( private anthropometryService: AnthropometryService ) { }

  ngOnInit(): void {
    for (let i=0; i<this.allData.previous.length; i++) {
      this.sessions.push("Sesión " + this.allData.previous[i].session.number + " - " + this.allData.previous[i].session.date);
      this.data.push(this.allData.previous[i].anthropometry as WaistCircumference);
      if(this.data[i].gender == undefined){
        throw new Error ("No se ha podido calcular el índice cintura-cadera")
      }
      this.results.push(this.anthropometryService.calculateWaistCircumference(this.data[i].data.waistCircumference, this.data[i].gender as boolean));
    }
  }
}
