import { Component, ElementRef, computed, effect, inject, input, output, signal, viewChild } from '@angular/core';
import { PostReactionService } from '../../../core/services/reactions/post-reaction.service';
import { CommentReactionService } from '../../../core/services/reactions/comment-reaction.service';
import { PageResponse } from '../../../models/page-response.model';
import { I18nService } from '../../../core/i18n/i18n.service';

/** Quantos nomes aparecem antes do "e mais N pessoas". */
const NAMES_SHOWN = 2;

interface Reactor {
  nickname: string;
  username: string;
}

/**
 * Linha "Fulano, Beltrano e mais 12 pessoas curtiram", no espírito do Instagram.
 *
 * O backend não devolve quem reagiu junto com o post, só os contadores — então os nomes
 * precisam de uma requisição própria. Para não disparar uma por card assim que o feed
 * abre, a busca só acontece quando o card entra na tela.
 */
@Component({
  selector: 'app-reaction-summary',
  imports: [],
  templateUrl: './reaction-summary.html',
  styleUrl: './reaction-summary.css',
})
export class ReactionSummaryComponent {
  postId = input.required<string>();
  /** Ausente num post; preenchido quando o resumo é de um comentário. */
  commentId = input<string | null>(null);
  likeCount = input.required<number>();
  reactionCount = input.required<number>();

  /** Abrir a lista completa é responsabilidade de quem usa o resumo. */
  openList = output<void>();

  private postReactionService = inject(PostReactionService);
  private commentReactionService = inject(CommentReactionService);
  private host = viewChild<ElementRef<HTMLElement>>('host');
  private i18n = inject(I18nService);

  private reactors = signal<Reactor[]>([]);
  private fetched = false;

  total = computed(() => this.likeCount() + this.reactionCount());

  /** Os nomes podem não ter chegado ainda; nesse caso mostra só a contagem. */
  names = computed(() => this.reactors().slice(0, NAMES_SHOWN).map((reactor) => reactor.nickname || reactor.username));

  othersCount = computed(() => Math.max(this.total() - this.names().length, 0));

  label = computed(() => {
    this.i18n.currentLang();
    return buildReactionLabel(this.total(), this.names(), this.i18n);
  });

  constructor() {
    effect((onCleanup) => {
      const element = this.host()?.nativeElement;
      if (!element || this.total() === 0 || this.fetched) return;

      const observer = new IntersectionObserver((entries) => {
        if (!entries.some((entry) => entry.isIntersecting)) return;
        observer.disconnect();
        this.fetchNames();
      });

      observer.observe(element);
      onCleanup(() => observer.disconnect());
    });
  }

  private fetchNames(): void {
    if (this.fetched) return;
    this.fetched = true;

    const commentId = this.commentId();
    // getReactions devolve curtidas e demais reações juntas, então basta uma chamada.
    const request$ = commentId
      ? this.commentReactionService.getReactions(this.postId(), commentId, 0, NAMES_SHOWN)
      : this.postReactionService.getReactions(this.postId(), 0, NAMES_SHOWN);

    request$.subscribe({
      next: (response: PageResponse<{ username: string; nickname: string }>) =>
        this.reactors.set(response.content.map((entry) => ({ nickname: entry.nickname, username: entry.username }))),
      // Sem nomes o resumo ainda funciona mostrando só a contagem — não vale um aviso.
      error: (error) => console.error('Failed to fetch reaction names:', error),
    });
  }
}

/**
 * "Ana e Bia reagiram", "Ana, Bia e mais 13 pessoas reagiram", ou só a contagem quando
 * os nomes ainda não chegaram.
 */
export function buildReactionLabel(
  total: number,
  names: string[],
  i18n?: { t: (key: string, params?: Record<string, string | number>) => string } | null,
): string {
  const mentioned = Math.max(total, names.length);
  const others = Math.max(total - names.length, 0);

  if (!i18n) {
    const verb = mentioned === 1 ? 'reagiu' : 'reagiram';
    if (names.length === 0) {
      return `${total} ${total === 1 ? 'pessoa' : 'pessoas'} ${verb}`;
    }
    if (others === 0) {
      return `${joinNames(names, 'e')} ${verb}`;
    }
    return `${names.join(', ')} e mais ${others} ${others === 1 ? 'pessoa' : 'pessoas'} ${verb}`;
  }

  const andWord = i18n.t('common.and');

  if (names.length === 0) {
    if (total === 1) {
      return i18n.t('reactions.peopleReactedSingle');
    }
    return i18n.t('reactions.peopleReactedPlural', { count: total });
  }

  if (others === 0) {
    if (names.length === 1) {
      return i18n.t('reactions.reactedSingle', { name: names[0] });
    }
    return i18n.t('reactions.reactedPlural', { names: joinNames(names, andWord) });
  }

  const joinedNames = names.join(', ');
  if (others === 1) {
    return i18n.t('reactions.reactedAndMoreSingle', { names: joinedNames });
  }
  return i18n.t('reactions.reactedAndMorePlural', { names: joinedNames, count: others });
}

function joinNames(names: string[], andWord = 'e'): string {
  return names.length === 1 ? names[0] : `${names.slice(0, -1).join(', ')} ${andWord} ${names.at(-1)}`;
}
