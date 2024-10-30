import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IccSessionComponent } from './icc-session.component';

describe('IccSessionComponent', () => {
  let component: IccSessionComponent;
  let fixture: ComponentFixture<IccSessionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IccSessionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(IccSessionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
