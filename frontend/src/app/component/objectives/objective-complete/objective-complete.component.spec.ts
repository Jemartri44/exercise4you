import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ObjectiveCompleteComponent } from './objective-complete.component';

describe('ObjectiveCompleteComponent', () => {
  let component: ObjectiveCompleteComponent;
  let fixture: ComponentFixture<ObjectiveCompleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ObjectiveCompleteComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ObjectiveCompleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
