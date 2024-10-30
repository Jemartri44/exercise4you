import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImcIntroductionComponent } from './imc-introduction.component';

describe('ImcIntroductionComponent', () => {
  let component: ImcIntroductionComponent;
  let fixture: ComponentFixture<ImcIntroductionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImcIntroductionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ImcIntroductionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
