import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { AnthropometryData } from '../../../../model/anthropometry/anthropometry-data';
import { SkinFolds } from '../../../../model/anthropometry/skin-folds';

@Component({
  selector: 'app-skin-folds-introduction',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './skin-folds-introduction.component.html',
  styleUrl: './skin-folds-introduction.component.css'
})
export class SkinFoldsIntroductionComponent {
  @Input() data: AnthropometryData | null | undefined;
  kid: boolean = false;

  ngOnInit() {
    if (this.data) {
      let skinFolds = this.data as SkinFolds;
      if(skinFolds.age == undefined) {
        throw new Error ("No se pudo recuperar la información sobre el paciente");
      }
      if(skinFolds.age < 18) {
        this.kid = true;
      } else { 
        this.kid = false;
      }
    }
  }
}
