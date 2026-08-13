import { PlayerRole } from '../../shared/models/profile-options.model';
import { TeamMemberStatus, TeamMemberType } from '../../shared/models/team-options.model';

export interface TeamMemberResponse {
    teamName: string;
    userId: string;
    userName: string;
    roleInTeam: PlayerRole | null;
    memberType: TeamMemberType;
    status: TeamMemberStatus;
    joinedAt: string;
    assignmentDueAt: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface TeamMemberEditRequest {
    roleInTeam?: PlayerRole;
    memberType?: TeamMemberType;
}
