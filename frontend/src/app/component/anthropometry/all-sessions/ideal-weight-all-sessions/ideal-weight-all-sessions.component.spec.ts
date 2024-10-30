import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IdealWeightAllSessionsComponent } from './ideal-weight-all-sessions.component';

describe('IdealWeightAllSessionsComponent', () => {
  let component: IdealWeightAllSessionsComponent;
  let fixture: ComponentFixture<IdealWeightAllSessionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IdealWeightAllSessionsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(IdealWeightAllSessionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
