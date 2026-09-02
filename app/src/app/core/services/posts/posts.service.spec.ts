import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PostsService } from './posts.service';
import { describe, expect, it, beforeEach, afterEach } from 'vitest';

describe('PostsService', () => {
  let service: PostsService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(PostsService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getProfileMediaPosts should call /api/feed/profile/:profileId/media', () => {
    service.getProfileMediaPosts('user-123', 0, 10).subscribe();
    const req = httpTesting.expectOne((r) => r.url.includes('/api/feed/profile/user-123/media') && r.params.get('page') === '0' && r.params.get('size') === '10');
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, last: true });
  });

  it('getMyMediaPosts should call /api/feed/posts/me/media', () => {
    service.getMyMediaPosts(1, 15).subscribe();
    const req = httpTesting.expectOne((r) => r.url.includes('/api/feed/posts/me/media') && r.params.get('page') === '1' && r.params.get('size') === '15');
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, last: true });
  });
});
