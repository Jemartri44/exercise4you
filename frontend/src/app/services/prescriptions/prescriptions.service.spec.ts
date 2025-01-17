import { TestBed } from '@angular/core/testing';

import { PrescriptionsService } from './prescriptions.service';

describe('PrescriptionsService', () => {
  let service: PrescriptionsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PrescriptionsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
