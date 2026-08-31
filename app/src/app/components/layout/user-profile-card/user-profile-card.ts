import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { gamerProfileCard, gamerProfileResponse } from '../../../models/profile/gamer-profile.model';
import { ProfileService } from '../../../core/services/profile/profile.service';
import { getCountryFlagUrl } from '../../../shared/models/location-options.model';

@Component({
  selector: 'app-user-profile-card',
  imports: [RouterLink],
  templateUrl: './user-profile-card.html',
  styleUrl: './user-profile-card.css',
})
export class UserProfileCardComponent {
  userService = inject(ProfileService);
  user = signal<gamerProfileCard>({
    nickname: '',
    mainRole: '',
    avatarUrl: '',
    username: '',
    country: '',
    city: '',
    state: '',
  });

  flagUrl = computed(() => getCountryFlagUrl(this.user().country));

  ngOnInit() {
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
        });
      },
      error: (error) => {
        console.error('Failed to fetch user profile:', error);
      },
    });
  }
}
