import { Component, input, output, signal } from '@angular/core';
import { UserRowComponent } from '../../shared/user-row/user-row';
import { SuggestedProfile } from '../../../shared/models/social.model';

@Component({
  selector: 'app-suggested-follows',
  imports: [UserRowComponent],
  templateUrl: './suggested-follows.html',
  styleUrl: './suggested-follows.css',
})
export class SuggestedFollowsComponent {
  profiles = input<SuggestedProfile[]>([
    { name: 'Coach_X', handle: 'the_strat_king', avatarUrl: 'https://i.pravatar.cc/80?img=33' },
    { name: 'Aura_Gamer', handle: 'auraplays', avatarUrl: 'https://i.pravatar.cc/80?img=47' },
  ]);

  follow = output<SuggestedProfile>();

  private readonly followedHandles = signal(new Set<string>());

  isFollowing(handle: string): boolean {
    return this.followedHandles().has(handle);
  }

  onToggleFollow(profile: SuggestedProfile): void {
    this.followedHandles.update((handles) => {
      const next = new Set(handles);
      if (next.has(profile.handle)) {
        next.delete(profile.handle);
      } else {
        next.add(profile.handle);
      }
      return next;
    });
    this.follow.emit(profile);
  }
}