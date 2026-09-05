import { Component, HostListener, inject, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { I18nService } from '../../../core/i18n/i18n.service';
import { AppLanguage } from '../../../core/i18n/i18n.types';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-language-selector',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './language-selector.html',
})
export class LanguageSelectorComponent {
  private readonly i18n = inject(I18nService);

  readonly compact = input<boolean>(false);
  readonly alignLeft = input<boolean>(false);

  readonly open = signal<boolean>(false);

  readonly supportedLanguages = this.i18n.supportedLanguages;
  readonly currentLang = this.i18n.currentLang;
  readonly currentLangInfo = this.i18n.currentLangInfo;

  toggle(): void {
    this.open.update((v) => !v);
  }

  selectLanguage(code: AppLanguage): void {
    this.i18n.setLanguage(code, true);
    this.open.set(false);
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    if (this.open()) {
      this.open.set(false);
    }
  }
}
