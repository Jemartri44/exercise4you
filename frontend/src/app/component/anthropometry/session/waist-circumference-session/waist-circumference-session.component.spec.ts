import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WaistCircumferenceSessionComponent } from './waist-circumference-session.component';

describe('WaistCircumferenceSessionComponent', () => {
  let component: WaistCircumferenceSessionComponent;
  let fixture: ComponentFixture<WaistCircumferenceSessionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WaistCircumferenceSessionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(WaistCircumferenceSessionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
