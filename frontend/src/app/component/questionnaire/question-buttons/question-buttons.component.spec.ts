import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionButtonsComponent } from './question-buttons.component';

describe('DaysComponent', () => {
  let component: QuestionButtonsComponent;
  let fixture: ComponentFixture<QuestionButtonsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestionButtonsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(QuestionButtonsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
