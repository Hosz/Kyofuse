import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserSessionService } from '../../../../core/services/auth/user-session.service';
import { ToastService } from '../../../../core/services/ui/toast.service';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { DeviceType, UserSession } from '../../../../models/auth/user-session.model';

@Component({
  selector: 'app-sessions-settings-section',
  imports: [CommonModule, TranslatePipe],
  templateUrl: './sessions-settings.html',
  styleUrl: './sessions-settings.css',
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

  readonly currentSession = computed(() => this.sessions().find((s) => s.current) ?? null);
  readonly otherSessions = computed(() => this.sessions().filter((s) => !s.current));

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
        this.toastService.success('Outras sessões encerradas com sucesso.');
        this.loadSessions();
      },
      error: (err) => {
        console.error('Failed to revoke other sessions:', err);
        this.revokingAll.set(false);
        this.toastService.error('Erro ao encerrar outras sessões.');
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
        this.toastService.success('Sessão encerrada com sucesso.');
        this.loadSessions();
      },
      error: (err) => {
        console.error('Failed to revoke session:', err);
        this.revokingSessionId.set(null);
        this.toastService.error('Erro ao encerrar sessão.');
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
          this.toastService.success('Dispositivo removido dos confiáveis.');
          this.loadSessions();
          if (this.selectedSession()?.id === session.id) {
            this.selectedSession.update((s) => (s ? { ...s, trusted: false, trustedAt: undefined } : null));
          }
        },
        error: (err) => {
          console.error('Failed to untrust device:', err);
          this.togglingTrust.set(false);
          this.toastService.error('Erro ao alterar status de confiança.');
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
            this.toastService.error('Erro ao alterar status de confiança.');
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

  formatRelativeTime(isoDate?: string): string {
    if (!isoDate) return '';
    const now = Date.now();
    const time = new Date(isoDate).getTime();
    const diffSeconds = Math.max(0, Math.floor((now - time) / 1000));

    if (diffSeconds < 60) {
      return this.i18n.t('settings.activeNow');
    }
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
