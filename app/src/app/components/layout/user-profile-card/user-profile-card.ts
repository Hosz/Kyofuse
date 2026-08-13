import { Component, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { UserProfile } from '../../../shared/models/social.model';
import { gamerProfileCard, gamerProfileResponse } from '../../../models/profile/gamer-profile.model';
import { ProfileService } from '../../../core/services/profile/profile.service';

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
    username: ''
  });

  ngOnInit() {
    this.userService.myProfile().subscribe({
    next: (response: gamerProfileResponse) => {
      this.user.set({
        nickname: response.nickname,
        mainRole: response.mainRole,
        avatarUrl: response.avatarUrl,
        username: response.username
      });
    },
    error: (error) => {
      console.error('Failed to fetch user profile:', error);
    }
  });
  }
}
