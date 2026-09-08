import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { LegalPageComponent } from './legal-page';
import { CookieConsentService } from '../../core/services/ui/cookie-consent.service';

describe('LegalPageComponent', () => {
  let component: LegalPageComponent;
  let fixture: ComponentFixture<LegalPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LegalPageComponent],
      providers: [
        provideRouter([]),
        CookieConsentService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LegalPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve iniciar com termos como padrão se não houver dados de rota', () => {
    expect(component.activeDoc()).toBe('termos');
  });

  it('deve alternar a aba ativa ao selecionar um documento', () => {
    component.selectDoc('privacidade', false);
    expect(component.activeDoc()).toBe('privacidade');

    component.selectDoc('cookies', false);
    expect(component.activeDoc()).toBe('cookies');

    component.selectDoc('diretrizes', false);
    expect(component.activeDoc()).toBe('diretrizes');
  });
});
