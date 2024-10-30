import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IdealWeightEndingComponent } from './ideal-weight-ending.component';

describe('IdealWeightEndingComponent', () => {
  let component: IdealWeightEndingComponent;
  let fixture: ComponentFixture<IdealWeightEndingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IdealWeightEndingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(IdealWeightEndingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
