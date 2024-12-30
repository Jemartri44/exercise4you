import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ObjectivesSessionButtonsComponent } from './objectives-session-buttons.component';

describe('ObjectivesSessionButtonsComponent', () => {
  let component: ObjectivesSessionButtonsComponent;
  let fixture: ComponentFixture<ObjectivesSessionButtonsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ObjectivesSessionButtonsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ObjectivesSessionButtonsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
