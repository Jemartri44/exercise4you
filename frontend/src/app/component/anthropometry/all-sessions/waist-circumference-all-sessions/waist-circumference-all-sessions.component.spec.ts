import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WaistCircumferenceAllSessionsComponent } from './waist-circumference-all-sessions.component';

describe('WaistCircumferenceAllSessionsComponent', () => {
  let component: WaistCircumferenceAllSessionsComponent;
  let fixture: ComponentFixture<WaistCircumferenceAllSessionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WaistCircumferenceAllSessionsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(WaistCircumferenceAllSessionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
