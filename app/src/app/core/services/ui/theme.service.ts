import { Injectable, signal, effect, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export type ThemeMode = 'dark' | 'light' | 'system';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  private readonly THEME_KEY = 'kyofuse_theme';

  readonly theme = signal<ThemeMode>(this.loadInitialTheme());
  readonly isDark = signal<boolean>(true);

  private mediaQueryListener?: (e: MediaQueryListEvent) => void;

  constructor() {
    this.applyTheme(this.theme());
    this.setupSystemThemeListener();

    // Re-apply whenever theme signal changes
    effect(() => {
      const currentTheme = this.theme();
      this.applyTheme(currentTheme);
    });
  }

  public setTheme(mode: ThemeMode): void {
    this.theme.set(mode);
    this.applyTheme(mode);
    if (this.isBrowser) {
      try {
        localStorage.setItem(this.THEME_KEY, mode);
      } catch (e) {
        console.warn('Failed to save theme to localStorage:', e);
      }
    }
  }

  public toggleTheme(): void {
    const next = this.isDark() ? 'light' : 'dark';
    this.setTheme(next);
  }

  private loadInitialTheme(): ThemeMode {
    if (!this.isBrowser) return 'dark';
    try {
      const saved = localStorage.getItem(this.THEME_KEY) as ThemeMode | null;
      if (saved === 'dark' || saved === 'light' || saved === 'system') {
        return saved;
      }
    } catch {
      // Ignore localStorage errors
    }
    return 'dark';
  }

  private applyTheme(mode: ThemeMode): void {
    if (!this.isBrowser) return;

    let dark = true;
    if (mode === 'system') {
      dark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    } else {
      dark = mode === 'dark';
    }

    this.isDark.set(dark);
    const root = document.documentElement;
    if (dark) {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
  }

  private setupSystemThemeListener(): void {
    if (!this.isBrowser || !window.matchMedia) return;

    const query = window.matchMedia('(prefers-color-scheme: dark)');
    this.mediaQueryListener = (e: MediaQueryListEvent) => {
      if (this.theme() === 'system') {
        this.applyTheme('system');
      }
    };

    if (query.addEventListener) {
      query.addEventListener('change', this.mediaQueryListener);
    } else {
      query.addListener(this.mediaQueryListener);
    }
  }
}
