import { Component, input } from '@angular/core';
import { ProfileHighlight } from '../../../shared/models/profile.model';

@Component({
  selector: 'app-profile-highlights',
  imports: [],
  templateUrl: './profile-highlights.html',
  styleUrl: './profile-highlights.css',
})
export class ProfileHighlightsComponent {
  highlights = input<ProfileHighlight[]>([]);
}
