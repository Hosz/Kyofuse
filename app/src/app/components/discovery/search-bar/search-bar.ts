import { Component, ElementRef, HostListener, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { TeamService } from '../../../core/services/teams/team.service';
import { TeamResponse } from '../../../models/teams/team.model';
import { ProfileService } from '../../../core/services/profile/profile.service';
import { gamerProfileResponse } from '../../../models/profile/gamer-profile.model';
import { FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';

const SEARCH_DEBOUNCE_MS = 300;
const PREVIEW_SIZE = 3;

@Component({
  selector: 'app-search-bar',
  imports: [],
  templateUrl: './search-bar.html',
  styleUrl: './search-bar.css',
})
export class SearchBarComponent {
  readonly fallbackAvatar = FALLBACK_AVATAR_URL;
  placeholder = input('Buscar perfis ou times...');

  private router = inject(Router);
  private teamService = inject(TeamService);
  private profileService = inject(ProfileService);
  private elementRef = inject(ElementRef<HTMLElement>);
  private debounceHandle?: ReturnType<typeof setTimeout>;

  query = signal('');
  dropdownOpen = signal(false);

  teams = signal<TeamResponse[]>([]);
  teamsTotal = signal(0);
  teamsLoading = signal(false);

  profiles = signal<gamerProfileResponse[]>([]);
  profilesTotal = signal(0);
  profilesLoading = signal(false);

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.dropdownOpen.set(false);
    }
  }

  onInput(value: string): void {
    this.query.set(value);
    this.dropdownOpen.set(value.trim().length > 0);

    if (this.debounceHandle) clearTimeout(this.debounceHandle);
    if (!value.trim()) {
      this.teams.set([]);
      this.teamsTotal.set(0);
      this.profiles.set([]);
      this.profilesTotal.set(0);
      return;
    }

    this.debounceHandle = setTimeout(() => this.search(), SEARCH_DEBOUNCE_MS);
  }

  onFocus(): void {
    if (this.query().trim().length > 0) this.dropdownOpen.set(true);
  }

  goToTeam(team: TeamResponse): void {
    this.dropdownOpen.set(false);
    this.router.navigate(['/times', team.id]);
  }

  goToProfile(profile: gamerProfileResponse): void {
    this.dropdownOpen.set(false);
    this.router.navigate(['/perfil', profile.username]);
  }

  goToResults(): void {
    const query = this.query().trim();
    if (!query) return;
    this.dropdownOpen.set(false);
    this.router.navigate(['/busca'], { queryParams: { q: query } });
  }

  private search(): void {
    const query = this.query().trim();
    if (!query) return;

    this.teamsLoading.set(true);
    this.profilesLoading.set(true);

    this.teamService.listingTeams({ name: query }).subscribe({
      next: (response) => {
        this.teams.set(response.content.slice(0, PREVIEW_SIZE));
        this.teamsTotal.set(response.totalElements);
        this.teamsLoading.set(false);
      },
      error: (error) => {
        console.error('Failed to search teams:', error);
        this.teamsLoading.set(false);
      },
    });

    this.profileService.listingProfiles({ username: query }).subscribe({
      next: (response) => {
        this.profiles.set(response.content.slice(0, PREVIEW_SIZE));
        this.profilesTotal.set(response.totalElements);
        this.profilesLoading.set(false);
      },
      error: (error) => {
        console.error('Failed to search profiles:', error);
        this.profilesLoading.set(false);
      },
    });
  }
}
