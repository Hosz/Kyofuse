import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ModalComponent } from '../../shared/modal/modal';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { CommunityService } from '../../../core/services/communities/community.service';
import { CommunityResponse } from '../../../models/communities/community.model';

const CANDIDATES_PAGE_SIZE = 50;

@Component({
  selector: 'app-pin-community-modal',
  imports: [ModalComponent, TranslatePipe],
  templateUrl: './pin-community-modal.html',
  styleUrl: './pin-community-modal.css',
})
export class PinCommunityModalComponent {
  open = input(false);
  pinned = input<CommunityResponse[]>([]);

  closed = output<void>();
  /** Lista completa de fixadas após cada alteração, pra a home refletir sem recarregar. */
  pinnedChanged = output<CommunityResponse[]>();

  private communityService = inject(CommunityService);

  /** Candidatas são as comunidades do usuário: fixar uma comunidade de que ele não
   * participa mostraria uma aba onde só apareceriam os posts públicos dela. */
  private candidates = signal<CommunityResponse[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  pendingId = signal<string | null>(null);
  private loaded = false;

  draggedId = signal<string | null>(null);
  dragOverId = signal<string | null>(null);

  /**
   * Lista única: as fixadas primeiro, na ordem escolhida, e as demais logo abaixo. Fixar
   * empurra a comunidade pro fim do bloco fixado e as não fixadas descem uma posição —
   * o movimento sai de graça porque a ordem é derivada, não guardada em outra lista.
   */
  items = computed(() => {
    const pinnedIds = new Set(this.pinned().map((community) => community.id));
    const unpinned = this.candidates().filter((community) => !pinnedIds.has(community.id));
    return [...this.pinned(), ...unpinned];
  });

  pinnedCount = computed(() => this.pinned().length);

  constructor() {
    effect(() => {
      if (this.open() && !this.loaded) {
        this.loaded = true;
        this.loadCandidates();
      }
    });
  }

  isPinned(communityId: string): boolean {
    return this.pinned().some((community) => community.id === communityId);
  }

  toggle(community: CommunityResponse): void {
    if (this.pendingId()) return;
    this.pendingId.set(community.id);
    this.error.set(null);

    const alreadyPinned = this.isPinned(community.id);
    // Fixar devolve a comunidade e desafixar devolve void — o resultado é descartado
    // nos dois casos, então uniformiza pra um Observable<void> só.
    const request$: Observable<void> = alreadyPinned
      ? this.communityService.unpinCommunity(community.id)
      : this.communityService.pinCommunity(community.id).pipe(map(() => undefined));

    request$.subscribe({
      next: () => {
        this.pendingId.set(null);
        this.pinnedChanged.emit(
          alreadyPinned
            ? this.pinned().filter((item) => item.id !== community.id)
            : [...this.pinned(), community],
        );
      },
      error: (error) => {
        console.error('Failed to toggle pinned community:', error);
        this.pendingId.set(null);
        this.error.set(error?.error?.message ?? 'Não foi possível alterar essa comunidade.');
      },
    });
  }

  onDragStart(event: DragEvent, community: CommunityResponse): void {
    if (!this.isPinned(community.id)) return;

    // O Firefox só inicia o arraste se algum dado for escrito no dataTransfer.
    event.dataTransfer?.setData('text/plain', community.id);
    if (event.dataTransfer) event.dataTransfer.effectAllowed = 'move';

    this.draggedId.set(community.id);
  }

  /** Só aceita soltar sobre outra fixada: arrastar pra dentro do bloco não fixado não
   * teria significado — o que define fixar/desafixar é o botão. */
  onDragOver(event: DragEvent, community: CommunityResponse): void {
    if (!this.draggedId() || !this.isPinned(community.id)) return;

    event.preventDefault();
    if (event.dataTransfer) event.dataTransfer.dropEffect = 'move';
    this.dragOverId.set(community.id);
  }

  onDrop(event: DragEvent, target: CommunityResponse): void {
    event.preventDefault();
    const draggedId = this.draggedId();
    this.clearDrag();

    if (!draggedId || draggedId === target.id || !this.isPinned(target.id)) return;

    const order = this.pinned().map((community) => community.id);
    const from = order.indexOf(draggedId);
    const to = order.indexOf(target.id);
    if (from < 0 || to < 0) return;

    order.splice(to, 0, ...order.splice(from, 1));
    this.persistOrder(order);
  }

  clearDrag(): void {
    this.draggedId.set(null);
    this.dragOverId.set(null);
  }

  /** Alternativa por teclado e toque ao arraste, que depende de mouse. */
  move(community: CommunityResponse, direction: -1 | 1): void {
    const order = this.pinned().map((item) => item.id);
    const from = order.indexOf(community.id);
    const to = from + direction;
    if (from < 0 || to < 0 || to >= order.length) return;

    [order[from], order[to]] = [order[to], order[from]];
    this.persistOrder(order);
  }

  canMoveUp(community: CommunityResponse): boolean {
    return this.pinned().findIndex((item) => item.id === community.id) > 0;
  }

  canMoveDown(community: CommunityResponse): boolean {
    const index = this.pinned().findIndex((item) => item.id === community.id);
    return index >= 0 && index < this.pinned().length - 1;
  }

  onClose(): void {
    this.clearDrag();
    this.closed.emit();
  }

  private persistOrder(order: string[]): void {
    if (this.pendingId()) return;
    this.pendingId.set('reorder');
    this.error.set(null);

    this.communityService.reorderPinnedCommunities(order).subscribe({
      next: (reordered) => {
        this.pendingId.set(null);
        this.pinnedChanged.emit(reordered);
      },
      error: (error) => {
        console.error('Failed to reorder pinned communities:', error);
        this.pendingId.set(null);
        this.error.set(error?.error?.message ?? 'Não foi possível reordenar as comunidades.');
      },
    });
  }

  private loadCandidates(): void {
    this.loading.set(true);

    this.communityService.listMyCommunities(0, CANDIDATES_PAGE_SIZE).subscribe({
      next: (response) => {
        this.candidates.set(response.content);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch communities:', error);
        this.error.set('Não foi possível carregar suas comunidades.');
        this.loading.set(false);
      },
    });
  }
}
