import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IccEndingComponent } from './icc-ending.component';

describe('IccEndingComponent', () => {
  let component: IccEndingComponent;
  let fixture: ComponentFixture<IccEndingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IccEndingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(IccEndingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
