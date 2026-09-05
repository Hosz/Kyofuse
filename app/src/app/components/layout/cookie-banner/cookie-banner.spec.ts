import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { CookieBannerComponent } from './cookie-banner';
import { CookieConsentService } from '../../../core/services/ui/cookie-consent.service';

describe('CookieBannerComponent', () => {
  let component: CookieBannerComponent;
  let fixture: ComponentFixture<CookieBannerComponent>;
  let cookieService: CookieConsentService;

  beforeEach(async () => {
    localStorage.clear();

    await TestBed.configureTestingModule({
      imports: [CookieBannerComponent],
      providers: [
        provideRouter([]),
        CookieConsentService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CookieBannerComponent);
    component = fixture.componentInstance;
    cookieService = TestBed.inject(CookieConsentService);
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve aceitar todos os cookies ao chamar acceptAll', () => {
    component.acceptAll();
    expect(cookieService.hasConsent()).toBe(true);
    expect(cookieService.preferences().functional).toBe(true);
    expect(cookieService.preferences().analytics).toBe(true);
  });

  it('deve aceitar apenas essenciais ao chamar acceptEssential', () => {
    component.acceptEssential();
    expect(cookieService.hasConsent()).toBe(true);
    expect(cookieService.preferences().functional).toBe(false);
    expect(cookieService.preferences().analytics).toBe(false);
  });

  it('deve abrir preferências ao chamar openPreferences', () => {
    component.openPreferences();
    expect(cookieService.modalVisible()).toBe(true);
  });
});
