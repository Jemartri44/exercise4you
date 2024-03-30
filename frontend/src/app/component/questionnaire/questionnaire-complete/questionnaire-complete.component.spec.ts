import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionnaireCompleteComponent } from './questionnaire-complete.component';

describe('QuestionnaireCompleteComponent', () => {
  let component: QuestionnaireCompleteComponent;
  let fixture: ComponentFixture<QuestionnaireCompleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestionnaireCompleteComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(QuestionnaireCompleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
