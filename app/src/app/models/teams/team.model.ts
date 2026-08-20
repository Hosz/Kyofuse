import { PlayerRole } from '../../shared/models/profile-options.model';
import { TeamStatus } from '../../shared/models/team-options.model';

export interface TeamResponse {
    id: string;
    ownerId: string;
    ownerName: string;
    name: string;
    slug: string;
    avatarUrl: string | null;
    bannerUrl: string | null;
    description: string | null;
    region: string | null;
    minPremierRating: number | null;
    maxPremierRating: number | null;
    minFaceitLevel: number | null;
    maxFaceitLevel: number | null;
    minGcRank: number | null;
    maxGcRank: number | null;
    status: TeamStatus;
    requiredRoles: PlayerRole[];
    createdAt: string;
    updatedAt: string;
}

export interface TeamRequest {
    name: string;
    slug: string;
    avatarUrl?: string;
    bannerUrl?: string;
    description: string;
    region: string;
    minPremierRating: number;
    maxPremierRating: number;
    minFaceitLevel: number;
    maxFaceitLevel: number;
    minGcRank: number;
    maxGcRank: number;
    requiredRoles: PlayerRole[];
}

export interface TeamFilter {
    name?: string;
    slug?: string;
    status?: TeamStatus;
    region?: string;
    requiredRoles?: PlayerRole[];
    minPremierRating?: number;
    maxPremierRating?: number;
    minFaceitLevel?: number;
    maxFaceitLevel?: number;
    minGcRank?: number;
    maxGcRank?: number;
}

export interface UpdateTeamRequest {
    name?: string;
    description?: string;
    avatarUrl?: string;
    bannerUrl?: string;
    region?: string;
    minPremierRating?: number;
    maxPremierRating?: number;
    minFaceitLevel?: number;
    maxFaceitLevel?: number;
    minGcRank?: number;
    maxGcRank?: number;
    status?: TeamStatus;
}

export interface UpdateTeamRequiredRolesRequest {
    requiredRoles: PlayerRole[];
}
