import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ConversationService } from '../../core/services/chat/conversation.service';
import { CommunityService } from '../../core/services/communities/community.service';
import { TeamService } from '../../core/services/teams/team.service';
import { ProfileEntity } from '../../shared/models/profile.model';
import { catchError, forkJoin, map, of } from 'rxjs';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { ProfileHeaderComponent } from '../../components/profile/profile-header/profile-header';
import { ProfileHighlightsComponent } from '../../components/profile/profile-highlights/profile-highlights';
import { ProfileLockedComponent } from '../../components/profile/profile-locked/profile-locked';
import { EntitySummaryCardComponent } from '../../components/profile/entity-summary-card/entity-summary-card';
import { EntityListModalComponent } from '../../components/profile/entity-list-modal/entity-list-modal';
import { UserListModalComponent } from '../../components/profile/user-list-modal/user-list-modal';
import { InviteModalComponent } from '../../components/profile/invite-modal/invite-modal';
import { FeedTabsComponent } from '../../components/feed/feed-tabs/feed-tabs';
import { FeedListComponent } from '../../components/feed/feed-list/feed-list';
import { PostCardComponent } from '../../components/feed/post-card/post-card';
import { CommentCardComponent } from '../../components/feed/comment-card/comment-card';
import { FeedTab, Comment, Post } from '../../shared/models/social.model';
import { ProfileRankStats, ProfileViewMode } from '../../shared/models/profile.model';
import { ProfileService } from '../../core/services/profile/profile.service';
import { gamerProfileResponse } from '../../models/profile/gamer-profile.model';
import { AuthService } from '../../core/services/auth/auth.service';
import { CurrentUserService } from '../../core/services/profile/current-user.service';
import { PostsService } from '../../core/services/posts/posts.service';
import { CommentsService } from '../../core/services/comments/comments.service';
import { FollowService } from '../../core/services/follow/follow.service';
import { FriendshipService } from '../../core/services/friendship/friendship.service';
import { FriendRequestService } from '../../core/services/friendship/friend-request.service';
import { toComment, toPost } from '../../shared/utils/mappers.util';

type ProfileTabId = 'posts' | 'reposts' | 'media' | 'replies';
type ModalKind = 'followers' | 'following' | 'friends' | 'communities' | 'teams' | 'invite' | null;

export interface ReplyItem {
  comment: Comment;
  post: Post | null;
}

const EMPTY_PROFILE: gamerProfileResponse = {
  id: '',
  userId: '',
  username: '',
  nickname: '',
  bio: '',
  avatarUrl: '',
  bannerUrl: '',
  country: '',
  city: '',
  state: '',
  mainRole: '',
  secondaryRole: '',
  premierRating: 0,
  faceitLevel: 0,
  gcRank: 0,
  playstyle: '',
  lookingForTeam: false,
  lookingForDuo: false,
  setupStatus: '',
  favoriteMaps: [],
};

@Component({
  selector: 'app-profile',
  imports: [
    AppSidebarComponent,
    ProfileHeaderComponent,
    ProfileHighlightsComponent,
    ProfileLockedComponent,
    EntitySummaryCardComponent,
    EntityListModalComponent,
    UserListModalComponent,
    InviteModalComponent,
    FeedTabsComponent,
    FeedListComponent,
    PostCardComponent,
    CommentCardComponent,
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class ProfileComponent {
  /** Vinculado automaticamente ao parâmetro de rota :username (withComponentInputBinding).
   * Ausente = visualizando o próprio perfil. */
  username = input<string | null>(null);

  viewMode = signal<ProfileViewMode>('owner');

  authService = inject(AuthService);
  currentUser = inject(CurrentUserService);
  profileService = inject(ProfileService);
  postService = inject(PostsService);
  commentsService = inject(CommentsService);
  followService = inject(FollowService);
  friendshipService = inject(FriendshipService);
  friendRequestService = inject(FriendRequestService);
  private router = inject(Router);
  private conversationService = inject(ConversationService);
  private communityService = inject(CommunityService);
  private teamService = inject(TeamService);

  profile = signal<gamerProfileResponse>(EMPTY_PROFILE);
  startingConversation = signal(false);
  messageError = signal<string | null>(null);

  communities = signal<ProfileEntity[]>([]);
  teams = signal<ProfileEntity[]>([]);

  /**
   * Perfil privado de quem não é amigo nem seguidor: a identidade aparece normalmente,
   * mas a API nega posts, respostas e as listas de conexões. Um 403 nesses endpoints é
   * resposta esperada, não falha — por isso vira estado de tela, não erro de console.
   */
  contentRestricted = signal(false);
  posts = signal<Post[]>([]);
  postsCount = signal(0);
  private postsPage = signal(0);
  private postsLastPage = signal(true);
  postsLoadingMore = signal(false);
  hasMorePostsToLoad = computed(() => !this.postsLastPage());
  followersCount = signal(0);
  followingCount = signal(0);
  friendsCount = signal(0);

  viewerIsFollowing = signal(false);
  followActionPending = signal(false);

  friendRequestSent = signal(false);
  friendRequestPending = signal(false);
  private sentFriendRequestId: string | null = null;

  activeModal = signal<ModalKind>(null);
  activeTabId = signal<ProfileTabId>('posts');

  mediaPosts = signal<Post[]>([]);
  mediaPostsLoading = signal(false);
  private mediaPostsLoaded = false;
  private mediaPostsPage = signal(0);
  private mediaPostsLastPage = signal(true);
  mediaPostsLoadingMore = signal(false);
  hasMoreMediaToLoad = computed(() => !this.mediaPostsLastPage());

  replies = signal<ReplyItem[]>([]);
  repliesLoading = signal(false);
  private repliesLoaded = false;
  private repliesPage = signal(0);
  private repliesLastPage = signal(true);
  repliesLoadingMore = signal(false);
  hasMoreRepliesToLoad = computed(() => !this.repliesLastPage());

  profileEmptyTitle = computed(() =>
    this.viewMode() === 'owner' ? 'Você ainda não publicou nada' : 'Nenhuma publicação ainda',
  );

  profileEmptyMessage = computed(() =>
    this.viewMode() === 'owner'
      ? 'Compartilhe um clipe, tática ou momento com a comunidade!'
      : 'Esse perfil não postou nada ainda.',
  );

  mediaEmptyTitle = computed(() =>
    this.viewMode() === 'owner' ? 'Você ainda não publicou nenhuma mídia' : 'Nenhuma mídia ainda',
  );

  mediaEmptyMessage = computed(() =>
    this.viewMode() === 'owner'
      ? 'Compartilhe fotos, jogadas e táticas com a comunidade!'
      : 'Esse perfil não publicou nenhuma mídia ainda.',
  );

  constructor() {
    effect(() => {
      const targetUsername = this.username();
      this.loadProfileForUser(targetUsername);
    });
  }

  private resetState(): void {
    this.profile.set(EMPTY_PROFILE);
    this.viewMode.set('owner');
    this.contentRestricted.set(false);
    this.posts.set([]);
    this.postsCount.set(0);
    this.postsPage.set(0);
    this.postsLastPage.set(true);
    this.postsLoadingMore.set(false);
    this.followersCount.set(0);
    this.followingCount.set(0);
    this.friendsCount.set(0);
    this.viewerIsFollowing.set(false);
    this.followActionPending.set(false);
    this.friendRequestSent.set(false);
    this.friendRequestPending.set(false);
    this.sentFriendRequestId = null;
    this.mediaPosts.set([]);
    this.mediaPostsLoaded = false;
    this.mediaPostsPage.set(0);
    this.mediaPostsLastPage.set(true);
    this.mediaPostsLoadingMore.set(false);
    this.replies.set([]);
    this.repliesLoaded = false;
    this.repliesPage.set(0);
    this.repliesLastPage.set(true);
    this.repliesLoadingMore.set(false);
    this.communities.set([]);
    this.teams.set([]);
  }

  private loadProfileForUser(targetUsername: string | null): void {
    this.authService.me().subscribe({
      next: (me) => {
        const isMe = !targetUsername || (!!me?.username && targetUsername.toLowerCase() === me.username.toLowerCase());
        if (isMe) {
          if (
            this.viewMode() === 'owner' &&
            this.profile().username &&
            targetUsername &&
            this.profile().username.toLowerCase() === targetUsername.toLowerCase()
          ) {
            return;
          }
          this.resetState();
          this.loadOwnProfile();
          if (!targetUsername && me?.username) {
            this.router.navigate(['/perfil', me.username], { replaceUrl: true });
          }
        } else {
          this.resetState();
          this.loadOtherProfile(targetUsername!);
        }
      },
      error: () => {
        this.resetState();
        if (targetUsername) {
          this.loadOtherProfile(targetUsername);
        } else {
          this.loadOwnProfile();
        }
      },
    });
  }

  rankStats = computed<ProfileRankStats>(() => ({
    premier: this.profile().premierRating ? this.formatCount(this.profile().premierRating) : 'Sem nível',
    faceit: this.profile().faceitLevel ? `Nível ${this.profile().faceitLevel}` : 'Sem nível',
    gc: this.profile().gcRank ? `${this.profile().gcRank}` : 'Sem nível',
  }));

  profileTabs = computed<FeedTab[]>(() => [
    { label: 'Posts', active: this.activeTabId() === 'posts' },
    { label: 'Reposts', active: this.activeTabId() === 'reposts' },
    { label: 'Mídia', active: this.activeTabId() === 'media' },
    { label: 'Respostas', active: this.activeTabId() === 'replies' },
  ]);

  onTabSelected(tab: FeedTab): void {
    if (tab.label === 'Posts') this.activeTabId.set('posts');
    else if (tab.label === 'Reposts') this.activeTabId.set('reposts');
    else if (tab.label === 'Mídia') {
      this.activeTabId.set('media');
      this.loadMediaPostsIfNeeded();
    } else {
      this.activeTabId.set('replies');
      this.loadRepliesIfNeeded();
    }
  }

  goToReply(reply: ReplyItem): void {
    this.router.navigate(['/post', reply.comment.postId], { queryParams: { comment: reply.comment.id } });
  }

  /** Sem isso o botão nascia sempre como "Seguir", e clicar em quem já seguimos
   * disparava um follow duplicado que o backend recusa. */
  private loadFollowState(targetUserId: string): void {
    this.followService.isFollowing(targetUserId).subscribe({
      next: (following) => this.viewerIsFollowing.set(following),
      error: (error) => console.error('Failed to fetch follow state:', error),
    });
  }

  onToggleFollow(): void {
    const targetUserId = this.profile().userId;
    if (!targetUserId || this.followActionPending()) return;

    const wasFollowing = this.viewerIsFollowing();
    this.viewerIsFollowing.set(!wasFollowing);
    this.followActionPending.set(true);

    const onError = (error: unknown) => {
      console.error('Failed to toggle follow:', error);
      this.viewerIsFollowing.set(wasFollowing);
      this.followActionPending.set(false);
    };
    const onSuccess = () => this.followActionPending.set(false);

    if (wasFollowing) {
      this.followService.unfollowUser(targetUserId).subscribe({ next: onSuccess, error: onError });
    } else {
      this.followService.followUser(targetUserId).subscribe({ next: onSuccess, error: onError });
    }
  }

  onToggleFriendRequest(): void {
    const targetUserId = this.profile().userId;
    if (!targetUserId || this.friendRequestPending()) return;

    this.friendRequestPending.set(true);

    if (this.friendRequestSent()) {
      const requestId = this.sentFriendRequestId;
      if (!requestId) {
        this.friendRequestPending.set(false);
        return;
      }

      this.friendRequestService.removeRequest(requestId).subscribe({
        next: () => {
          this.friendRequestSent.set(false);
          this.sentFriendRequestId = null;
          this.friendRequestPending.set(false);
        },
        error: (error) => {
          console.error('Failed to cancel friend request:', error);
          this.friendRequestPending.set(false);
        },
      });
      return;
    }

    this.friendRequestService.sendRequest(targetUserId).subscribe({
      next: (response) => {
        this.friendRequestSent.set(true);
        this.sentFriendRequestId = response.id;
        this.friendRequestPending.set(false);
      },
      error: (error) => {
        console.error('Failed to send friend request:', error);
        this.friendRequestPending.set(false);
      },
    });
  }

  /**
   * Times e comunidades do dono do perfil. Carregado depois que o perfil resolve,
   * porque a rota do próprio perfil (/perfil, sem :userId) só descobre o userId ali.
   */
  private loadMemberships(userId: string): void {
    this.communityService.listUserCommunities(userId).subscribe({
      next: (response) =>
        this.communities.set(
          response.content.map((community) => ({
            id: community.id,
            name: community.communityName,
            meta: community.visibility === 'PRIVATE' ? 'Privada' : 'Pública',
            icon: 'groups_2',
            imageUrl: community.communityAvatarUrl,
            route: `/comunidade/${community.id}`,
          })),
        ),
      error: (error) => console.error('Failed to fetch user communities:', error),
    });

    this.teamService.listingUserTeams(userId).subscribe({
      next: (response) =>
        this.teams.set(
          response.content.map((team) => ({
            id: team.id,
            name: team.name,
            meta: team.region || 'Time',
            icon: 'groups',
            imageUrl: team.avatarUrl,
            route: `/times/${team.id}`,
          })),
        ),
      error: (error) => console.error('Failed to fetch user teams:', error),
    });
  }

  onProfileBlocked(): void {
    this.router.navigateByUrl('/home');
  }

  /**
   * createConversation é idempotente pra DIRECT: se já existir conversa com essa
   * pessoa o backend devolve a existente em vez de criar outra, então dá pra usar
   * tanto pra iniciar quanto pra reabrir uma conversa.
   */
  onMessageClick(): void {
    const targetUserId = this.profile().userId;
    if (!targetUserId || this.startingConversation()) return;

    this.startingConversation.set(true);
    this.conversationService.createConversation({ participantIds: [targetUserId] }).subscribe({
      next: (conversation) => {
        this.startingConversation.set(false);
        this.router.navigate(['/chats', conversation.id]);
      },
      error: (error) => {
        console.error('Failed to start conversation:', error);
        this.startingConversation.set(false);
        this.messageError.set(
          error?.error?.message ?? 'Não foi possível iniciar uma conversa com esse usuário.',
        );
      },
    });
  }

  onAuthorBlocked(authorId: string): void {
    if (authorId === this.profile().userId) {
      this.router.navigateByUrl('/home');
      return;
    }

    this.posts.update((list) => list.filter((post) => post.author.id !== authorId));
    this.mediaPosts.update((list) => list.filter((post) => post.author.id !== authorId));
  }

  onReplyDeleted(commentId: string): void {
    this.replies.update((list) => list.filter((reply) => reply.comment.id !== commentId));
  }

  onPostDeleted(postId: string): void {
    this.posts.update((list) => list.filter((post) => post.id !== postId));
    this.mediaPosts.update((list) => list.filter((post) => post.id !== postId));
  }

  openModal(kind: ModalKind): void {
    this.activeModal.set(kind);
  }

  closeModal(): void {
    this.activeModal.set(null);
  }

  private loadOwnProfile(): void {
    this.viewMode.set('owner');

    this.profileService.myProfile().subscribe({
      next: (response) => {
        this.profile.set(response);
        this.currentUser.setProfile(response);
        this.loadMemberships(response.userId);
      },
      error: (error) => console.error('Failed to fetch my profile:', error),
    });

    this.loadPosts(0);

    this.followService.showMyFollowersQuantity().subscribe({
      next: (count) => this.followersCount.set(count ?? 0),
      error: (error) => {
        if (error?.status !== 403) console.error('Failed to fetch followers count:', error);
      },
    });

    this.followService.showMyFollowingQuantity().subscribe({
      next: (count) => this.followingCount.set(count ?? 0),
      error: (error) => {
        if (error?.status !== 403) console.error('Failed to fetch following count:', error);
      },
    });

    this.friendshipService.showMyFriendsQuantity().subscribe({
      next: (count) => this.friendsCount.set(count),
      error: (error) => {
        if (error?.status !== 403) console.error('Failed to fetch friends count:', error);
      },
    });
  }

  private loadOtherProfile(targetUsername: string): void {
    this.profileService.userProfile(targetUsername).subscribe({
      next: (response) => {
        if (this.currentUser.isMe(response.userId) || this.currentUser.isMe(response.username)) {
          this.loadOwnProfile();
          return;
        }
        this.profile.set(response);
        this.viewMode.set('visitor');
        this.loadFollowState(response.userId);
        this.loadOtherProfileExtras(response.userId);
        this.loadMemberships(response.userId);
      },
      error: (error) => {
        // Depois que perfil privado passou a ser visível, um 403 aqui significa bloqueio.
        if (error?.status !== 403) console.error('Failed to fetch user profile:', error);
        this.viewMode.set('restricted');
      },
    });
  }

  private loadOtherProfileExtras(targetUserId: string): void {
    this.loadPosts(0);

    this.followService.showFollowersQuantity(targetUserId).subscribe({
      next: (count) => this.followersCount.set(count ?? 0),
      error: (error) => {
        if (error?.status !== 403) console.error('Failed to fetch followers count:', error);
      },
    });

    this.followService.showFollowingQuantity(targetUserId).subscribe({
      next: (count) => this.followingCount.set(count ?? 0),
      error: (error) => {
        if (error?.status !== 403) console.error('Failed to fetch following count:', error);
      },
    });

    this.friendshipService.showUserFriendsQuantity(targetUserId).subscribe({
      next: (count) => this.friendsCount.set(count),
      error: (error) => {
        if (error?.status !== 403) console.error('Failed to fetch friends count:', error);
      },
    });

    /** Não existe endpoint pra checar diretamente "já mandei pedido pra esse usuário?",
     * então procuramos nos meus pedidos enviados. */
    this.friendRequestService.showSentRequests().subscribe({
      next: (response) => {
        const existing = response.content.find((request) => request.receiverId === targetUserId);
        if (existing) {
          this.friendRequestSent.set(true);
          this.sentFriendRequestId = existing.id;
        }
      },
      error: (error) => console.error('Failed to fetch sent friend requests:', error),
    });
  }

  loadMorePosts(): void {
    if (this.postsLoadingMore() || this.postsLastPage()) return;
    this.postsLoadingMore.set(true);
    this.loadPosts(this.postsPage() + 1);
  }

  loadMoreMediaPosts(): void {
    if (this.mediaPostsLoadingMore() || this.mediaPostsLastPage()) return;
    this.mediaPostsLoadingMore.set(true);
    this.loadMediaPosts(this.mediaPostsPage() + 1);
  }

  loadMoreReplies(): void {
    if (this.repliesLoadingMore() || this.repliesLastPage()) return;
    this.repliesLoadingMore.set(true);
    this.loadReplies(this.repliesPage() + 1);
  }

  private loadMediaPostsIfNeeded(): void {
    if (this.mediaPostsLoaded) return;
    this.mediaPostsLoaded = true;
    this.mediaPostsLoading.set(true);
    this.loadMediaPosts(0);
  }

  private loadMediaPosts(page: number): void {
    const targetUserId = this.profile().userId;
    const request = this.viewMode() !== 'owner' && targetUserId
      ? this.postService.getProfileMediaPosts(targetUserId, page)
      : this.postService.getMyMediaPosts(page);

    request.subscribe({
      next: (response) => {
        const mapped = response.content.map((post) => toPost(post));
        this.mediaPosts.update((list) => (page === 0 ? mapped : [...list, ...mapped]));
        this.mediaPostsLastPage.set(response.last);
        this.mediaPostsPage.set(page);
        this.mediaPostsLoading.set(false);
        this.mediaPostsLoadingMore.set(false);
      },
      error: (error) => {
        if (error?.status === 403) {
          this.contentRestricted.set(true);
        } else {
          console.error('Failed to fetch media posts:', error);
        }
        this.mediaPostsLoading.set(false);
        this.mediaPostsLoadingMore.set(false);
      },
    });
  }

  private loadPosts(page: number): void {
    const targetUserId = this.profile().userId;
    const request = this.viewMode() !== 'owner' && targetUserId
      ? this.postService.getProfilePosts(targetUserId, page)
      : this.postService.getMyPosts(page);

    request.subscribe({
      next: (response) => {
        const mapped = response.content.map((post) => toPost(post));
        this.posts.update((list) => (page === 0 ? mapped : [...list, ...mapped]));
        this.postsCount.set(response.totalElements);
        this.postsLastPage.set(response.last);
        this.postsPage.set(page);
        this.postsLoadingMore.set(false);
      },
      error: (error) => {
        if (error?.status === 403) {
          this.contentRestricted.set(true);
        } else {
          console.error('Failed to fetch posts:', error);
        }
        this.postsLoadingMore.set(false);
      },
    });
  }

  private loadRepliesIfNeeded(): void {
    if (this.repliesLoaded) return;
    this.repliesLoaded = true;
    this.repliesLoading.set(true);
    this.loadReplies(0);
  }

  private loadReplies(page: number): void {
    const targetUserId = this.profile().userId;
    if (!targetUserId) {
      this.repliesLoading.set(false);
      this.repliesLoadingMore.set(false);
      return;
    }

    this.commentsService.listUserComments(targetUserId, page).subscribe({
      next: (response) => {
        const comments = response.content.map((comment) => toComment(comment));
        this.repliesLastPage.set(response.last);
        this.repliesPage.set(page);

        if (comments.length === 0) {
          if (page === 0) this.replies.set([]);
          this.repliesLoading.set(false);
          this.repliesLoadingMore.set(false);
          return;
        }

        forkJoin(
          comments.map((comment) =>
            this.postService.getPost(comment.postId).pipe(
              map((post) => toPost(post)),
              catchError(() => of(null)),
            ),
          ),
        ).subscribe((posts) => {
          const mapped = comments.map((comment, index) => ({ comment, post: posts[index] }));
          this.replies.update((list) => (page === 0 ? mapped : [...list, ...mapped]));
          this.repliesLoading.set(false);
          this.repliesLoadingMore.set(false);
        });
      },
      error: (error) => {
        if (error?.status === 403) {
          this.contentRestricted.set(true);
        } else {
          console.error('Failed to fetch user comments:', error);
        }
        this.repliesLoading.set(false);
        this.repliesLoadingMore.set(false);
      },
    });
  }

  private formatCount(value: number): string {
    if (value >= 1_000) return `${(value).toLocaleString('pt-BR', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}`;
    return `${value}`;
  }
}
