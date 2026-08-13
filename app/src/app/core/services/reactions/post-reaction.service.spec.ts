import { TestBed } from '@angular/core/testing';

import { PostReactionService } from './post-reaction.service';

describe('PostReactionService', () => {
  let service: PostReactionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PostReactionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
