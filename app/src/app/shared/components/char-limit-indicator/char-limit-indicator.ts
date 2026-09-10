import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-char-limit-indicator',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (visible()) {
      <div
        class="relative flex h-8 w-8 items-center justify-center shrink-0"
        [title]="isExceeded() ? ('Limite excedido em ' + (-remaining()) + ' caracteres') : (remaining() + ' caracteres restantes')"
      >
        <svg class="h-7 w-7 transform -rotate-90" viewBox="0 0 30 30">
          <!-- Background track circle -->
          <circle
            cx="15"
            cy="15"
            r="11.5"
            stroke="currentColor"
            stroke-width="2.5"
            class="text-outline-variant/30"
            fill="transparent"
          />
          <!-- Dynamic progress circle -->
          <circle
            cx="15"
            cy="15"
            r="11.5"
            stroke="currentColor"
            stroke-width="2.5"
            stroke-linecap="round"
            fill="transparent"
            [attr.stroke-dasharray]="circumference"
            [style.strokeDashoffset]="dashOffset()"
            class="transition-all duration-150"
            [ngClass]="circleColorClass()"
          />
        </svg>

        <!-- Center character count text when close to limit or exceeded -->
        @if (showCount()) {
          <span
            class="absolute inset-0 flex items-center justify-center font-mono font-bold leading-none select-none transition-colors duration-150"
            [ngClass]="countTextClass()"
          >
            {{ remaining() }}
          </span>
        }
      </div>
    }
  `,
})
export class CharLimitIndicatorComponent {
  readonly current = input.required<number>();
  readonly max = input<number>(500);
  readonly warningThreshold = input<number>(20);
  readonly showOnlyNearLimit = input<boolean>(false);

  readonly circumference = 2 * Math.PI * 11.5; // ~72.257

  readonly remaining = computed(() => this.max() - this.current());
  readonly isExceeded = computed(() => this.remaining() < 0);
  readonly isWarning = computed(() => this.remaining() >= 0 && this.remaining() <= this.warningThreshold());

  readonly visible = computed(() => {
    if (this.current() <= 0) return false;
    if (this.showOnlyNearLimit()) {
      return this.remaining() <= this.warningThreshold();
    }
    return true;
  });

  readonly showCount = computed(() => this.remaining() <= this.warningThreshold());

  readonly percentage = computed(() => {
    const ratio = Math.min(this.current(), this.max()) / this.max();
    return Math.max(0, Math.min(100, ratio * 100));
  });

  readonly dashOffset = computed(() => {
    if (this.isExceeded()) {
      return 0;
    }
    return this.circumference * (1 - this.percentage() / 100);
  });

  readonly circleColorClass = computed(() => {
    if (this.isExceeded()) {
      return 'text-error';
    }
    if (this.isWarning()) {
      return 'text-warning';
    }
    return 'text-primary';
  });

  readonly countTextClass = computed(() => {
    if (this.isExceeded()) {
      return Math.abs(this.remaining()) >= 100
        ? 'text-[9px] text-error font-extrabold'
        : 'text-[10px] text-error font-extrabold';
    }
    if (this.isWarning()) {
      return this.remaining() >= 100
        ? 'text-[9px] text-on-surface-variant font-bold'
        : 'text-[10px] text-on-surface-variant font-bold';
    }
    return 'text-[10px] text-on-surface-variant font-medium';
  });
}
