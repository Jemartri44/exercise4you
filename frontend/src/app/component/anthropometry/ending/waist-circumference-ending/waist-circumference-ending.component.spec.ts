import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WaistCircumferenceEndingComponent } from './waist-circumference-ending.component';

describe('WaistCircumferenceEndingComponent', () => {
  let component: WaistCircumferenceEndingComponent;
  let fixture: ComponentFixture<WaistCircumferenceEndingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WaistCircumferenceEndingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(WaistCircumferenceEndingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
