import { Component, Input, OnInit } from '@angular/core';
import { AnthropometryAllData } from '../../../../model/anthropometry/anthropometry-all-data';
import { Icc } from '../../../../model/anthropometry/icc';
import { AnthropometryService } from '../../../../services/anthropometry/anthropometry.service';
import { CommonModule, NgFor } from '@angular/common';

@Component({
  selector: 'app-icc-all-sessions',
  standalone: true,
  imports: [ CommonModule, NgFor ],
  templateUrl: './icc-all-sessions.component.html',
  styleUrl: './icc-all-sessions.component.css'
})
export class IccAllSessionsComponent implements OnInit{
  @Input() allData: AnthropometryAllData;
  sessions: string[] = [];
  data: Icc[] = [];
  results: {icc: number, category: string, risk: string}[] = [];

  constructor( private anthropometryService: AnthropometryService ) { }

  ngOnInit(): void {
    for (let i=0; i<this.allData.previous.length; i++) {
      this.sessions.push("Sesión " + this.allData.previous[i].session.number + " - " + this.allData.previous[i].session.date);
      this.data.push(this.allData.previous[i].anthropometry as Icc);
      if(this.data[i].gender == undefined){
        throw new Error ("No se ha podido calcular el índice cintura-cadera")
      }
      this.results.push(this.anthropometryService.calculateIcc(this.data[i].data.waistCircumference, this.data[i].data.hipCircumference, this.data[i].gender as boolean));
    }
  }
}
