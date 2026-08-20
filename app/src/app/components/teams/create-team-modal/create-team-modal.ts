import { Component, inject, input, output, signal } from '@angular/core';
import { ModalComponent } from '../../shared/modal/modal';
import { TeamService } from '../../../core/services/teams/team.service';
import { TeamRequest, TeamResponse } from '../../../models/teams/team.model';
import { PLAYER_ROLE_OPTIONS, PlayerRole } from '../../../shared/models/profile-options.model';
import { toSlug } from '../../../shared/utils/format.util';

@Component({
  selector: 'app-create-team-modal',
  imports: [ModalComponent],
  templateUrl: './create-team-modal.html',
  styleUrl: './create-team-modal.css',
})
export class CreateTeamModalComponent {
  open = input(false);

  closed = output<void>();
  created = output<TeamResponse>();

  private teamService = inject(TeamService);

  readonly roleOptions = PLAYER_ROLE_OPTIONS;

  name = signal('');
  slug = signal('');
  avatarUrl = signal('');
  bannerUrl = signal('');
  description = signal('');
  region = signal('');
  minPremierRating = signal<number | null>(null);
  maxPremierRating = signal<number | null>(null);
  minFaceitLevel = signal<number | null>(null);
  maxFaceitLevel = signal<number | null>(null);
  minGcRank = signal<number | null>(null);
  maxGcRank = signal<number | null>(null);
  requiredRoles = signal<PlayerRole[]>([]);

  saving = signal(false);
  error = signal<string | null>(null);

  /** O slug é sugerido a partir do nome, mas para de acompanhar assim que o usuário
   * editar o campo manualmente — senão sobrescreveria a escolha dele a cada tecla. */
  private slugTouched = false;

  onNameChange(value: string): void {
    this.name.set(value);
    if (!this.slugTouched) this.slug.set(toSlug(value));
  }

  onSlugChange(value: string): void {
    this.slugTouched = true;
    this.slug.set(value);
  }

  toggleRequiredRole(role: PlayerRole): void {
    this.requiredRoles.update((roles) =>
      roles.includes(role) ? roles.filter((r) => r !== role) : [...roles, role],
    );
  }

  isRoleSelected(role: PlayerRole): boolean {
    return this.requiredRoles().includes(role);
  }

  onClose(): void {
    if (this.saving()) return;
    this.reset();
    this.closed.emit();
  }

  submit(): void {
    if (this.saving()) return;
    if (!this.name().trim() || !this.slug().trim()) {
      this.error.set('Nome e slug são obrigatórios.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    const request: TeamRequest = {
      name: this.name().trim(),
      slug: this.slug().trim(),
      avatarUrl: this.avatarUrl().trim() || undefined,
      bannerUrl: this.bannerUrl().trim() || undefined,
      description: this.description().trim(),
      region: this.region().trim(),
      minPremierRating: this.minPremierRating() ?? undefined,
      maxPremierRating: this.maxPremierRating() ?? undefined,
      minFaceitLevel: this.minFaceitLevel() ?? undefined,
      maxFaceitLevel: this.maxFaceitLevel() ?? undefined,
      minGcRank: this.minGcRank() ?? undefined,
      maxGcRank: this.maxGcRank() ?? undefined,
      requiredRoles: this.requiredRoles(),
    } as TeamRequest;

    this.teamService.createTeams(request).subscribe({
      next: (team) => {
        this.saving.set(false);
        this.reset();
        this.created.emit(team);
      },
      error: (error) => {
        console.error('Failed to create team:', error);
        this.saving.set(false);
        this.error.set(error?.error?.message ?? 'Não foi possível criar o time. Confira os dados informados.');
      },
    });
  }

  private reset(): void {
    this.name.set('');
    this.slug.set('');
    this.avatarUrl.set('');
    this.bannerUrl.set('');
    this.description.set('');
    this.region.set('');
    this.minPremierRating.set(null);
    this.maxPremierRating.set(null);
    this.minFaceitLevel.set(null);
    this.maxFaceitLevel.set(null);
    this.minGcRank.set(null);
    this.maxGcRank.set(null);
    this.requiredRoles.set([]);
    this.error.set(null);
    this.slugTouched = false;
  }
}
