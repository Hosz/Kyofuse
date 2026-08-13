import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Comment } from '../../../shared/models/social.model';
import { ReactionButtonComponent } from '../../shared/reaction-button/reaction-button';
import { UserOptionsMenuComponent } from '../../shared/user-options-menu/user-options-menu';

@Component({
  selector: 'app-comment-card',
  imports: [RouterLink, ReactionButtonComponent, UserOptionsMenuComponent],
  templateUrl: './comment-card.html',
  styleUrl: './comment-card.css',
})
export class CommentCardComponent {
  comment = input.required<Comment>();
  /** Verdadeiro quando o usuário chegou aqui a partir de "Respostas" no perfil, pra destacar esse comentário. */
  highlighted = input(false);

  /** Emitido com o id do autor quando ele é bloqueado. */
  authorBlocked = output<string>();
}
