import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImcAllSessionsComponent } from './imc-all-sessions.component';

describe('ImcAllSessionsComponent', () => {
  let component: ImcAllSessionsComponent;
  let fixture: ComponentFixture<ImcAllSessionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImcAllSessionsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ImcAllSessionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
