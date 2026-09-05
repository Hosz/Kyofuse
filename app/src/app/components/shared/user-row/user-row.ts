import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-user-row',
  imports: [RouterLink, TranslatePipe],
  templateUrl: './user-row.html',
  styleUrl: './user-row.css',
})
export class UserRowComponent {
  readonly fallbackAvatar = FALLBACK_AVATAR_URL;
  /** Ausente quando a linha vem de dados ainda não ligados à API real (ex.: sugestões) — a linha fica sem link. */
  userId = input<string | null>(null);
  name = input.required<string>();
  handle = input.required<string>();
  avatarUrl = input.required<string>();

  /** Quando omitido, a linha fica sem botão de ação (apenas informativa). */
  isFollowing = input<boolean | null>(null);

  actionClick = output<void>();
  userClick = output<void>();
}
