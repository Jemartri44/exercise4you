import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IpaqComponent } from './questionnaire-list.component';

describe('IpaqComponent', () => {
  let component: IpaqComponent;
  let fixture: ComponentFixture<IpaqComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IpaqComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(IpaqComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
