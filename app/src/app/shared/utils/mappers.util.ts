import { Comment, Post } from '../models/social.model';
import { postResponse } from '../../models/posts/posts-response.model';
import { CommentResponse } from '../../models/comments/comments.model';
import { FALLBACK_AVATAR_URL, toTimeAgo } from './format.util';

export function toPost(post: postResponse): Post {
  return {
    id: post.id,
    author: {
      id: post.authorId,
      name: post.authorNickname,
      handle: post.authorUsername,
      avatarUrl: post.authorAvatarUrl || FALLBACK_AVATAR_URL,
    },
    timeAgo: toTimeAgo(post.createdAt),
    content: post.content,
    stats: {
      comments: post.commentCount,
      reposts: 0,
      likes: post.likeCount,
      reactions: post.reactionCount,
    },
  };
}

export function toComment(comment: CommentResponse): Comment {
  return {
    id: comment.id,
    postId: comment.postId,
    authorId: comment.authorId,
    author: {
      id: comment.authorId,
      name: comment.authorNickname,
      handle: comment.authorUsername,
      avatarUrl: comment.profileImage || FALLBACK_AVATAR_URL,
    },
    timeAgo: toTimeAgo(comment.createdAt),
    content: comment.content,
    stats: {
      likes: comment.likeCount,
      reactions: comment.reactionCount,
    },
  };
}
