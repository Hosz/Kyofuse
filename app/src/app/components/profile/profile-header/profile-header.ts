import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LiveStatsCardComponent } from '../live-stats-card/live-stats-card';
import { SocialLinksComponent } from '../../shared/social-links/social-links';
import { ProfileRankStats, ProfileViewMode } from '../../../shared/models/profile.model';
import { gamerProfileResponse } from '../../../models/profile/gamer-profile.model';
import { SocialLinks } from '../../../shared/models/social-links.model';
import { UserOptionsMenuComponent } from '../../shared/user-options-menu/user-options-menu';

@Component({
  selector: 'app-profile-header',
  imports: [RouterLink, LiveStatsCardComponent, SocialLinksComponent, UserOptionsMenuComponent],
  templateUrl: './profile-header.html',
  styleUrl: './profile-header.css',
})
export class ProfileHeaderComponent {
  profile = input.required<gamerProfileResponse>();

  bannerUrl = input.required<string>();
  verified = input.required<boolean>();
  stats = input.required<ProfileRankStats>();
  socialLinks = input<SocialLinks | undefined>(undefined);
  handle = input.required<string>();
  viewMode = input.required<ProfileViewMode>();

  followersCount = input(0);
  followingCount = input(0);
  friendsCount = input(0);
  postsCount = input(0);

  viewerIsFollowing = input(false);
  friendRequestSent = input(false);
  friendRequestPending = input(false);

  followersClick = output<void>();
  followingClick = output<void>();
  friendsClick = output<void>();
  toggleFollow = output<void>();
  toggleFriendRequest = output<void>();
  inviteClick = output<void>();
  messageClick = output<void>();
  blocked = output<void>();

  formatCount(value: number | null | undefined): string {
    const safeValue = value ?? 0;
    if (safeValue >= 1_000_000) return `${(safeValue / 1_000_000).toFixed(1)}M`;
    if (safeValue >= 1_000) return `${(safeValue / 1_000).toFixed(1)}K`;
    return `${safeValue}`;
  }
}
