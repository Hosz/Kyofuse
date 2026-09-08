import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ThemeService } from '../../../core/services/ui/theme.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './theme-toggle.html',
})
export class ThemeToggleComponent {
  private readonly themeService = inject(ThemeService);

  readonly isDark = this.themeService.isDark;

  toggle(): void {
    this.themeService.toggleTheme();
  }
}
