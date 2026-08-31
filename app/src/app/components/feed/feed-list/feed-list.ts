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
  emptyTitle = input<string>('Nenhuma publicação por aqui ainda');
  emptyMessage = input<string>('Compartilhe um clipe, tática ou pensamento com a comunidade!');
  emptyIcon = input<string>('dynamic_feed');

  authorBlocked = output<string>();
  postDeleted = output<string>();
}