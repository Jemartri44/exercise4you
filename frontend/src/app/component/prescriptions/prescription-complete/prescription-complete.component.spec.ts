import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrescriptionCompleteComponent } from './prescription-complete.component';

describe('PrescriptionCompleteComponent', () => {
  let component: PrescriptionCompleteComponent;
  let fixture: ComponentFixture<PrescriptionCompleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrescriptionCompleteComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PrescriptionCompleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
