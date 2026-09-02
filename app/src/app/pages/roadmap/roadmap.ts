import { Component, inject, signal } from '@angular/core';
import { AppSidebarComponent } from '../../components/layout/app-sidebar/app-sidebar';
import { DiscoverySidebarComponent } from '../../components/discovery/discovery-sidebar/discovery-sidebar';
import { ToastService } from '../../core/services/ui/toast.service';

export interface RoadmapItem {
  id: string;
  icon: string;
  category: string;
  categoryGroup: 'MATCHMAKING_TIMES' | 'SOCIAL_FEED' | 'TATICAS_GUIAS';
  title: string;
  description: string;
  status: 'PLANEJADO';
  estimatedQuarter: string;
}

@Component({
  selector: 'app-roadmap',
  imports: [AppSidebarComponent, DiscoverySidebarComponent],
  templateUrl: './roadmap.html',
  styleUrl: './roadmap.css',
})
export class RoadmapComponent {
  private toastService = inject(ToastService);

  readonly activeFilter = signal<'ALL' | 'MATCHMAKING_TIMES' | 'SOCIAL_FEED' | 'TATICAS_GUIAS'>('ALL');

  readonly roadmapItems: RoadmapItem[] = [
    {
      id: 'duo-finder',
      icon: 'person_search',
      category: 'Matchmaking',
      categoryGroup: 'MATCHMAKING_TIMES',
      title: 'Duo & Trio Finder por Filtros',
      description:
        'Encontre companheiros de equipe com base no seu nível de Premier/GC, função primária (Entry, AWP, Suporte, IGL), mapas favoritos e horários de jogo.',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q4 2026',
    },
    {
      id: 'scrims-lfg',
      icon: 'sports_esports',
      category: 'Times & Competitivo',
      categoryGroup: 'MATCHMAKING_TIMES',
      title: 'Agendamento de Treinos & Scrims de Time',
      description:
        'Capitães de times cadastrados podem marcar horários de treinos, desafiar outros times da plataforma e registrar os resultados das partidas.',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q4 2026',
    },
    {
      id: 'team-recruitment',
      icon: 'how_to_reg',
      category: 'Recrutamento',
      categoryGroup: 'MATCHMAKING_TIMES',
      title: 'Painel de Vagas Abertas & Recrutamento de Times',
      description:
        'Times anunciam vagas abertas por função (ex: "Procura-se AWP nível 15+ GC") e jogadores enviam candidaturas diretamente pela plataforma.',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q1 2027',
    },
    {
      id: 'tournaments-hub',
      icon: 'emoji_events',
      category: 'Torneios & Campeonatos',
      categoryGroup: 'MATCHMAKING_TIMES',
      title: 'Central de Torneios Oficiais & Comunitários (Estilo HLTV)',
      description:
        'Acompanhe campeonatos mundiais e regionais de CS2 com estatísticas de partidas, escalações de times, tabelas de classificação, premiações e crie seus próprios torneios no Kyofuse com chaves automáticas.',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q3 2027',
    },
    {
      id: 'tactics-hub',
      icon: 'menu_book',
      category: 'Táticas & Guias',
      categoryGroup: 'TATICAS_GUIAS',
      title: 'Biblioteca de Lineups & Táticas Comunitárias',
      description:
        'Guias práticos com fotos e instruções passo a passo de granadas (smokes, molotovs, flashes) criados pela comunidade, organizados por mapa e lado (TR/CT).',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q1 2027',
    },
    {
      id: 'reposts',
      icon: 'repeat',
      category: 'Feed & Social',
      categoryGroup: 'SOCIAL_FEED',
      title: 'Sistema de Reposts & Citações de Publicações',
      description:
        'Compartilhe jogadas e publicações de outros jogadores diretamente no seu perfil e no feed dos seus seguidores com apenas um clique.',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q1 2027',
    },
    {
      id: 'badges',
      icon: 'military_tech',
      category: 'Perfil Gamer',
      categoryGroup: 'SOCIAL_FEED',
      title: 'Insígnias de Perfil & Conquistas de Comunidade',
      description:
        'Medalhas cosméticas desbloqueadas por marcos na plataforma (ex: Membro Fundador, Capitão Ativo, Líder de Comunidade, Estrategista).',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q1 2027',
    },
    {
      id: 'polls',
      icon: 'poll',
      category: 'Engajamento',
      categoryGroup: 'SOCIAL_FEED',
      title: 'Enquetes Interativas no Feed e Comunidades',
      description:
        'Crie votações interativas para decidir estratégias de mapa, votar no MVP da semana ou debater atualizações do jogo com seus seguidores.',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q2 2027',
    },
    {
      id: 'media-uploads',
      icon: 'image',
      category: 'Mídia',
      categoryGroup: 'SOCIAL_FEED',
      title: 'Upload de Imagens e Screenshots nos Posts',
      description:
        'Compartilhe capturas de placar, táticas desenhadas e screenshots de jogadas marcantes nas suas publicações.',
      status: 'PLANEJADO',
      estimatedQuarter: 'Q2 2027',
    },
  ];

  setFilter(filter: 'ALL' | 'MATCHMAKING_TIMES' | 'SOCIAL_FEED' | 'TATICAS_GUIAS'): void {
    this.activeFilter.set(filter);
  }

  filteredItems(): RoadmapItem[] {
    const filter = this.activeFilter();
    if (filter === 'ALL') return this.roadmapItems;
    return this.roadmapItems.filter((item) => item.categoryGroup === filter);
  }

  notifyMe(item: RoadmapItem): void {
    this.toastService.success(
      `Perfeito! Você será notificado assim que "${item.title}" estiver disponível.`,
    );
  }
}
