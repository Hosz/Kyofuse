import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { CreateTeamModalComponent } from './create-team-modal';
import { TeamService } from '../../../core/services/teams/team.service';
import { MediaService } from '../../../core/services/media/media.service';
import { TeamRequest, TeamResponse } from '../../../models/teams/team.model';

describe('CreateTeamModalComponent', () => {
  let component: CreateTeamModalComponent;
  let fixture: ComponentFixture<CreateTeamModalComponent>;
  let teamService: TeamService;

  const mockTeamResponse: TeamResponse = {
    id: 'team-1',
    ownerId: 'user-1',
    ownerName: 'owner',
    name: 'Test Team',
    slug: 'test-team',
    avatarUrl: null,
    bannerUrl: null,
    description: null,
    region: null,
    minPremierRating: null,
    maxPremierRating: null,
    minFaceitLevel: null,
    maxFaceitLevel: null,
    minGcRank: null,
    maxGcRank: null,
    status: 'ACTIVE',
    requiredRoles: [],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateTeamModalComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: TeamService,
          useValue: {
            createTeams: vi.fn().mockReturnValue(of(mockTeamResponse)),
          },
        },
        {
          provide: MediaService,
          useValue: {
            uploadImage: vi.fn().mockReturnValue(of({ url: 'https://example.com/img.png' })),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateTeamModalComponent);
    component = fixture.componentInstance;
    teamService = TestBed.inject(TeamService);
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve iniciar com createCommunity desligado (false)', () => {
    expect(component.createCommunity()).toBe(false);
  });

  it('deve alternar createCommunity ao chamar toggleCreateCommunity', () => {
    expect(component.createCommunity()).toBe(false);

    component.toggleCreateCommunity();
    expect(component.createCommunity()).toBe(true);

    component.toggleCreateCommunity();
    expect(component.createCommunity()).toBe(false);
  });

  it('deve enviar createCommunity true no payload quando a opção estiver ligada', () => {
    component.onNameChange('Test Team');
    component.toggleCreateCommunity();
    expect(component.createCommunity()).toBe(true);

    let createdEmitted: TeamResponse | undefined;
    component.created.subscribe((team) => (createdEmitted = team));

    component.submit();

    expect(teamService.createTeams).toHaveBeenCalledWith(
      expect.objectContaining({
        name: 'Test Team',
        slug: 'test-team',
        createCommunity: true,
      } as Partial<TeamRequest>),
    );
    expect(createdEmitted).toEqual(mockTeamResponse);
  });

  it('deve enviar createCommunity false no payload quando a opção estiver desligada', () => {
    component.onNameChange('Solo Team');
    expect(component.createCommunity()).toBe(false);

    component.submit();

    expect(teamService.createTeams).toHaveBeenCalledWith(
      expect.objectContaining({
        name: 'Solo Team',
        slug: 'solo-team',
        createCommunity: false,
      } as Partial<TeamRequest>),
    );
  });

  it('deve resetar createCommunity para false ao fechar/resetar', () => {
    component.onNameChange('Reset Team');
    component.toggleCreateCommunity();
    expect(component.createCommunity()).toBe(true);

    component.onClose();
    expect(component.createCommunity()).toBe(false);
  });
});
