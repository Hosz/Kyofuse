import { SocialLinks } from './social-links.model';

export type ProfileViewMode = 'owner' | 'visitor' | 'restricted';

export type ProfileTabId = 'posts' | 'reposts' | 'media' | 'replies';

export interface ProfileStats {
  followers: number;
  following: number;
  friends: number;
  posts: number;
}

export interface ProfileRankStats {
  premier: string;
  faceit: string;
  gc: string;
}

export interface ProfileHighlight {
  icon: string;
  label: string;
}

export interface ProfileEntity {
  id: string;
  name: string;
  meta: string;
  /** Ícone exibido quando a entidade não tem imagem própria cadastrada. */
  icon: string;
  imageUrl?: string | null;
  route: string;
}

export interface UserListEntry {
  id: string;
  name: string;
  handle: string;
  avatarUrl: string;
  isFollowing?: boolean;
}

export interface ProfileUser {
  name: string;
  handle: string;
  avatarUrl: string;
  bannerUrl?: string;
  bio: string;
  verified?: boolean;
  isPrivate: boolean;
  /** Se o visitante já segue este perfil — controla acesso quando isPrivate = true. */
  viewerFollows?: boolean;
  stats: ProfileStats;
  rankStats: ProfileRankStats;
  socialLinks?: SocialLinks;
  highlights: ProfileHighlight[];
  communities: ProfileEntity[];
  teams: ProfileEntity[];
  followersList: UserListEntry[];
  followingList: UserListEntry[];
  friendsList: UserListEntry[];
}
