import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrescriptionsSessionButtonsComponent } from './prescriptions-session-buttons.component';

describe('PrescriptionsSessionButtonsComponent', () => {
  let component: PrescriptionsSessionButtonsComponent;
  let fixture: ComponentFixture<PrescriptionsSessionButtonsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrescriptionsSessionButtonsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PrescriptionsSessionButtonsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
