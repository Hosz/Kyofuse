import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserSessionService } from '../../../../core/services/auth/user-session.service';
import { ToastService } from '../../../../core/services/ui/toast.service';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { DeviceType, UserSession } from '../../../../models/auth/user-session.model';

import { SkeletonComponent } from '../../../../components/shared/skeleton/skeleton';

@Component({
  selector: 'app-sessions-settings-section',
  imports: [CommonModule, SkeletonComponent, TranslatePipe],
  templateUrl: './sessions-settings.html',
  styleUrl: './sessions-settings.css',
  host: {
    class: 'block w-full min-w-0',
  },
})
export class SessionsSettingsSectionComponent implements OnInit {
  private readonly userSessionService = inject(UserSessionService);
  private readonly toastService = inject(ToastService);
  private readonly i18n = inject(I18nService);

  loading = signal(true);
  sessions = signal<UserSession[]>([]);
  selectedSession = signal<UserSession | null>(null);
  showRevokeAllConfirm = signal(false);
  revokingSessionId = signal<string | null>(null);
  revokingAll = signal(false);
  togglingTrust = signal(false);

  readonly currentSession = computed(() => {
    const list = this.sessions();
    const current = list.find((s) => s.current);
    if (current) return current;
    return list.length > 0 ? list[0] : null;
  });
  readonly otherSessions = computed(() => {
    const current = this.currentSession();
    if (!current) return [];
    return this.sessions().filter((s) => s.id !== current.id);
  });

  ngOnInit(): void {
    this.loadSessions();
  }

  loadSessions(): void {
    this.userSessionService.listSessions().subscribe({
      next: (data) => {
        this.sessions.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to load sessions:', err);
        this.loading.set(false);
      },
    });
  }

  openSessionDetails(session: UserSession): void {
    this.selectedSession.set(session);
  }

  closeSessionDetails(): void {
    this.selectedSession.set(null);
  }

  confirmRevokeAll(): void {
    if (this.revokingAll()) return;
    this.revokingAll.set(true);

    this.userSessionService.revokeAllOtherSessions().subscribe({
      next: () => {
        this.revokingAll.set(false);
        this.showRevokeAllConfirm.set(false);
        this.toastService.success(this.i18n.t('settings.otherSessionsRevokedSuccess'));
        this.loadSessions();
      },
      error: (err) => {
        console.error('Failed to revoke other sessions:', err);
        this.revokingAll.set(false);
        this.toastService.error(this.i18n.t('settings.otherSessionsRevokeError'));
      },
    });
  }

  revokeSession(sessionId: string): void {
    if (this.revokingSessionId()) return;
    this.revokingSessionId.set(sessionId);

    this.userSessionService.revokeSession(sessionId).subscribe({
      next: () => {
        this.revokingSessionId.set(null);
        if (this.selectedSession()?.id === sessionId) {
          this.selectedSession.set(null);
        }
        this.toastService.success(this.i18n.t('settings.sessionRevokedSuccess'));
        this.loadSessions();
      },
      error: (err) => {
        console.error('Failed to revoke session:', err);
        this.revokingSessionId.set(null);
        this.toastService.error(this.i18n.t('settings.sessionRevokeError'));
      },
    });
  }

  toggleTrust(session: UserSession): void {
    if (this.togglingTrust()) return;
    this.togglingTrust.set(true);

    if (session.trusted) {
      this.userSessionService.untrustDevice(session.id).subscribe({
        next: () => {
          this.togglingTrust.set(false);
          this.toastService.success(this.i18n.t('settings.deviceUntrustedSuccess'));
          this.loadSessions();
          if (this.selectedSession()?.id === session.id) {
            this.selectedSession.update((s) => (s ? { ...s, trusted: false, trustedAt: undefined } : null));
          }
        },
        error: (err) => {
          console.error('Failed to untrust device:', err);
          this.togglingTrust.set(false);
          this.toastService.error(this.i18n.t('settings.trustStatusChangeError'));
        },
      });
    } else {
      if (session.current) {
        this.userSessionService.trustCurrentDevice().subscribe({
          next: () => {
            this.togglingTrust.set(false);
            this.toastService.success(this.i18n.t('auth.deviceTrustedSuccess'));
            this.loadSessions();
            if (this.selectedSession()?.id === session.id) {
              this.selectedSession.update((s) => (s ? { ...s, trusted: true, trustedAt: new Date().toISOString() } : null));
            }
          },
          error: (err) => {
            console.error('Failed to trust device:', err);
            this.togglingTrust.set(false);
            this.toastService.error(this.i18n.t('settings.trustStatusChangeError'));
          },
        });
      } else {
        this.togglingTrust.set(false);
      }
    }
  }

  getDeviceIcon(deviceType: DeviceType): string {
    switch (deviceType) {
      case 'WINDOWS':
        return 'desktop_windows';
      case 'MACOS':
        return 'laptop_mac';
      case 'LINUX':
        return 'terminal';
      case 'ANDROID':
        return 'phone_android';
      case 'IOS':
        return 'phone_iphone';
      default:
        return 'devices';
    }
  }

  formatDeviceName(session?: UserSession | null): string {
    if (!session) return '';

    let browser = (session.browser || '').trim();
    let os = (session.os || '').trim();

    // Fallback: If browser or os are missing, parse from deviceName if present
    if (!browser || !os) {
      const raw = (session.deviceName || '').trim();
      const match = raw.match(/^(.*?)\s+(?:no|on|en|auf|sur|на)\s+(.*)$/i);
      if (match) {
        if (!browser) browser = match[1].trim();
        if (!os) os = match[2].trim();
      } else if (!browser && !os) {
        if (raw.toLowerCase() === 'dispositivo desconhecido' || raw.toLowerCase() === 'unknown device') {
          return this.i18n.t('settings.unknownDevice');
        }
        return raw;
      }
    }

    const browserFormatted = this.formatBrowser(browser);
    const osFormatted = this.formatOs(os);

    if (browserFormatted && osFormatted) {
      const template = this.i18n.t('settings.deviceOnOs');
      return template.replace('{browser}', browserFormatted).replace('{os}', osFormatted);
    }

    return browserFormatted || osFormatted || session.deviceName || this.i18n.t('settings.unknownDevice');
  }

  formatBrowser(browser?: string): string {
    if (!browser) return '';
    const bLower = browser.trim().toLowerCase();
    if (bLower === 'navegador web' || bLower === 'web browser' || bLower === 'navegador') {
      return this.i18n.t('settings.unknownBrowser');
    }
    return browser;
  }

  formatOs(os?: string): string {
    if (!os) return '';
    const oLower = os.trim().toLowerCase();
    if (
      oLower === 'sistema operacional desconhecido' ||
      oLower === 'unknown os' ||
      oLower === 'unknown operating system'
    ) {
      return this.i18n.t('settings.unknownOs');
    }
    return os;
  }

  formatLocation(location?: string): string {
    if (!location) return this.i18n.t('settings.unknownLocation');
    const locLower = location.trim().toLowerCase();
    if (
      locLower === 'localização desconhecida' ||
      locLower === 'localizacao desconhecida' ||
      locLower === 'unknown location'
    ) {
      return this.i18n.t('settings.unknownLocation');
    }
    return location;
  }

  formatRelativeTime(isoDate?: string): string {
    if (!isoDate) return '';
    const now = Date.now();
    const time = new Date(isoDate).getTime();
    if (isNaN(time)) return '';
    const diffSeconds = Math.max(0, Math.floor((now - time) / 1000));

    if (diffSeconds < 60) {
      return this.i18n.t('settings.activeNow');
    }

    const currentLang = this.i18n.currentLang() || 'pt';

    try {
      const rtf = new Intl.RelativeTimeFormat(currentLang, { numeric: 'always', style: 'long' });
      const diffMinutes = Math.floor(diffSeconds / 60);
      let formatted = '';

      if (diffMinutes < 60) {
        formatted = rtf.format(-diffMinutes, 'minute');
      } else {
        const diffHours = Math.floor(diffMinutes / 60);
        if (diffHours < 24) {
          formatted = rtf.format(-diffHours, 'hour');
        } else {
          const diffDays = Math.floor(diffHours / 24);
          if (diffDays < 30) {
            formatted = rtf.format(-diffDays, 'day');
          } else {
            const diffMonths = Math.floor(diffDays / 30);
            if (diffMonths < 12) {
              formatted = rtf.format(-diffMonths, 'month');
            } else {
              const diffYears = Math.floor(diffDays / 365);
              formatted = rtf.format(-diffYears, 'year');
            }
          }
        }
      }

      return formatted ? formatted.charAt(0).toUpperCase() + formatted.slice(1) : '';
    } catch {
      const diffMinutes = Math.floor(diffSeconds / 60);
      if (diffMinutes < 60) {
        return `Há ${diffMinutes} min`;
      }
      const diffHours = Math.floor(diffMinutes / 60);
      if (diffHours < 24) {
        return `Há ${diffHours} ${diffHours === 1 ? 'hora' : 'horas'}`;
      }
      const diffDays = Math.floor(diffHours / 24);
      return `Há ${diffDays} ${diffDays === 1 ? 'dia' : 'dias'}`;
    }
  }

  formatExactDateTime(isoDate?: string): string {
    if (!isoDate) return '';
    try {
      const date = new Date(isoDate);
      return new Intl.DateTimeFormat(this.i18n.currentLang() || 'pt-BR', {
        dateStyle: 'medium',
        timeStyle: 'short',
      }).format(date);
    } catch {
      return isoDate;
    }
  }
}
