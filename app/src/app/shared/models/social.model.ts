export interface NavItem {
  icon: string;
  label: string;
  route: string;
}

export interface UserProfile {
  username: string;
  rank: string;
  avatarUrl: string;
}

export interface FeedTab {
  label: string;
  active?: boolean;
}

export interface PostAuthor {
  id: string;
  name: string;
  handle: string;
  avatarUrl: string;
  badge?: string;
  badgeTone?: 'tertiary' | 'primary';
}

export interface PostMedia {
  imageUrl: string;
  tag?: string;
}

export interface PostStats {
  comments: number;
  reposts: number;
  likes: number;
  reactions: number;
}

export interface Post {
  id: string;
  author: PostAuthor;
  timeAgo: string;
  content: string;
  media?: PostMedia;
  stats: PostStats;
}

export interface CommentStats {
  likes: number;
  reactions: number;
}

export interface Comment {
  id: string;
  postId: string;
  authorId: string;
  author: PostAuthor;
  timeAgo: string;
  content: string;
  stats: CommentStats;
}

export interface SuggestedProfile {
  name: string;
  handle: string;
  avatarUrl: string;
}

export interface FeaturedTeam {
  name: string;
  playersCount: string;
  status: string;
}