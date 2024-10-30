import { Component, Input, OnInit } from '@angular/core';
import { AnthropometryAllData } from '../../../../model/anthropometry/anthropometry-all-data';
import { IdealWeight } from '../../../../model/anthropometry/ideal-weight';
import { AnthropometryService } from '../../../../services/anthropometry/anthropometry.service';
import { SkinFolds } from '../../../../model/anthropometry/skin-folds';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-skin-folds-all-sessions',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './skin-folds-all-sessions.component.html',
  styleUrl: './skin-folds-all-sessions.component.css'
})
export class SkinFoldsAllSessionsComponent implements OnInit{
  @Input() allData: AnthropometryAllData;
  sessions: string[] = [];
  data: SkinFolds[] = [];
  results: {density: number, fatMass: number, fatMassPercentage: number, fatFreeMass: number, fatLevel: string, leanMass: number}[] = [];

  constructor( private anthropometryService: AnthropometryService ) { }

  ngOnInit(): void {
    for (let i=0; i<this.allData.previous.length; i++) {
      this.sessions.push("Sesión " + this.allData.previous[i].session.number + " - " + this.allData.previous[i].session.date);
      this.data.push(this.allData.previous[i].anthropometry as SkinFolds);
      if(this.data[i].gender == undefined){
        throw new Error ("No se ha podido calcular la cantidad de masa grasa")
      }
      let foldsSum: number = 0;
      let anteriorThighFold = this.data[i].data.anteriorThighFold;
      let abdominalFold = this.data[i].data.abdominalFold;
      let pectoralFold = this.data[i].data.pectoralFold;
      let midaxillaryFold = this.data[i].data.midaxillaryFold;
      let tricipitalFold = this.data[i].data.tricipitalFold;
      let subscapularFold = this.data[i].data.subscapularFold;
      let suprailiacFold = this.data[i].data.suprailiacFold;
      let bicipitalFold = this.data[i].data.bicipitalFold;
      if(this.data[i].data.formula1 != "Fórmula de Jackson-Pollock de 3 pliegues" || this.data[i].data.formula1 != "Fórmula de Jackson-Pollock de 7 pliegues"){
        if(anteriorThighFold !== undefined) { foldsSum = foldsSum + anteriorThighFold }
      }
      if((this.data[i].data.formula1 != "Fórmula de Jackson-Pollock de 3 pliegues" && !this.data[i].gender) || this.data[i].data.formula1 != "Fórmula de Jackson-Pollock de 7 pliegues" || this.data[i].data.formula1 != "Fórmula de Durnin-Womersley" || this.data[i].data.formula1 != "Fórmula de Boileau et al."){
        if(tricipitalFold !== undefined) { foldsSum = foldsSum + tricipitalFold }
      }
      if((this.data[i].data.formula1 != "Fórmula de Jackson-Pollock de 3 pliegues" && !this.data[i].gender) || this.data[i].data.formula1 != "Fórmula de Jackson-Pollock de 7 pliegues" || this.data[i].data.formula1 != "Fórmula de Durnin-Womersley"){
        if(suprailiacFold !== undefined) { foldsSum = foldsSum + suprailiacFold }
      }
      if((this.data[i].data.formula1 != "Fórmula de Jackson-Pollock de 3 pliegues" && this.data[i].gender) || this.data[i].data.formula1 != "Fórmula de Jackson-Pollock de 7 pliegues"){
        if(abdominalFold !== undefined) { foldsSum = foldsSum + abdominalFold }
      }
      if((this.data[i].data.formula1 != "Fórmula de Jackson-Pollock de 3 pliegues" && this.data[i].gender) || this.data[i].data.formula1 != "Fórmula de Jackson-Pollock de 7 pliegues"){
        if(pectoralFold !== undefined) { foldsSum = foldsSum + pectoralFold }
      }
      if(this.data[i].data.formula1 != "Fórmula de Jackson-Pollock de 7 pliegues"){
        if(midaxillaryFold !== undefined) { foldsSum = foldsSum + midaxillaryFold }
      }
      if(this.data[i].data.formula1 != "Fórmula de Jackson-Pollock de 7 pliegues" || this.data[i].data.formula1 != "Fórmula de Durnin-Womersley" || this.data[i].data.formula1 != "Fórmula de Boileau et al."){
        if(subscapularFold !== undefined) { foldsSum = foldsSum + subscapularFold }
      }
      if(this.data[i].data.formula1 != "Fórmula de Durnin-Womersley"){
        if(bicipitalFold !== undefined) { foldsSum = foldsSum + bicipitalFold }
      }
      this.results.push(this.anthropometryService.calculateSkinFolds(this.data[i].data.formula1, this.data[i].data.formula2, foldsSum, this.data[i].data.weight, this.data[i].data.height, this.data[i].gender as boolean, this.data[i].age));
    }
  }

  assign(number: number | undefined): number {
    if(number === undefined) { return 0 }
    return number
  }
}
