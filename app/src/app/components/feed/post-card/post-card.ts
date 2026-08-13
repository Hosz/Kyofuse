import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Post } from '../../../shared/models/social.model';
import { ReactionButtonComponent } from '../../shared/reaction-button/reaction-button';
import { UserOptionsMenuComponent } from '../../shared/user-options-menu/user-options-menu';
import { ShareButtonComponent } from '../../shared/share-button/share-button';

@Component({
  selector: 'app-post-card',
  imports: [RouterLink, ReactionButtonComponent, UserOptionsMenuComponent, ShareButtonComponent],
  templateUrl: './post-card.html',
  styleUrl: './post-card.css',
})
export class PostCardComponent {
  post = input.required<Post>();

  /** Emitido com o id do autor quando ele é bloqueado, pra quem estiver ouvindo remover os posts dele da lista. */
  authorBlocked = output<string>();
}