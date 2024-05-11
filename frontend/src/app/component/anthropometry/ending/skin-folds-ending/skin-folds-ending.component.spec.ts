import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SkinFoldsEndingComponent } from './skin-folds-ending.component';

describe('SkinFoldsEndingComponent', () => {
  let component: SkinFoldsEndingComponent;
  let fixture: ComponentFixture<SkinFoldsEndingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SkinFoldsEndingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SkinFoldsEndingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
