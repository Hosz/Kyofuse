import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AccountManagerService } from './account-manager.service';
import { API_URL } from '../../../models/api-url.model';

describe('AccountManagerService', () => {
  let service: AccountManagerService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(AccountManagerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created and generate a deviceId', () => {
    expect(service).toBeTruthy();
    const deviceId = service.getDeviceId();
    expect(deviceId).toBeTruthy();
    expect(localStorage.getItem('kyofuse_device_id')).toBe(deviceId);
  });

  it('should register and update saved accounts', () => {
    service.registerOrUpdateAccount({
      userId: 'user-1',
      username: 'gamer_one',
      nickname: 'Gamer One',
      switchToken: 'token-1',
    });

    expect(service.savedAccounts().length).toBe(1);
    expect(service.savedAccounts()[0].username).toBe('gamer_one');
    expect(service.savedAccounts()[0].switchToken).toBe('token-1');

    service.registerOrUpdateAccount({
      userId: 'user-1',
      username: 'gamer_one',
      nickname: 'Gamer Updated',
    });

    expect(service.savedAccounts().length).toBe(1);
    expect(service.savedAccounts()[0].nickname).toBe('Gamer Updated');
    expect(service.savedAccounts()[0].switchToken).toBe('token-1');
  });

  it('should switch account via API', () => {
    service.registerOrUpdateAccount({
      userId: 'user-2',
      username: 'gamer_two',
      switchToken: 'token-2',
    });

    service.switchAccount('user-2').subscribe((res) => {
      expect(res.userId).toBe('user-2');
      expect(res.switchToken).toBe('new-rotated-token');
    });

    const req = httpMock.expectOne(`${API_URL}/api/auth/switch-account`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      targetUserId: 'user-2',
      switchToken: 'token-2',
      deviceId: service.getDeviceId(),
    });

    req.flush({
      userId: 'user-2',
      email: 'gamer2@example.com',
      username: 'gamer_two',
      role: 'USER',
      switchToken: 'new-rotated-token',
    });

    expect(service.savedAccounts().find(a => a.userId === 'user-2')?.switchToken).toBe('new-rotated-token');
  });

  it('should disconnect account and remove from local storage', () => {
    service.registerOrUpdateAccount({
      userId: 'user-3',
      username: 'gamer_three',
      switchToken: 'token-3',
    });

    expect(service.savedAccounts().length).toBe(1);

    service.disconnectAccount('user-3').subscribe();

    const req = httpMock.expectOne(`${API_URL}/api/auth/disconnect-account`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      targetUserId: 'user-3',
      deviceId: service.getDeviceId(),
    });

    req.flush(null);

    expect(service.savedAccounts().length).toBe(0);
  });

  it('should deduplicate accounts by username when registering', () => {
    service.registerOrUpdateAccount({
      userId: 'profile-id-1',
      username: 'player',
      nickname: 'Player',
      switchToken: 'token-1',
    });

    service.registerOrUpdateAccount({
      userId: 'user-id-1',
      username: 'player',
      nickname: 'Player Updated',
    });

    expect(service.savedAccounts().length).toBe(1);
    expect(service.savedAccounts()[0].userId).toBe('user-id-1');
    expect(service.savedAccounts()[0].nickname).toBe('Player Updated');
    expect(service.savedAccounts()[0].switchToken).toBe('token-1');
  });
});
