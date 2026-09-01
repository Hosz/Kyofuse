import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { describe, expect, it, beforeEach, afterEach } from 'vitest';
import { FriendshipService } from './friendship.service';
import { API_URL } from '../../../models/api-url.model';

describe('FriendshipService', () => {
  let service: FriendshipService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FriendshipService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(FriendshipService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get friendship status', () => {
    const mockStatus = { isFriend: true, requestSent: false, requestReceived: false, requestId: null };
    service.getFriendshipStatus('user-123').subscribe((status) => {
      expect(status).toEqual(mockStatus);
    });

    const req = httpMock.expectOne(`${API_URL}/api/user-friendship/user-123/status`);
    expect(req.request.method).toBe('GET');
    req.flush(mockStatus);
  });
});
