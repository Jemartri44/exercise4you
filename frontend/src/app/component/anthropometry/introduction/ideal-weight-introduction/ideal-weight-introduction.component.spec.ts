import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IdealWeightIntroductionComponent } from './ideal-weight-introduction.component';

describe('IdealWeightIntroductionComponent', () => {
  let component: IdealWeightIntroductionComponent;
  let fixture: ComponentFixture<IdealWeightIntroductionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IdealWeightIntroductionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(IdealWeightIntroductionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
