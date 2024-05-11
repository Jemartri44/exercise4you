import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataRecordComponent } from './data-record.component';

describe('DataRecordComponent', () => {
  let component: DataRecordComponent;
  let fixture: ComponentFixture<DataRecordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DataRecordComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DataRecordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
