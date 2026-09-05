import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { DiscoverySidebarComponent } from '../../components/discovery/discovery-sidebar/discovery-sidebar';
import { PostCardComponent } from '../../components/feed/post-card/post-card';
import { MentionInputComponent } from '../../shared/components/mention-input/mention-input';
import { CommentCardComponent } from '../../components/feed/comment-card/comment-card';
import { PostsService } from '../../core/services/posts/posts.service';
import { CommentsService } from '../../core/services/comments/comments.service';
import { Comment, Post } from '../../shared/models/social.model';
import { toComment, toPost } from '../../shared/utils/mappers.util';
import { TranslatePipe } from '../../core/i18n/translate.pipe';

@Component({
  selector: 'app-post-detail',
  imports: [MentionInputComponent, RouterLink, FormsModule, AppSidebarComponent, DiscoverySidebarComponent, PostCardComponent, CommentCardComponent, TranslatePipe],
  templateUrl: './post-detail.html',
  styleUrl: './post-detail.css',
})
export class PostDetailComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private postsService = inject(PostsService);
  private commentsService = inject(CommentsService);

  private postId = '';

  loading = signal(true);
  post = signal<Post | null>(null);
  comments = signal<Comment[]>([]);
  commentDraft = signal('');
  posting = signal(false);

  private commentsPage = signal(0);
  private commentsLastPage = signal(true);
  commentsLoadingMore = signal(false);
  hasMoreCommentsToLoad = computed(() => !this.commentsLastPage());

  /** Vindo de "Respostas" no perfil (?comment=id) — rola até o comentário e destaca ele, tipo Twitter. */
  highlightedCommentId = signal<string | null>(null);

  ngOnInit(): void {
    this.postId = this.route.snapshot.paramMap.get('postId') ?? '';
    this.highlightedCommentId.set(this.route.snapshot.queryParamMap.get('comment'));

    if (!this.postId) {
      this.loading.set(false);
      return;
    }

    this.postsService.getPost(this.postId).subscribe({
      next: (response) => {
        this.post.set(toPost(response));
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch post:', error);
        this.loading.set(false);
      },
    });

    this.loadComments();
  }

  onAuthorBlocked(): void {
    this.router.navigateByUrl('/home');
  }

  /** O post apagado não existe mais — não faz sentido continuar na página dele. */
  onPostDeleted(): void {
    this.router.navigateByUrl('/home');
  }

  onCommentDeleted(commentId: string): void {
    this.comments.update((list) => list.filter((comment) => comment.id !== commentId));
  }

  onCommentAuthorBlocked(authorId: string): void {
    this.comments.update((list) => list.filter((comment) => comment.authorId !== authorId));
  }

  submitComment(): void {
    const content = this.commentDraft().trim();
    if (!content || this.posting()) return;

    this.posting.set(true);
    this.commentsService.postComment(this.postId, { content }).subscribe({
      next: (comment) => {
        this.comments.update((list) => [toComment(comment), ...list]);
        this.commentDraft.set('');
        this.posting.set(false);
      },
      error: (error) => {
        console.error('Failed to post comment:', error);
        this.posting.set(false);
      },
    });
  }

  loadMoreComments(): void {
    if (this.commentsLoadingMore() || this.commentsLastPage()) return;
    this.commentsLoadingMore.set(true);
    this.loadComments(this.commentsPage() + 1);
  }

  private loadComments(page: number = 0): void {
    this.commentsService.listComments(this.postId, page).subscribe({
      next: (response) => {
        const mapped = response.content.map((comment) => toComment(comment));
        this.comments.update((list) => (page === 0 ? mapped : [...list, ...mapped]));
        this.commentsLastPage.set(response.last);
        this.commentsPage.set(page);
        this.commentsLoadingMore.set(false);
        if (page === 0) this.scrollToHighlightedComment();
      },
      error: (error) => {
        console.error('Failed to fetch comments:', error);
        this.commentsLoadingMore.set(false);
      },
    });
  }

  private scrollToHighlightedComment(): void {
    const commentId = this.highlightedCommentId();
    if (!commentId) return;

    setTimeout(() => {
      document.getElementById(`comment-${commentId}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    });
  }
}
