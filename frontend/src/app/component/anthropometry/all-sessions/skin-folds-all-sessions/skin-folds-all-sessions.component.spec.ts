import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SkinFoldsAllSessionsComponent } from './skin-folds-all-sessions.component';

describe('SkinFoldsAllSessionsComponent', () => {
  let component: SkinFoldsAllSessionsComponent;
  let fixture: ComponentFixture<SkinFoldsAllSessionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SkinFoldsAllSessionsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SkinFoldsAllSessionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
