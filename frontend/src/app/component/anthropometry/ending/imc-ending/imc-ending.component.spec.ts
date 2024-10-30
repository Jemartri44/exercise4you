import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImcEndingComponent } from './imc-ending.component';

describe('ImcEndingComponent', () => {
  let component: ImcEndingComponent;
  let fixture: ComponentFixture<ImcEndingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImcEndingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ImcEndingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
