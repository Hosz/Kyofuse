import { Component, input, output } from '@angular/core';
import { PostCardComponent } from '../post-card/post-card';
import { Post } from '../../../shared/models/social.model';

@Component({
  selector: 'app-feed-list',
  imports: [PostCardComponent],
  templateUrl: './feed-list.html',
  styleUrl: './feed-list.css',
})
export class FeedListComponent {
  posts = input<Post[]>([]);

  authorBlocked = output<string>();
}