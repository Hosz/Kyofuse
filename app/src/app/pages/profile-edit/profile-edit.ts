import { Component, OnDestroy, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { ProfileService } from '../../core/services/profile/profile.service';
import { MediaService } from '../../core/services/media/media.service';
import { ToastService } from '../../core/services/ui/toast.service';
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
import {
  getCitiesForState,
  getCountryFlagUrl,
  getCountryOptions,
  getStatesForCountry,
  normalizeCountry,
  normalizeState,
} from '../../shared/models/location-options.model';
import { FALLBACK_AVATAR_URL } from '../../shared/utils/format.util';

type SectionId = 'identidade' | 'pessoal' | 'competitivo';

/** Espelha os tons de cada mapa como fundo de fallback visual às cartas quando
 * a imagem estiver carregando ou não estiver disponível. */
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

import { RoleIconComponent } from '../../components/shared/role-icon/role-icon';

@Component({
  selector: 'app-profile-edit',
  imports: [RouterLink, AppSidebarComponent, RoleIconComponent],
  templateUrl: './profile-edit.html',
  styleUrl: './profile-edit.css',
})
export class ProfileEditComponent implements OnDestroy {
  private profileService = inject(ProfileService);
  private mediaService = inject(MediaService);
  private toastService = inject(ToastService);
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
  uploadingAvatar = signal(false);
  uploadingBanner = signal(false);
  error = signal<string | null>(null);

  onAvatarFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.uploadingAvatar.set(true);
    this.mediaService.uploadImage(file).subscribe({
      next: (res) => {
        this.avatarUrl.set(res.url);
        this.uploadingAvatar.set(false);
        this.toastService.info('Foto carregada. Clique em Salvar para aplicar.');
      },
      error: (err) => {
        console.error('Failed to upload avatar:', err);
        this.toastService.error('Não foi possível enviar o avatar.');
        this.uploadingAvatar.set(false);
      },
    });
  }

  removeAvatar(): void {
    this.avatarUrl.set('');
  }

  onBannerFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    this.uploadingBanner.set(true);
    this.mediaService.uploadImage(file).subscribe({
      next: (res) => {
        this.bannerUrl.set(res.url);
        this.uploadingBanner.set(false);
        this.toastService.info('Banner carregado. Clique em Salvar para aplicar.');
      },
      error: (err) => {
        console.error('Failed to upload banner:', err);
        this.toastService.error('Não foi possível enviar o banner.');
        this.uploadingBanner.set(false);
      },
    });
  }

  removeBanner(): void {
    this.bannerUrl.set('');
  }

  nickname = signal('');
  bio = signal('');
  avatarUrl = signal('');
  bannerUrl = signal('');
  country = signal('');
  city = signal('');
  state = signal('');
  showCountryFlag = signal(true);

  readonly countryOptions = getCountryOptions();
  readonly flagUrl = computed(() => getCountryFlagUrl(this.country()));

  readonly stateOptions = computed(() => {
    const states = getStatesForCountry(this.country());
    const currentState = this.state();
    if (currentState && !states.some((s) => s.value === currentState)) {
      return [{ value: currentState, label: currentState }, ...states];
    }
    return states;
  });

  readonly cityOptions = computed(() => {
    const cities = getCitiesForState(this.country(), this.state());
    const currentCity = this.city();
    if (currentCity && !cities.includes(currentCity)) {
      return [currentCity, ...cities];
    }
    return cities;
  });

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
    if (this.mainRole() === role) {
      if (this.secondaryRole()) {
        this.mainRole.set(this.secondaryRole());
        this.secondaryRole.set('');
        this.toastService.info('O papel secundário assumiu o lugar de papel principal.');
      } else {
        this.mainRole.set('');
      }
    } else {
      this.mainRole.set(role);
      if (this.secondaryRole() === role) {
        this.secondaryRole.set('');
        this.toastService.info('O papel secundário foi desmarcado pois não pode ser igual ao principal.');
      }
    }
  }

  selectSecondaryRole(role: PlayerRole): void {
    if (this.secondaryRole() === role) {
      this.secondaryRole.set('');
    } else {
      if (this.mainRole() === role) {
        this.toastService.info('O papel secundário não pode ser igual ao papel principal.');
        return;
      }
      if (!this.mainRole()) {
        this.mainRole.set(role);
        this.toastService.info('Papel definido como principal.');
        return;
      }
      this.secondaryRole.set(role);
    }
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

  onCountryChange(value: string): void {
    this.country.set(value);
    const validStates = getStatesForCountry(value);
    if (!validStates.some((s) => s.value === this.state())) {
      this.state.set('');
      this.city.set('');
    }
  }

  onStateChange(value: string): void {
    this.state.set(value);
    const validCities = getCitiesForState(this.country(), value);
    if (!validCities.includes(this.city())) {
      this.city.set('');
    }
  }

  onCityChange(value: string): void {
    this.city.set(value);
  }

  submit(): void {
    if (this.saving()) return;
    if (!this.nickname().trim()) {
      this.error.set('Informe um nickname.');
      this.scrollToSection('pessoal');
      return;
    }

    if (this.mainRole() && this.secondaryRole() && this.mainRole() === this.secondaryRole()) {
      this.error.set('O papel principal e o papel secundário não podem ser iguais.');
      this.toastService.error('O papel principal e o papel secundário não podem ser iguais.');
      this.scrollToSection('competitivo');
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
      showCountryFlag: this.showCountryFlag(),
      mainRole: this.mainRole() ? (this.mainRole() as PlayerRole) : null,
      secondaryRole: this.secondaryRole() ? (this.secondaryRole() as PlayerRole) : null,
      premierRating: this.premierRating() ?? undefined,
      faceitLevel: this.faceitLevel() ?? undefined,
      gcRank: this.gcRank() ?? undefined,
      playstyle: this.playstyle() ? (this.playstyle() as Playstyle) : null,
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
    const normalizedCountry = normalizeCountry(p.country ?? '');
    this.country.set(normalizedCountry);
    const normalizedState = normalizeState(normalizedCountry, p.state ?? '');
    this.state.set(normalizedState);
    this.city.set(p.city ?? '');
    this.showCountryFlag.set(p.showCountryFlag ?? true);
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
