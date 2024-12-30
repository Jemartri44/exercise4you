import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeeObjectivesComponent } from './see-objectives.component';

describe('SeeObjectivesComponent', () => {
  let component: SeeObjectivesComponent;
  let fixture: ComponentFixture<SeeObjectivesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SeeObjectivesComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SeeObjectivesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
