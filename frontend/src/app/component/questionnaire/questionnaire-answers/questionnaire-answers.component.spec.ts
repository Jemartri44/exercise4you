import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionnaireAnswersComponent } from './questionnaire-answers.component';

describe('QuestionnaireAnswersComponent', () => {
  let component: QuestionnaireAnswersComponent;
  let fixture: ComponentFixture<QuestionnaireAnswersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestionnaireAnswersComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(QuestionnaireAnswersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
