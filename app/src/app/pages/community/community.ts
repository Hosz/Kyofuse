import { Component, computed, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { ModalComponent } from '../../components/shared/modal/modal';
import { CommunityService } from '../../core/services/communities/community.service';
import { CommunityMemberService } from '../../core/services/communities/community-member.service';
import { CommunityJoinRequestService } from '../../core/services/communities/community-join-request.service';
import { ConversationService } from '../../core/services/chat/conversation.service';
import { PostsService } from '../../core/services/posts/posts.service';
import { ProfileService } from '../../core/services/profile/profile.service';
import { Post } from '../../shared/models/social.model';
import { toPost } from '../../shared/utils/mappers.util';
import {
  CommunityJoinRequestResponse,
  CommunityMemberResponse,
  CommunityMemberRole,
  CommunityResponse,
  CommunityVisibility,
} from '../../models/communities/community.model';
import { FALLBACK_AVATAR_URL } from '../../shared/utils/format.util';
import { ConfirmDialogComponent } from '../../components/shared/confirm-dialog/confirm-dialog';
import { I18nService } from '../../core/i18n/i18n.service';
import { TeamResponse } from '../../models/teams/team.model';

import { SkeletonComponent } from '../../components/shared/skeleton/skeleton';

import { MediaService } from '../../core/services/media/media.service';
import { ToastService } from '../../core/services/ui/toast.service';

import { PostMediaItemRequest } from '../../models/posts/post-request.model';
import { ImageModalComponent } from '../../components/shared/image-modal/image-modal';
import { TranslatePipe } from '../../core/i18n/translate.pipe';

const ROLE_LABEL: Record<CommunityMemberRole, string> = {
  ADMIN: 'Admin',
  MODERATOR: 'Moderador',
  MEMBER: 'Membro',
};

import { MentionInputComponent } from '../../shared/components/mention-input/mention-input';
import { CharLimitIndicatorComponent } from '../../shared/components/char-limit-indicator/char-limit-indicator';

type ConfirmAction = 'archive' | 'delete' | null;

@Component({
  selector: 'app-community',
  imports: [
    RouterLink,
    AppSidebarComponent,
    ModalComponent,
    ConfirmDialogComponent,
    SkeletonComponent,
    ImageModalComponent,
    TranslatePipe,
    MentionInputComponent,
    CharLimitIndicatorComponent,
  ],
  templateUrl: './community.html',
  styleUrl: './community.css',
})
export class CommunityComponent {
  /** Vinculado automaticamente ao parâmetro de rota :communityId (withComponentInputBinding). */
  communityId = input<string | null>(null);

  private router = inject(Router);
  private communityService = inject(CommunityService);
  private communityMemberService = inject(CommunityMemberService);
  private communityJoinRequestService = inject(CommunityJoinRequestService);
  private conversationService = inject(ConversationService);
  private postsService = inject(PostsService);
  private profileService = inject(ProfileService);
  private mediaService = inject(MediaService);
  private toastService = inject(ToastService);
  readonly i18n = inject(I18nService);

  readonly fallbackAvatar = FALLBACK_AVATAR_URL;

  loading = signal(true);
  notFound = signal(false);
  community = signal<CommunityResponse | null>(null);
  myUserId = signal<string | null>(null);

  uploadingAvatar = signal(false);
  uploadingBanner = signal(false);

  onAvatarFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.uploadingAvatar.set(true);
    this.mediaService.uploadImage(file).subscribe({
      next: (res) => {
        this.editAvatarUrl.set(res.url);
        this.uploadingAvatar.set(false);
        this.toastService.info('Foto carregada. Clique em Salvar para aplicar.');
      },
      error: (err) => {
        console.error('Failed to upload community avatar:', err);
        this.toastService.error('Erro ao enviar avatar da comunidade.');
        this.uploadingAvatar.set(false);
      },
    });
  }

  removeAvatar(): void {
    this.editAvatarUrl.set('');
  }

  onBannerFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.uploadingBanner.set(true);
    this.mediaService.uploadImage(file).subscribe({
      next: (res) => {
        this.editBannerUrl.set(res.url);
        this.uploadingBanner.set(false);
        this.toastService.info('Banner carregado. Clique em Salvar para aplicar.');
      },
      error: (err) => {
        console.error('Failed to upload community banner:', err);
        this.toastService.error('Erro ao enviar banner da comunidade.');
        this.uploadingBanner.set(false);
      },
    });
  }

  removeBanner(): void {
    this.editBannerUrl.set('');
  }

  isOwner = computed(() => {
    const community = this.community();
    const myUserId = this.myUserId();
    return !!community && !!myUserId && community.ownerId === myUserId;
  });

  isCommunityAdmin = computed(() => {
    const uid = this.myUserId();
    if (!uid) return false;
    return this.members().some((m) => m.memberId === uid && m.role === 'ADMIN');
  });

  isHead = computed(() => this.isOwner() || this.isCommunityAdmin() || this.isStaff());

  attachTeamModalOpen = signal(false);
  availableTeams = signal<TeamResponse[]>([]);
  loadingAvailableTeams = signal(false);
  selectedTeamId = signal<string | null>(null);
  attachingTeam = signal(false);
  attachTeamError = signal<string | null>(null);

  openAttachTeamModal(): void {
    const id = this.community()?.id ?? this.communityId();
    if (!id) return;

    this.attachTeamModalOpen.set(true);
    this.attachTeamError.set(null);
    this.selectedTeamId.set(null);
    this.loadingAvailableTeams.set(true);

    this.communityService.listAvailableTeams(id).subscribe({
      next: (list) => {
        this.availableTeams.set(list);
        this.loadingAvailableTeams.set(false);
      },
      error: (err) => {
        console.error('Failed to list available teams:', err);
        this.loadingAvailableTeams.set(false);
      },
    });
  }

  closeAttachTeamModal(): void {
    if (this.attachingTeam()) return;
    this.attachTeamModalOpen.set(false);
  }

  confirmAttachTeam(): void {
    const commId = this.community()?.id ?? this.communityId();
    const teamId = this.selectedTeamId();
    if (!commId || !teamId || this.attachingTeam()) return;

    this.attachingTeam.set(true);
    this.attachTeamError.set(null);

    this.communityService.attachTeam(commId, teamId).subscribe({
      next: (updatedComm) => {
        this.community.set(updatedComm);
        this.attachingTeam.set(false);
        this.attachTeamModalOpen.set(false);
        this.toastService.success(this.i18n.t('communities.teamAttachedSuccess'));
      },
      error: (err) => {
        this.attachingTeam.set(false);
        this.attachTeamError.set(err?.error?.message ?? 'Erro ao vincular time.');
      },
    });
  }

  /**
   * Não existe endpoint "sou membro?" nem "qual meu papel?" — a única forma confiável
   * de saber é tentar as próprias ações restritas e observar se a API aceita (200) ou
   * nega (403), já que listCommunityMembers só responde pra quem já é membro ACTIVE e
   * listJoinRequests só responde pra dono/admin/moderador. O dono entra como membro
   * ADMIN na criação, então cai naturalmente no caminho de membro.
   */
  isMember = signal(false);
  isStaff = signal(false);

  members = signal<CommunityMemberResponse[]>([]);
  membersLoading = signal(false);

  joinRequests = signal<CommunityJoinRequestResponse[]>([]);
  joinRequestsLoading = signal(false);

  joining = signal(false);
  joinError = signal<string | null>(null);

  requestingJoin = signal(false);
  joinRequestSent = signal(false);
  joinRequestError = signal<string | null>(null);

  leaving = signal(false);

  editOpen = signal(false);
  editName = signal('');
  editSlug = signal('');
  editDescription = signal('');
  editAvatarUrl = signal('');
  editBannerUrl = signal('');
  editVisibility = signal<CommunityVisibility>('PUBLIC');
  saving = signal(false);
  editError = signal<string | null>(null);

  confirmAction = signal<ConfirmAction>(null);
  actionLoading = signal(false);
  actionError = signal<string | null>(null);

  posts = signal<Post[]>([]);
  postsLoading = signal(false);
  postContent = signal('');
  postMediaItems = signal<PostMediaItemRequest[]>([]);
  uploadingPostMedia = signal(false);
  selectedImageUrl = signal<string | null>(null);
  postVisibility = signal<'PUBLIC' | 'PRIVATE'>('PUBLIC');
  publishing = signal(false);
  postError = signal<string | null>(null);

  openImage(url: string, event?: Event): void {
    if (event) event.stopPropagation();
    this.selectedImageUrl.set(url);
  }

  closeImage(): void {
    this.selectedImageUrl.set(null);
  }

  onPostMediaSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const files = Array.from(input.files);
    const availableSlots = 4 - this.postMediaItems().length;

    if (availableSlots <= 0) {
      this.toastService.info('Você pode adicionar até 4 imagens por publicação.');
      input.value = '';
      return;
    }

    const filesToUpload = files.slice(0, availableSlots);
    this.uploadingPostMedia.set(true);

    let completed = 0;
    filesToUpload.forEach((file) => {
      this.mediaService.uploadImage(file).subscribe({
        next: (res) => {
          const item: PostMediaItemRequest = {
            fileKey: res.fileKey,
            url: res.url,
            thumbnailUrl: res.thumbnailUrl,
            contentType: res.contentType,
            fileSizeBytes: res.fileSizeBytes,
            width: res.width,
            height: res.height,
            displayOrder: this.postMediaItems().length,
          };
          this.postMediaItems.update((items) => [...items, item]);
          completed++;
          if (completed === filesToUpload.length) {
            this.uploadingPostMedia.set(false);
            input.value = '';
          }
        },
        error: (err) => {
          console.error('Failed to upload image:', err);
          this.toastService.error('Erro ao fazer upload da imagem.');
          completed++;
          if (completed === filesToUpload.length) {
            this.uploadingPostMedia.set(false);
            input.value = '';
          }
        },
      });
    });
  }

  removePostMedia(index: number): void {
    this.postMediaItems.update((items) => items.filter((_, i) => i !== index));
  }

  openingChat = signal(false);
  chatError = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.communityId();
    if (!id) {
      this.notFound.set(true);
      this.loading.set(false);
      return;
    }

    this.communityService.detailCommunity(id).subscribe({
      next: (community) => {
        this.community.set(community);
        this.loading.set(false);
        this.loadMembers();
        this.loadPosts();
        if (community.visibility === 'PRIVATE') this.loadJoinRequests();
      },
      error: (error) => {
        console.error('Failed to fetch community:', error);
        this.notFound.set(true);
        this.loading.set(false);
      },
    });

    this.profileService.myProfile().subscribe({
      next: (profile) => this.myUserId.set(profile.userId),
      error: (error) => console.error('Failed to fetch my profile:', error),
    });
  }

  /**
   * Não existe endpoint que devolva a conversa de uma comunidade pelo id dela — a
   * conversa COMMUNITY é encontrada varrendo as conversas de comunidade do usuário,
   * que é justamente a listagem que o backend expõe (e que só inclui comunidades das
   * quais ele é membro ACTIVE, o mesmo requisito pra entrar no chat).
   */
  openCommunityChat(): void {
    const communityId = this.communityId();
    if (!communityId || this.openingChat()) return;

    this.openingChat.set(true);
    this.chatError.set(null);

    this.conversationService.listCommunityConversations(0, 100).subscribe({
      next: (response) => {
        this.openingChat.set(false);
        const conversation = response.content.find((c) => c.communityId === communityId);
        if (!conversation) {
          this.chatError.set('Entre na comunidade para acessar o chat dela.');
          return;
        }
        this.router.navigate(['/chats', conversation.id]);
      },
      error: (error) => {
        console.error('Failed to open community chat:', error);
        this.openingChat.set(false);
        this.chatError.set('Não foi possível abrir o chat dessa comunidade.');
      },
    });
  }

  publishPost(): void {
    const communityId = this.communityId();
    const content = this.postContent().trim();
    const media = this.postMediaItems();
    if (!communityId || (!content && media.length === 0) || this.publishing() || this.uploadingPostMedia()) return;

    if (this.postContent().length > 500) {
      this.toastService.error('A publicação não pode exceder 500 caracteres.');
      return;
    }

    this.publishing.set(true);
    this.postError.set(null);

    this.postsService
      .postInCommunity(communityId, {
        content: content || undefined,
        postType: 'TEXT',
        visibility: this.postVisibility(),
        maps: [],
        media: media.length > 0 ? media : undefined,
      })
      .subscribe({
        next: (post) => {
          this.publishing.set(false);
          this.postContent.set('');
          this.postMediaItems.set([]);
          this.posts.update((list) => [toPost(post), ...list]);
          this.toastService.success('Publicação realizada com sucesso!');
        },
        error: (error) => {
          console.error('Failed to publish community post:', error);
          this.publishing.set(false);
          this.postError.set(error?.error?.message ?? 'Não foi possível publicar nessa comunidade.');
        },
      });
  }

  roleLabel(role: CommunityMemberRole): string {
    return ROLE_LABEL[role] ?? role;
  }

  visibilityLabel(visibility: CommunityVisibility): string {
    return visibility === 'PRIVATE' ? 'Privada' : 'Pública';
  }

  join(): void {
    const id = this.communityId();
    if (!id || this.joining()) return;
    this.joining.set(true);
    this.joinError.set(null);

    this.communityMemberService.joinCommunity(id).subscribe({
      next: () => {
        this.joining.set(false);
        this.loadMembers();
        this.loadPosts();
      },
      error: (error) => {
        this.joining.set(false);
        this.joinError.set(error?.error?.message ?? 'Não foi possível entrar nessa comunidade.');
      },
    });
  }

  requestJoin(): void {
    const id = this.communityId();
    if (!id || this.requestingJoin()) return;
    this.requestingJoin.set(true);
    this.joinRequestError.set(null);

    this.communityJoinRequestService.requestToJoinCommunity(id).subscribe({
      next: () => {
        this.requestingJoin.set(false);
        this.joinRequestSent.set(true);
      },
      error: (error) => {
        this.requestingJoin.set(false);
        if (error?.status === 409) {
          this.joinRequestSent.set(true);
          return;
        }
        this.joinRequestError.set(error?.error?.message ?? 'Não foi possível solicitar entrada.');
      },
    });
  }

  leaveConfirmOpen = signal(false);
  leaveError = signal<string | null>(null);

  askLeave(): void {
    this.leaveError.set(null);
    this.leaveConfirmOpen.set(true);
  }

  cancelLeave(): void {
    if (this.leaving()) return;
    this.leaveConfirmOpen.set(false);
  }

  leave(): void {
    const id = this.communityId();
    if (!id || this.leaving()) return;
    this.leaveConfirmOpen.set(false);
    this.leaving.set(true);

    this.communityMemberService.leaveCommunity(id).subscribe({
      next: () => {
        this.leaving.set(false);
        this.isMember.set(false);
        this.members.set([]);
        this.loadPosts();
      },
      error: (error) => {
        this.leaving.set(false);
        console.error('Failed to leave community:', error);
        this.leaveError.set(error?.error?.message ?? 'Não foi possível sair da comunidade.');
      },
    });
  }

  approveJoinRequest(request: CommunityJoinRequestResponse): void {
    this.communityJoinRequestService.approveJoinRequest(request.id).subscribe({
      next: () => {
        this.joinRequests.update((list) => list.filter((r) => r.id !== request.id));
        this.loadMembers();
      },
      error: (error) => console.error('Failed to approve join request:', error),
    });
  }

  rejectJoinRequest(request: CommunityJoinRequestResponse): void {
    this.communityJoinRequestService.rejectJoinRequest(request.id).subscribe({
      next: () => this.joinRequests.update((list) => list.filter((r) => r.id !== request.id)),
      error: (error) => console.error('Failed to reject join request:', error),
    });
  }

  openEdit(): void {
    const community = this.community();
    if (!community) return;
    this.editName.set(community.communityName);
    this.editSlug.set(community.communitySlug);
    this.editDescription.set(community.communityDescription ?? '');
    this.editAvatarUrl.set(community.communityAvatarUrl ?? '');
    this.editBannerUrl.set(community.communityBannerUrl ?? '');
    this.editVisibility.set(community.visibility);
    this.editError.set(null);
    this.editOpen.set(true);
  }

  closeEdit(): void {
    if (this.saving()) return;
    this.editOpen.set(false);
  }

  saveEdit(): void {
    const id = this.communityId();
    if (!id || this.saving()) return;
    if (!this.editName().trim() || !this.editSlug().trim()) {
      this.editError.set('Nome e slug são obrigatórios.');
      return;
    }

    this.saving.set(true);
    this.editError.set(null);

    this.communityService
      .editCommunity(id, {
        communityName: this.editName().trim(),
        communitySlug: this.editSlug().trim(),
        communityDescription: this.editDescription().trim(),
        communityAvatarUrl: this.editAvatarUrl().trim() || undefined,
        communityBannerUrl: this.editBannerUrl().trim() || undefined,
        visibility: this.editVisibility(),
      })
      .subscribe({
        next: (updated) => {
          this.community.set(updated);
          this.saving.set(false);
          this.editOpen.set(false);
        },
        error: (error) => {
          this.saving.set(false);
          this.editError.set(error?.error?.message ?? 'Não foi possível salvar as alterações.');
        },
      });
  }

  openConfirmAction(action: ConfirmAction): void {
    this.actionError.set(null);
    this.confirmAction.set(action);
  }

  closeConfirmAction(): void {
    if (this.actionLoading()) return;
    this.confirmAction.set(null);
  }

  confirmActionSubmit(): void {
    const id = this.communityId();
    const action = this.confirmAction();
    if (!id || !action || this.actionLoading()) return;

    this.actionLoading.set(true);
    this.actionError.set(null);

    const request$ = action === 'archive' ? this.communityService.archiveCommunity(id) : this.communityService.deleteCommunity(id);
    request$.subscribe({
      next: () => {
        this.actionLoading.set(false);
        this.confirmAction.set(null);
        this.router.navigateByUrl('/comunidade');
      },
      error: (error) => {
        this.actionLoading.set(false);
        this.actionError.set(error?.error?.message ?? 'Não foi possível concluir essa ação.');
      },
    });
  }

  private loadPosts(): void {
    const id = this.community()?.id ?? this.communityId();
    if (!id) return;
    this.postsLoading.set(true);

    this.postsService.getCommunityPosts(id).subscribe({
      next: (response) => {
        this.posts.set(response.content.map((post) => toPost(post)));
        this.postsLoading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch community posts:', error);
        this.postsLoading.set(false);
      },
    });
  }

  private loadMembers(): void {
    const id = this.community()?.id ?? this.communityId();
    if (!id) return;
    this.membersLoading.set(true);

    this.communityMemberService.listCommunityMembers(id).subscribe({
      next: (response) => {
        this.members.set(response.content);
        this.isMember.set(true);
        this.membersLoading.set(false);
      },
      error: (error) => {
        this.isMember.set(false);
        this.membersLoading.set(false);
        if (error?.status !== 403) console.error('Failed to fetch community members:', error);
      },
    });
  }

  private loadJoinRequests(): void {
    const id = this.community()?.id ?? this.communityId();
    if (!id) return;
    this.joinRequestsLoading.set(true);

    this.communityJoinRequestService.listJoinRequests(id).subscribe({
      next: (response) => {
        this.joinRequests.set(response.content);
        this.isStaff.set(true);
        this.joinRequestsLoading.set(false);
      },
      error: (error) => {
        this.isStaff.set(false);
        this.joinRequestsLoading.set(false);
        if (error?.status !== 403) console.error('Failed to fetch join requests:', error);
      },
    });
  }
}
