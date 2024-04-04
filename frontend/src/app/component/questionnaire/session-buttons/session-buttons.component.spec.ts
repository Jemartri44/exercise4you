import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SessionButtonsComponent } from './session-buttons.component';

describe('SessionButtonsComponent', () => {
  let component: SessionButtonsComponent;
  let fixture: ComponentFixture<SessionButtonsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SessionButtonsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SessionButtonsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
