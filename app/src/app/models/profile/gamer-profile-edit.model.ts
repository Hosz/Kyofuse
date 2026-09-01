export interface gamerProfileEdit {
    nickname: string;
    bio: string;
    avatarUrl: string;
    country: string;
    city: string;
    state: string;
    showCountryFlag?: boolean;
    mainRole: string;
    secondaryRole: string;
    premierRating: number;
    faceitLevel: number;
    gcRank: number;
    playstyle: string;
    lookingForTeam: boolean;
    lookingForDuo: boolean;
    favoriteMaps: string[];
}