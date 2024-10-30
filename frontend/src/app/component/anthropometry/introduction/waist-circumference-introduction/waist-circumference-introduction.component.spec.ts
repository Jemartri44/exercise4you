import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WaistCircumferenceIntroductionComponent } from './waist-circumference-introduction.component';

describe('WaistCircumferenceIntroductionComponent', () => {
  let component: WaistCircumferenceIntroductionComponent;
  let fixture: ComponentFixture<WaistCircumferenceIntroductionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WaistCircumferenceIntroductionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(WaistCircumferenceIntroductionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
