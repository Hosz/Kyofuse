import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ThemeMode, ThemeService } from '../../../../core/services/ui/theme.service';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { AppLanguage } from '../../../../core/i18n/i18n.types';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-preferences-settings',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './preferences-settings.html',
})
export class PreferencesSettingsSectionComponent {
  private readonly themeService = inject(ThemeService);
  private readonly i18n = inject(I18nService);

  readonly currentTheme = this.themeService.theme;
  readonly supportedLanguages = this.i18n.supportedLanguages;
  readonly currentLang = this.i18n.currentLang;

  setTheme(mode: ThemeMode): void {
    this.themeService.setTheme(mode);
  }

  setLanguage(code: AppLanguage): void {
    this.i18n.setLanguage(code, true);
  }
}
