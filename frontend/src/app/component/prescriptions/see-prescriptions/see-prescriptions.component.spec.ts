import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeePrescriptionsComponent } from './see-prescriptions.component';

describe('SeePrescriptionsComponent', () => {
  let component: SeePrescriptionsComponent;
  let fixture: ComponentFixture<SeePrescriptionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeePrescriptionsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SeePrescriptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
