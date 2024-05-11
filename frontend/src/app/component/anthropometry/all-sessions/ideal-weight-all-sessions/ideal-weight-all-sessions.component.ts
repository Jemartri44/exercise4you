import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { AnthropometryAllData } from '../../../../model/anthropometry/anthropometry-all-data';
import { IdealWeight } from '../../../../model/anthropometry/ideal-weight';
import { AnthropometryService } from '../../../../services/anthropometry/anthropometry.service';

@Component({
  selector: 'app-ideal-weight-all-sessions',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './ideal-weight-all-sessions.component.html',
  styleUrl: './ideal-weight-all-sessions.component.css'
})
export class IdealWeightAllSessionsComponent implements OnInit{
  @Input() allData: AnthropometryAllData;
  sessions: string[] = [];
  data: IdealWeight[] = [];
  results: {idealWeight: number, difference: number}[] = [];

  constructor( private anthropometryService: AnthropometryService ) { }

  ngOnInit(): void {
    for (let i=0; i<this.allData.previous.length; i++) {
      this.sessions.push("Sesión " + this.allData.previous[i].session.number + " - " + this.allData.previous[i].session.date);
      this.data.push(this.allData.previous[i].anthropometry as IdealWeight);
      if(this.data[i].gender == undefined){
        throw new Error ("No se ha podido calcular el peso ideal")
      }
      this.results.push(this.anthropometryService.calculateIdealWeight(this.data[i].data.formula, this.data[i].data.weight, this.data[i].data.height, this.data[i].gender as boolean));
    }
  }
}
