import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IccAllSessionsComponent } from './icc-all-sessions.component';

describe('IccAllSessionsComponent', () => {
  let component: IccAllSessionsComponent;
  let fixture: ComponentFixture<IccAllSessionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IccAllSessionsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(IccAllSessionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
