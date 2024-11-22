import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SkinFoldsSessionComponent } from './skin-folds-session.component';

describe('SkinFoldsSessionComponent', () => {
  let component: SkinFoldsSessionComponent;
  let fixture: ComponentFixture<SkinFoldsSessionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SkinFoldsSessionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SkinFoldsSessionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
