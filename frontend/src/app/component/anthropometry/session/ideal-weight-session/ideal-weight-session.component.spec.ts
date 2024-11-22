import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IdealWeightSessionComponent } from './ideal-weight-session.component';

describe('IdealWeightSessionComponent', () => {
  let component: IdealWeightSessionComponent;
  let fixture: ComponentFixture<IdealWeightSessionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IdealWeightSessionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(IdealWeightSessionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
