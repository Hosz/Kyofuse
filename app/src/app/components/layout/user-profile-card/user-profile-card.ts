import { Component, computed, inject, signal, HostListener, ElementRef } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { gamerProfileCard, gamerProfileResponse } from '../../../models/profile/gamer-profile.model';
import { ProfileService } from '../../../core/services/profile/profile.service';
import { getCountryFlagUrl } from '../../../shared/models/location-options.model';
import { FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';
import { AccountManagerService } from '../../../core/services/auth/account-manager.service';
import { AuthService } from '../../../core/services/auth/auth.service';
import { ToastService } from '../../../core/services/ui/toast.service';

@Component({
  selector: 'app-user-profile-card',
  imports: [RouterLink],
  templateUrl: './user-profile-card.html',
  styleUrl: './user-profile-card.css',
})
export class UserProfileCardComponent {
  readonly fallbackAvatar = FALLBACK_AVATAR_URL;
  private readonly userService = inject(ProfileService);
  private readonly authService = inject(AuthService);
  readonly accountManager = inject(AccountManagerService);
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);
  private readonly elementRef = inject(ElementRef);

  user = signal<gamerProfileCard>({
    nickname: '',
    mainRole: '',
    avatarUrl: '',
    username: '',
    country: '',
    city: '',
    state: '',
  });

  currentUserId = signal<string | null>(null);
  isDropdownOpen = signal(false);
  switchingUserId = signal<string | null>(null);

  flagUrl = computed(() => {
    const showFlag = this.user().showCountryFlag ?? true;
    return showFlag ? getCountryFlagUrl(this.user().country) : null;
  });

  otherAccounts = computed(() => {
    const activeUserId = this.currentUserId();
    const activeUsername = this.user().username;
    return this.accountManager.savedAccounts().filter(
      (acc) =>
        (!activeUserId || acc.userId !== activeUserId) &&
        (!activeUsername || acc.username.toLowerCase() !== activeUsername.toLowerCase())
    );
  });

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (this.isDropdownOpen() && !this.elementRef.nativeElement.contains(event.target)) {
      this.isDropdownOpen.set(false);
    }
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.userService.myProfile().subscribe({
      next: (response: gamerProfileResponse) => {
        this.user.set({
          nickname: response.nickname,
          mainRole: response.mainRole,
          avatarUrl: response.avatarUrl,
          username: response.username,
          country: response.country,
          city: response.city,
          state: response.state,
          showCountryFlag: response.showCountryFlag ?? true,
        });
        this.currentUserId.set(response.userId);

        this.accountManager.registerOrUpdateAccount({
          userId: response.userId,
          username: response.username,
          nickname: response.nickname,
          avatarUrl: response.avatarUrl,
          country: response.country,
          showCountryFlag: response.showCountryFlag ?? true,
        });

        // Garante que o switchToken esteja sincronizado e válido no backend para a conta atual
        this.accountManager.requestCurrentSwitchToken().subscribe({
          next: (res) => {
            this.accountManager.updateSwitchToken(response.userId, res.switchToken);
          },
          error: (err) => {
            console.error('Failed to sync switch token:', err);
          },
        });
      },
      error: (error) => {
        console.error('Failed to fetch user profile:', error);
      },
    });
  }

  toggleDropdown(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.isDropdownOpen.update((open) => !open);
  }

  switchAccount(targetUserId: string, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }

    if (this.switchingUserId()) return;
    this.switchingUserId.set(targetUserId);

    this.authService.switchAccount(targetUserId).subscribe({
      next: () => {
        this.toastService.success('Conta alternada com sucesso!');
        this.isDropdownOpen.set(false);
        this.switchingUserId.set(null);
        window.location.reload();
      },
      error: (error) => {
        console.error('Failed to switch account:', error);
        this.switchingUserId.set(null);
        if (error?.status === 401) {
          this.toastService.error(
            'Sessão da conta expirada neste dispositivo. Adicione a conta novamente para reconectar.'
          );
        } else if (error?.status === 429) {
          this.toastService.error(
            'Muitas tentativas em sequência. Por favor, aguarde alguns instantes.'
          );
        } else {
          this.toastService.error(
            error?.error?.message ?? 'Falha ao alternar conta. Tente novamente.'
          );
        }
      },
    });
  }

  disconnectAccount(targetUserId: string, event: Event): void {
    event.stopPropagation();
    this.accountManager.disconnectAccount(targetUserId).subscribe({
      next: () => {
        this.toastService.success('Conta desconectada deste dispositivo.');
      },
      error: () => {
        this.accountManager.removeLocalAccount(targetUserId);
        this.toastService.success('Conta removida da lista local.');
      },
    });
  }

  navigateToAddAccount(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.isDropdownOpen.set(false);

    const userId = this.currentUserId();
    if (userId) {
      this.accountManager.requestCurrentSwitchToken().subscribe({
        next: (res) => {
          this.accountManager.updateSwitchToken(userId, res.switchToken);
          this.router.navigate(['/auth'], { queryParams: { addAccount: 'true' } });
        },
        error: () => {
          this.router.navigate(['/auth'], { queryParams: { addAccount: 'true' } });
        },
      });
    } else {
      this.router.navigate(['/auth'], { queryParams: { addAccount: 'true' } });
    }
  }
}
