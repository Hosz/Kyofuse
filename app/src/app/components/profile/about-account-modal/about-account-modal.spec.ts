import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AboutAccountModalComponent } from './about-account-modal';
import { gamerProfileResponse } from '../../../models/profile/gamer-profile.model';

describe('AboutAccountModalComponent', () => {
  let component: AboutAccountModalComponent;
  let fixture: ComponentFixture<AboutAccountModalComponent>;

  const mockProfile: gamerProfileResponse = {
    id: 'p-1',
    userId: 'u-1',
    username: 's1mple',
    nickname: 'Oleksandr',
    bio: 'CS Legend',
    avatarUrl: 'https://example.com/avatar.png',
    bannerUrl: 'https://example.com/banner.png',
    country: 'Ukraine',
    city: 'Kyiv',
    state: 'Kyiv',
    mainRole: 'AWPER',
    secondaryRole: 'RIFLER',
    premierRating: 25000,
    faceitLevel: 10,
    gcRank: 20,
    playstyle: 'COMPETITIVE',
    lookingForTeam: false,
    lookingForDuo: false,
    setupStatus: 'COMPLETE',
    favoriteMaps: [],
    createdAt: '2024-03-15T12:00:00Z',
    registrationCountry: 'Brasil',
    registrationCountryCode: 'BR',
    registrationDevice: 'Chrome no Windows',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AboutAccountModalComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AboutAccountModalComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('profile', mockProfile);
    fixture.componentRef.setInput('open', true);
    fixture.detectChanges();
  });

  it('displays country, device and registration info correctly', () => {
    expect(component.countryDisplay()).toBe('Brasil');
    expect(component.deviceDisplay()).toBe('Chrome no Windows');
    expect(component.flagUrl()).toContain('br.png');
  });

  it('falls back to default labels when registration info is missing', () => {
    const emptyProfile: gamerProfileResponse = {
      ...mockProfile,
      registrationCountry: undefined,
      registrationCountryCode: undefined,
      registrationDevice: undefined,
      country: '',
    };
    fixture.componentRef.setInput('profile', emptyProfile);
    fixture.detectChanges();

    expect(component.deviceDisplay()).toBe('Não identificado');
    expect(component.countryDisplay()).toBe('Não informado');
  });
});
