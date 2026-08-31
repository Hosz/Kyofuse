import { Component, computed, input } from '@angular/core';
import { PlayerRole } from '../../../shared/models/profile-options.model';

const ROLE_ASSETS: Record<string, string> = {
  AWPER: '/assets/icons-role/awper.png',
  IGL: '/assets/icons-role/igl.png',
  LURKER: '/assets/icons-role/lurker.png',
  RIFLER: '/assets/icons-role/rifler.png',
  SUPPORT: '/assets/icons-role/support.png',
};

const ROLE_ACCENT_COLORS: Record<string, string> = {
  AWPER: '#38bdf8', // Sky blue (Sniper precision)
  IGL: '#c084fc', // Purple (Tactical leader)
  SUPPORT: '#34d399', // Emerald (Utility grenades)
  LURKER: '#f87171', // Red / Crimson (Stealth ninja)
  RIFLER: '#fb923c', // Orange (AK-47 assault)
  ENTRY_FRAGGER: '#facc15', // Yellow (Aggressive breach)
  FLEX: '#22d3ee', // Cyan (Versatility)
};

@Component({
  selector: 'app-role-icon',
  imports: [],
  templateUrl: './role-icon.html',
  styleUrl: './role-icon.css',
})
export class RoleIconComponent {
  role = input<PlayerRole | string | null | undefined>(null);
  size = input<'xs' | 'sm' | 'md' | 'lg' | 'custom'>('sm');
  colored = input<boolean>(false);
  color = input<string | null>(null);

  normalizedRole = computed<string>(() => {
    const raw = this.role();
    if (!raw) return '';
    const upper = raw.toUpperCase().trim();
    if (upper === 'AWPER' || upper === 'AWP' || upper === 'SNIPER') return 'AWPER';
    if (upper === 'IGL' || upper === 'CAPITAO' || upper === 'CAPITÃO' || upper === 'LEADER') return 'IGL';
    if (upper === 'SUPPORT' || upper === 'SUPORTE') return 'SUPPORT';
    if (upper === 'LURKER' || upper === 'LURK') return 'LURKER';
    if (upper === 'RIFLER' || upper === 'RIFLE' || upper === 'RIFLES') return 'RIFLER';
    if (upper === 'FLEX' || upper === 'CORINGA') return 'FLEX';
    if (upper === 'ENTRY_FRAGGER' || upper === 'ENTRY' || upper === 'ENTRY FRAGGER') return 'ENTRY_FRAGGER';
    return upper;
  });

  assetSrc = computed<string | null>(() => {
    return ROLE_ASSETS[this.normalizedRole()] ?? null;
  });

  maskUrl = computed<string | null>(() => {
    const src = this.assetSrc();
    return src ? `url(${src})` : null;
  });

  roleAccentColor = computed<string | null>(() => {
    return ROLE_ACCENT_COLORS[this.normalizedRole()] ?? null;
  });

  resolvedColor = computed<string | null>(() => {
    if (this.color()) return this.color();
    if (this.colored()) return this.roleAccentColor();
    return null;
  });

  sizeClass = computed<string>(() => {
    if (this.size() === 'custom') return 'w-full h-full';

    const r = this.normalizedRole();
    const isWideRifle = r === 'AWPER' || r === 'RIFLER';
    const isMediumWide = r === 'SUPPORT';

    switch (this.size()) {
      case 'xs':
        if (isWideRifle) return 'w-7 h-3.5';
        if (isMediumWide) return 'w-5 h-3.5';
        return 'w-4 h-4';
      case 'sm':
        if (isWideRifle) return 'w-9 h-4.5';
        if (isMediumWide) return 'w-6.5 h-4.5';
        return 'w-5 h-5';
      case 'md':
        if (isWideRifle) return 'w-12 h-6';
        if (isMediumWide) return 'w-8.5 h-6';
        return 'w-6 h-6';
      case 'lg':
        if (isWideRifle) return 'w-16 h-8';
        if (isMediumWide) return 'w-11 h-8';
        return 'w-8 h-8';
      default:
        return 'w-full h-full';
    }
  });
}
