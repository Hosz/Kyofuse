import { Component, computed, inject, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LiveStatsCardComponent } from '../live-stats-card/live-stats-card';
import { SocialLinksComponent } from '../../shared/social-links/social-links';
import { ProfileRankStats, ProfileViewMode } from '../../../shared/models/profile.model';
import { gamerProfileResponse } from '../../../models/profile/gamer-profile.model';
import { SocialLinks } from '../../../shared/models/social-links.model';
import { UserOptionsMenuComponent } from '../../shared/user-options-menu/user-options-menu';
import { RoleIconComponent } from '../../shared/role-icon/role-icon';
import { formatLocation, getCountryFlagUrl } from '../../../shared/models/location-options.model';
import { getPlayerRoleLabel } from '../../../shared/models/profile-options.model';
import { formatJoinedDate, FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';
import { ToastService } from '../../../core/services/ui/toast.service';

import { I18nService } from '../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-profile-header',
  imports: [RouterLink, LiveStatsCardComponent, SocialLinksComponent, UserOptionsMenuComponent, RoleIconComponent, TranslatePipe],
  templateUrl: './profile-header.html',
  styleUrl: './profile-header.css',
})
export class ProfileHeaderComponent {
  readonly fallbackAvatar = FALLBACK_AVATAR_URL;
  private toastService = inject(ToastService);
  private i18n = inject(I18nService);
  profile = input.required<gamerProfileResponse>();

  bannerUrl = input.required<string>();
  verified = input.required<boolean>();
  stats = input.required<ProfileRankStats>();
  socialLinks = input<SocialLinks | undefined>(undefined);
  handle = input.required<string>();
  viewMode = input.required<ProfileViewMode>();

  flagUrl = computed(() => {
    const showFlag = this.profile().showCountryFlag ?? true;
    return showFlag ? getCountryFlagUrl(this.profile().country) : null;
  });
  formattedLocation = computed(() =>
    formatLocation(this.profile().country, this.profile().state, this.profile().city),
  );
  joinedDate = computed(() => {
    const lang = this.i18n.currentLang();
    const template = this.i18n.t('profile.joined');
    return formatJoinedDate(this.profile().createdAt, lang, template);
  });
  mainRoleLabel = computed(() => {
    this.i18n.currentLang();
    return getPlayerRoleLabel(this.profile().mainRole, this.i18n);
  });
  secondaryRoleLabel = computed(() => {
    this.i18n.currentLang();
    return getPlayerRoleLabel(this.profile().secondaryRole, this.i18n);
  });

  inviteCommunity(): void {
    this.toastService.info('O convite de jogadores para comunidades estará disponível em breve!');
  }

  followersCount = input(0);
  followingCount = input(0);
  friendsCount = input(0);
  postsCount = input(0);

  viewerIsFollowing = input(false);
  viewerIsFriend = input(false);
  friendRequestSent = input(false);
  friendRequestReceived = input(false);
  friendRequestPending = input(false);

  followersClick = output<void>();
  followingClick = output<void>();
  friendsClick = output<void>();
  toggleFollow = output<void>();
  toggleFriendRequest = output<void>();
  friendRemoved = output<void>();
  inviteClick = output<void>();
  inviteCommunityClick = output<void>();
  messageClick = output<void>();
  blocked = output<void>();

  formatCount(value: number | null | undefined): string {
    const safeValue = value ?? 0;
    if (safeValue >= 1_000_000) return `${(safeValue / 1_000_000).toFixed(1)}M`;
    if (safeValue >= 1_000) return `${(safeValue / 1_000).toFixed(1)}K`;
    return `${safeValue}`;
  }
}
