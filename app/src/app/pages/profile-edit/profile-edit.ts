import { Component, OnDestroy, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { ProfileService } from '../../core/services/profile/profile.service';
import { gamerProfileResponse } from '../../models/profile/gamer-profile.model';
import { GamerProfileEditRequest } from '../../models/profile/gamer-profile-edit-request.model';
import {
  CS2_MAP_OPTIONS,
  Cs2Map,
  PLAYER_ROLE_OPTIONS,
  PLAYSTYLE_OPTIONS,
  PlayerRole,
  Playstyle,
} from '../../shared/models/profile-options.model';
import { FALLBACK_AVATAR_URL } from '../../shared/utils/format.util';

type SectionId = 'identidade' | 'pessoal' | 'competitivo';

/** Espelha os tons de cada mapa só pra dar variedade visual às cartas (não temos
 * capturas de tela reais dos mapas hospedadas pelo Kyofuse). */
const MAP_ACCENT_HUE: Record<Cs2Map, number> = {
  ANCIENT: 150,
  ANUBIS: 40,
  ALPINE: 200,
  CACHE: 10,
  DUST2: 30,
  INFERNO: 15,
  ITALY: 190,
  MIRAGE: 25,
  NUKE: 100,
  OFFICE: 220,
  OVERPASS: 130,
  STRONGHOLD: 0,
  TRAIN: 210,
  VERTIGO: 260,
  WARDEN: 20,
};

@Component({
  selector: 'app-profile-edit',
  imports: [RouterLink, AppSidebarComponent],
  templateUrl: './profile-edit.html',
  styleUrl: './profile-edit.css',
})
export class ProfileEditComponent implements OnDestroy {
  private profileService = inject(ProfileService);
  private router = inject(Router);
  private observer?: IntersectionObserver;

  readonly fallbackAvatar = FALLBACK_AVATAR_URL;
  readonly roleOptions = PLAYER_ROLE_OPTIONS;
  readonly playstyleOptions = PLAYSTYLE_OPTIONS;
  readonly mapOptions = CS2_MAP_OPTIONS;

  readonly sections: { id: SectionId; label: string; icon: string }[] = [
    { id: 'identidade', label: 'Identidade Visual', icon: 'palette' },
    { id: 'pessoal', label: 'Informações Pessoais', icon: 'badge' },
    { id: 'competitivo', label: 'Informações Competitivas', icon: 'military_tech' },
  ];

  activeSection = signal<SectionId>('identidade');

  loading = signal(true);
  saving = signal(false);
  error = signal<string | null>(null);

  nickname = signal('');
  bio = signal('');
  avatarUrl = signal('');
  bannerUrl = signal('');
  country = signal('');
  city = signal('');
  state = signal('');
  mainRole = signal<PlayerRole | ''>('');
  secondaryRole = signal<PlayerRole | ''>('');
  premierRating = signal<number | null>(null);
  faceitLevel = signal<number | null>(null);
  gcRank = signal<number | null>(null);
  playstyle = signal<Playstyle | ''>('');
  lookingForTeam = signal(false);
  lookingForDuo = signal(false);
  favoriteMaps = signal<Cs2Map[]>([]);

  ngOnInit(): void {
    this.profileService.myProfile().subscribe({
      next: (profile) => {
        this.seedFromProfile(profile);
        this.loading.set(false);
        this.setupSectionObserver();
      },
      error: (error) => {
        console.error('Failed to fetch my profile:', error);
        this.error.set('Não foi possível carregar seu perfil.');
        this.loading.set(false);
        this.setupSectionObserver();
      },
    });
  }

  /** As seções só existem no DOM depois que `loading` vira false (ficam atrás de um
   * @if), então o observer só pode ser montado depois disso — não em ngAfterViewInit,
   * que roda antes da resposta do perfil chegar. O setTimeout(0) espera o Angular
   * terminar de renderizar o novo ramo do template antes de consultar o DOM. */
  private setupSectionObserver(): void {
    setTimeout(() => {
      this.observer = new IntersectionObserver(
        (entries) => {
          for (const entry of entries) {
            if (entry.isIntersecting) {
              this.activeSection.set(entry.target.id as SectionId);
            }
          }
        },
        { rootMargin: '-15% 0px -70% 0px', threshold: 0 },
      );

      for (const section of this.sections) {
        const el = document.getElementById(section.id);
        if (el) this.observer.observe(el);
      }
    });
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  scrollToSection(id: SectionId): void {
    this.activeSection.set(id);
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  selectMainRole(role: PlayerRole): void {
    this.mainRole.set(this.mainRole() === role ? '' : role);
  }

  selectSecondaryRole(role: PlayerRole): void {
    this.secondaryRole.set(this.secondaryRole() === role ? '' : role);
  }

  selectPlaystyle(style: Playstyle): void {
    this.playstyle.set(this.playstyle() === style ? '' : style);
  }

  toggleMap(map: Cs2Map): void {
    this.favoriteMaps.update((maps) => {
      if (maps.includes(map)) return maps.filter((m) => m !== map);
      if (maps.length >= 3) return maps;
      return [...maps, map];
    });
  }

  /** Cor sólida de base — o textura de grade tática vem por cima via a classe `.tactical-grid`
   * (usar `background` inline sobrescreveria o `background-image` dela, por isso só a cor). */
  mapColor(map: Cs2Map): string {
    const hue = MAP_ACCENT_HUE[map];
    return `hsl(${hue} 32% 14%)`;
  }

  submit(): void {
    if (this.saving()) return;
    if (!this.nickname().trim()) {
      this.error.set('Informe um nickname.');
      this.scrollToSection('pessoal');
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    const request: GamerProfileEditRequest = {
      nickname: this.nickname().trim(),
      bio: this.bio().trim(),
      avatarUrl: this.avatarUrl().trim(),
      bannerUrl: this.bannerUrl().trim(),
      country: this.country().trim(),
      city: this.city().trim(),
      state: this.state().trim(),
      mainRole: this.mainRole() || undefined,
      secondaryRole: this.secondaryRole() || undefined,
      premierRating: this.premierRating() ?? undefined,
      faceitLevel: this.faceitLevel() ?? undefined,
      gcRank: this.gcRank() ?? undefined,
      playstyle: this.playstyle() || undefined,
      lookingForTeam: this.lookingForTeam(),
      lookingForDuo: this.lookingForDuo(),
      favoriteMaps: this.favoriteMaps(),
    };

    this.profileService.editProfile(request).subscribe({
      next: () => {
        this.saving.set(false);
        this.router.navigateByUrl('/perfil');
      },
      error: (error) => {
        console.error('Failed to edit profile:', error);
        this.saving.set(false);
        this.error.set('Não foi possível salvar as alterações. Tente novamente.');
      },
    });
  }

  private seedFromProfile(p: gamerProfileResponse): void {
    this.nickname.set(p.nickname ?? '');
    this.bio.set(p.bio ?? '');
    this.avatarUrl.set(p.avatarUrl ?? '');
    this.bannerUrl.set(p.bannerUrl ?? '');
    this.country.set(p.country ?? '');
    this.city.set(p.city ?? '');
    this.state.set(p.state ?? '');
    this.mainRole.set((p.mainRole as PlayerRole) || '');
    this.secondaryRole.set((p.secondaryRole as PlayerRole) || '');
    this.premierRating.set(p.premierRating || null);
    this.faceitLevel.set(p.faceitLevel ?? null);
    this.gcRank.set(p.gcRank ?? null);
    this.playstyle.set((p.playstyle as Playstyle) || '');
    this.lookingForTeam.set(!!p.lookingForTeam);
    this.lookingForDuo.set(!!p.lookingForDuo);
    this.favoriteMaps.set((p.favoriteMaps as Cs2Map[]) ?? []);
  }
}
