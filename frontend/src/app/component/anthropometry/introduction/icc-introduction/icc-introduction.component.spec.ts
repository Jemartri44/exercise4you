import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IccIntroductionComponent } from './icc-introduction.component';

describe('IccIntroductionComponent', () => {
  let component: IccIntroductionComponent;
  let fixture: ComponentFixture<IccIntroductionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IccIntroductionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(IccIntroductionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
