import { Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { ModalComponent } from '../../components/shared/modal/modal';
import { CommunityService } from '../../core/services/communities/community.service';
import { ProfileService } from '../../core/services/profile/profile.service';
import { CommunityMemberService } from '../../core/services/communities/community-member.service';
import {
  CommunityMemberResponse,
  CommunityMemberRole,
  CommunityResponse,
} from '../../models/communities/community.model';
import { FALLBACK_AVATAR_URL } from '../../shared/utils/format.util';

interface RoleGroup {
  role: CommunityMemberRole;
  label: string;
  members: CommunityMemberResponse[];
}

/** Ordem de exibição dos grupos, do papel mais alto pro mais baixo (doc.md 10.5). */
const ROLE_ORDER: { role: CommunityMemberRole; label: string }[] = [
  { role: 'ADMIN', label: 'Administradores' },
  { role: 'MODERATOR', label: 'Moderadores' },
  { role: 'MEMBER', label: 'Membros' },
];

const MEMBERS_PAGE_SIZE = 100;

@Component({
  selector: 'app-community-members',
  imports: [RouterLink, AppSidebarComponent, ModalComponent],
  templateUrl: './community-members.html',
  styleUrl: './community-members.css',
})
export class CommunityMembersComponent {
  /** Vinculado automaticamente ao parâmetro de rota :communityId (withComponentInputBinding). */
  communityId = input<string | null>(null);

  private communityService = inject(CommunityService);
  private communityMemberService = inject(CommunityMemberService);
  private profileService = inject(ProfileService);

  readonly fallbackAvatar = FALLBACK_AVATAR_URL;

  community = signal<CommunityResponse | null>(null);
  members = signal<CommunityMemberResponse[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  query = signal('');
  myUserId = signal<string | null>(null);

  removeConfirmMember = signal<CommunityMemberResponse | null>(null);
  removing = signal(false);
  removeError = signal<string | null>(null);

  /** Owner, Admin ou Moderador podem remover — mesma hierarquia do backend (doc.md 10.5).
   * Diferente da página da comunidade, aqui o próprio papel sai da lista de membros, sem
   * precisar deduzir por tentativa e erro nas chamadas restritas. */
  iAmStaff = computed(() => {
    const myUserId = this.myUserId();
    if (!myUserId) return false;
    if (this.community()?.ownerId === myUserId) return true;
    const mine = this.members().find((member) => member.memberId === myUserId);
    return mine?.role === 'ADMIN' || mine?.role === 'MODERATOR';
  });

  myRole = computed<'OWNER' | 'ADMIN' | 'MODERATOR' | 'MEMBER' | null>(() => {
    const myUserId = this.myUserId();
    if (!myUserId) return null;
    if (this.community()?.ownerId === myUserId) return 'OWNER';
    const mine = this.members().find((member) => member.memberId === myUserId);
    return (mine?.role as 'ADMIN' | 'MODERATOR' | 'MEMBER') ?? null;
  });

  private visibleMembers = computed(() => {
    const query = this.query().trim().toLowerCase();
    if (!query) return this.members();
    return this.members().filter(
      (member) =>
        member.memberUsername.toLowerCase().includes(query) ||
        (member.memberNickname ?? '').toLowerCase().includes(query),
    );
  });

  /** Só grupos com pelo menos um membro aparecem, pra não exibir seções vazias. */
  groups = computed<RoleGroup[]>(() =>
    ROLE_ORDER.map(({ role, label }) => ({
      role,
      label,
      members: this.visibleMembers().filter((member) => member.role === role),
    })).filter((group) => group.members.length > 0),
  );

  totalMembers = computed(() => this.members().length);

  ngOnInit(): void {
    const id = this.communityId();
    if (!id) {
      this.error.set('Comunidade não encontrada.');
      this.loading.set(false);
      return;
    }

    this.profileService.myProfile().subscribe({
      next: (profile) => this.myUserId.set(profile.userId),
      error: (error) => console.error('Failed to fetch my profile:', error),
    });

    this.communityService.detailCommunity(id).subscribe({
      next: (community) => this.community.set(community),
      error: (error) => console.error('Failed to fetch community:', error),
    });

    this.communityMemberService.listCommunityMembers(id, 0, MEMBERS_PAGE_SIZE).subscribe({
      next: (response) => {
        // A listagem devolve todo o histórico de vínculos; só quem está ativo hoje
        // deve aparecer como membro.
        this.members.set(response.content.filter((member) => member.status === 'ACTIVE'));
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Failed to fetch community members:', error);
        this.error.set(
          error?.status === 403
            ? 'Entre na comunidade para ver os membros dela.'
            : 'Não foi possível carregar os membros.',
        );
        this.loading.set(false);
      },
    });
  }

  onQueryChange(value: string): void {
    this.query.set(value);
  }

  isOwner(member: CommunityMemberResponse): boolean {
    return member.memberId === this.community()?.ownerId;
  }

  canRemove(member: CommunityMemberResponse): boolean {
    if (!this.iAmStaff() || this.isOwner(member) || member.memberId === this.myUserId()) {
      return false;
    }
    const role = this.myRole();
    if (role === 'OWNER') return true;
    if (role === 'ADMIN') return member.role !== 'ADMIN';
    if (role === 'MODERATOR') return member.role === 'MEMBER';
    return false;
  }

  openRemoveConfirm(member: CommunityMemberResponse): void {
    this.removeError.set(null);
    this.removeConfirmMember.set(member);
  }

  closeRemoveConfirm(): void {
    if (this.removing()) return;
    this.removeConfirmMember.set(null);
  }

  confirmRemove(): void {
    const communityId = this.communityId();
    const member = this.removeConfirmMember();
    if (!communityId || !member || this.removing()) return;

    this.removing.set(true);
    this.removeError.set(null);

    this.communityMemberService.removeMember(communityId, member.memberId).subscribe({
      next: () => {
        this.removing.set(false);
        this.removeConfirmMember.set(null);
        this.members.update((list) => list.filter((m) => m.id !== member.id));
      },
      error: (error) => {
        console.error('Failed to remove member:', error);
        this.removing.set(false);
        this.removeError.set(error?.error?.message ?? 'Não foi possível remover esse membro.');
      },
    });
  }
}
