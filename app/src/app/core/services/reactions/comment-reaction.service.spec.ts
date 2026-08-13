import { TestBed } from '@angular/core/testing';

import { CommentReactionService } from './comment-reaction.service';

describe('CommentReactionService', () => {
  let service: CommentReactionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CommentReactionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
