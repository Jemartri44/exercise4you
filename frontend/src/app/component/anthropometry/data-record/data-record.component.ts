import { CommonModule } from '@angular/common';
import { Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';

@Component({
  selector: 'app-data-record',
  standalone: true,
  imports: [ CommonModule ],
  templateUrl: './data-record.component.html',
  styleUrl: './data-record.component.css'
})
export class DataRecordComponent implements OnInit {

  @ViewChild('flexRadioDefault1') flexRadioDefault1: ElementRef;
  @ViewChild('select') select: ElementRef;
  @ViewChild('flexRadioDefault2') flexRadioDefault2: ElementRef;
  @Output() selectedOption = new EventEmitter<string>();
  @Input() options: string[];

  ngOnInit(): void {
    this.change();
  }

  change(){
    if(this.flexRadioDefault1 === undefined || this.select === undefined || this.flexRadioDefault2 === undefined){
      return;
    }
    if(this.flexRadioDefault1.nativeElement.checked){
      this.selectedOption.emit(this.select.nativeElement.value);
      return;
    }
    if(this.flexRadioDefault2.nativeElement.checked){
      this.selectedOption.emit("allSessions");
    }
  }

  selectChange(){
    this.flexRadioDefault1.nativeElement.checked = true;
    this.flexRadioDefault2.nativeElement.checked = false;
    this.change();
  }

  labelClick(){
    if(!this.flexRadioDefault2.nativeElement.checked){
      this.flexRadioDefault1.nativeElement.checked = false;
      this.flexRadioDefault2.nativeElement.checked = true;
      this.change();
    }
  }
}
