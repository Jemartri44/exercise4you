import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SkinFoldsIntroductionComponent } from './skin-folds-introduction.component';

describe('SkinFoldsIntroductionComponent', () => {
  let component: SkinFoldsIntroductionComponent;
  let fixture: ComponentFixture<SkinFoldsIntroductionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SkinFoldsIntroductionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SkinFoldsIntroductionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
