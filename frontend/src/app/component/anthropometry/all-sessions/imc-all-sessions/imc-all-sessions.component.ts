import { Component, Input, OnInit } from '@angular/core';
import { AnthropometryAllData } from '../../../../model/anthropometry/anthropometry-all-data';
import { Imc } from '../../../../model/anthropometry/imc';
import { AnthropometryService } from '../../../../services/anthropometry/anthropometry.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-imc-all-sessions',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './imc-all-sessions.component.html',
  styleUrl: './imc-all-sessions.component.css'
})
export class ImcAllSessionsComponent implements OnInit{
  @Input() allData: AnthropometryAllData;
  sessions: string[] = [];
  data: Imc[] = [];
  results: {imc: number, imcp: number, category: string, risk: string}[] = [];

  constructor( private anthropometryService: AnthropometryService ) { }

  ngOnInit(): void {
    for (let i=0; i<this.allData.previous.length; i++) {
      this.sessions.push("Sesión " + this.allData.previous[i].session.number + " - " + this.allData.previous[i].session.date);
      this.data.push(this.allData.previous[i].anthropometry as Imc);
      this.results.push(this.anthropometryService.calculateImc(this.data[i].data.weight, this.data[i].data.height));
    }
  }
}
