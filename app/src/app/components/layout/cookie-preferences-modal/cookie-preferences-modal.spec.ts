import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { CookiePreferencesModalComponent } from './cookie-preferences-modal';
import { CookieConsentService } from '../../../core/services/ui/cookie-consent.service';
import { ToastService } from '../../../core/services/ui/toast.service';

describe('CookiePreferencesModalComponent', () => {
  let component: CookiePreferencesModalComponent;
  let fixture: ComponentFixture<CookiePreferencesModalComponent>;
  let cookieService: CookieConsentService;
  let toastService: ToastService;

  beforeEach(async () => {
    localStorage.clear();

    await TestBed.configureTestingModule({
      imports: [CookiePreferencesModalComponent],
      providers: [
        provideRouter([]),
        CookieConsentService,
        ToastService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CookiePreferencesModalComponent);
    component = fixture.componentInstance;
    cookieService = TestBed.inject(CookieConsentService);
    toastService = TestBed.inject(ToastService);
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve alternar functionalEnabled ao chamar toggleFunctional', () => {
    const initial = component.functionalEnabled();
    component.toggleFunctional();
    expect(component.functionalEnabled()).toBe(!initial);
  });

  it('deve alternar analyticsEnabled ao chamar toggleAnalytics', () => {
    const initial = component.analyticsEnabled();
    component.toggleAnalytics();
    expect(component.analyticsEnabled()).toBe(!initial);
  });

  it('deve salvar preferências customizadas e fechar a modal', () => {
    cookieService.openPreferences();
    expect(cookieService.modalVisible()).toBe(true);

    component.save();
    expect(cookieService.modalVisible()).toBe(false);
    expect(cookieService.hasConsent()).toBe(true);
  });

  it('deve aceitar todos os cookies ao chamar acceptAll', () => {
    cookieService.openPreferences();
    component.acceptAll();

    expect(cookieService.modalVisible()).toBe(false);
    expect(cookieService.preferences().functional).toBe(true);
    expect(cookieService.preferences().analytics).toBe(true);
  });

  it('deve fechar a modal ao pressionar Escape', () => {
    cookieService.openPreferences();
    expect(cookieService.modalVisible()).toBe(true);

    component.onEscape();
    expect(cookieService.modalVisible()).toBe(false);
  });
});
