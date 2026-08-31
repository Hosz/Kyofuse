export interface gamerProfileResponse {
    id: string;
    userId: string;
    username: string;
    nickname: string;
    bio: string;
    avatarUrl: string;
    bannerUrl: string;
    country: string;
    city: string;
    state: string;
    mainRole: string;
    secondaryRole: string;
    premierRating: number;
    faceitLevel: number;
    gcRank: number;
    playstyle: string;
    lookingForTeam: boolean;
    lookingForDuo: boolean;
    setupStatus: string;
    favoriteMaps: string[];
    createdAt?: string;
    updatedAt?: string;
}

export interface gamerProfileCard {
    nickname: string;
    mainRole: string;
    avatarUrl: string;
    username: string;
    country?: string;
    city?: string;
    state?: string;
}