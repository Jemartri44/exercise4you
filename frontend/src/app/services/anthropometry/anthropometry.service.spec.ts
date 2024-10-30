import { TestBed } from '@angular/core/testing';

import { AnthropometryService } from './anthropometry.service';

describe('AnthropometryService', () => {
  let service: AnthropometryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AnthropometryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
