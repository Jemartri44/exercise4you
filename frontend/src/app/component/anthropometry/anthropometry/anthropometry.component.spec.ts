import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnthropometryComponent } from './anthropometry.component';

describe('AnthropometryComponent', () => {
  let component: AnthropometryComponent;
  let fixture: ComponentFixture<AnthropometryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnthropometryComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AnthropometryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
