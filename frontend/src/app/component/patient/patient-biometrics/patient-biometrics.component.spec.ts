import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientBiometricsComponent } from './patient-biometrics.component';

describe('PatientBiometricsComponent', () => {
  let component: PatientBiometricsComponent;
  let fixture: ComponentFixture<PatientBiometricsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PatientBiometricsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PatientBiometricsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
