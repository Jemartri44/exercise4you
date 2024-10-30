import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImcSessionComponent } from './imc-session.component';

describe('ImcSessionComponent', () => {
  let component: ImcSessionComponent;
  let fixture: ComponentFixture<ImcSessionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImcSessionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ImcSessionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
