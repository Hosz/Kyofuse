import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Location } from '@angular/common';
import { Title } from '@angular/platform-browser';
import { CookieConsentService } from '../../core/services/ui/cookie-consent.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { TranslatePipe } from '../../core/i18n/translate.pipe';

export type LegalDocType = 'termos' | 'privacidade' | 'cookies' | 'diretrizes';

export interface TabItem {
  id: LegalDocType;
  title: string;
  icon: string;
  badge?: string;
}

@Component({
  selector: 'app-legal-page',
  imports: [RouterLink, TranslatePipe],
  templateUrl: './legal-page.html',
  styleUrl: './legal-page.css',
})
export class LegalPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly titleService = inject(Title);
  readonly i18n = inject(I18nService);
  readonly cookieConsentService = inject(CookieConsentService);

  readonly activeDoc = signal<LegalDocType>('termos');

  readonly isPortuguese = computed(() => this.i18n.currentLang() === 'pt');

  readonly tabs = computed<TabItem[]>(() => [
    { id: 'termos', title: this.i18n.t('legal.readTerms'), icon: 'gavel' },
    { id: 'privacidade', title: this.i18n.t('legal.readPrivacy'), icon: 'shield' },
    { id: 'cookies', title: this.i18n.t('legal.readCookies'), icon: 'cookie' },
    { id: 'diretrizes', title: this.i18n.t('legal.readGuidelines'), icon: 'military_tech' },
  ]);

  ngOnInit(): void {
    const routeDoc = this.route.snapshot.data['document'] as LegalDocType | undefined;
    const urlSegment = this.route.snapshot.url[0]?.path as LegalDocType | undefined;

    if (routeDoc && this.isValidDoc(routeDoc)) {
      this.selectDoc(routeDoc, false);
    } else if (urlSegment && this.isValidDoc(urlSegment)) {
      this.selectDoc(urlSegment, false);
    } else {
      this.selectDoc('termos', false);
    }
  }

  selectDoc(doc: LegalDocType, updateUrl = true): void {
    this.activeDoc.set(doc);

    const titleMap: Record<LegalDocType, string> = {
      termos: this.i18n.t('legal.termsTitle'),
      privacidade: this.i18n.t('legal.privacyTitle'),
      cookies: this.i18n.t('legal.cookiesTitle'),
      diretrizes: this.i18n.t('legal.guidelinesTitle'),
    };

    this.titleService.setTitle(`Kyofuse | ${titleMap[doc]}`);

    if (updateUrl) {
      this.location.replaceState(`/${doc}`);
    }

    try {
      if (typeof window !== 'undefined' && typeof window.scrollTo === 'function') {
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    } catch {
      // ignore
    }
  }

  scrollToSection(id: string, event?: Event): void {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    if (typeof window === 'undefined' || typeof document === 'undefined') {
      return;
    }
    const element = document.getElementById(id);
    if (!element) {
      return;
    }
    const headerOffset = 130;
    const elementPosition = element.getBoundingClientRect().top;
    const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

    window.scrollTo({
      top: offsetPosition,
      behavior: 'smooth',
    });
  }

  goBack(): void {
    if (typeof window !== 'undefined' && window.history.length > 1) {
      this.location.back();
    } else {
      this.router.navigateByUrl('/home');
    }
  }

  openCookiePreferences(): void {
    this.cookieConsentService.openPreferences();
  }

  printDocument(): void {
    if (typeof window !== 'undefined') {
      window.print();
    }
  }

  private isValidDoc(doc: string): doc is LegalDocType {
    return ['termos', 'privacidade', 'cookies', 'diretrizes'].includes(doc);
  }
}
