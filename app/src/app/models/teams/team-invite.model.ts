import { PlayerRole } from '../../shared/models/profile-options.model';
import { TeamMemberType } from '../../shared/models/team-options.model';

export type TeamInviteStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'CANCELED';

export interface TeamInviteResponse {
    id: string;
    teamId: string;
    teamName: string;
    senderId: string;
    senderName: string;
    receiverId: string;
    receiverName: string;
    status: TeamInviteStatus;
    message: string | null;
    proposedMemberType: TeamMemberType | null;
    proposedRoleInTeam: PlayerRole | null;
    createdAt: string;
}

export interface TeamInviteRequest {
    message?: string;
    proposedMemberType?: TeamMemberType;
    proposedRoleInTeam?: PlayerRole;
}

export interface TeamInviteCancelRequest {
    cancellationReason: string;
}
