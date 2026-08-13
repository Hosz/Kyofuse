import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-password-strength-meter',
  imports: [],
  templateUrl: './password-strength-meter.html',
  styleUrl: './password-strength-meter.css',
})
export class PasswordStrengthMeterComponent {
  /** 0 a 4 — quantidade de critérios de força atendidos. */
  score = input(0);

  segments = computed(() => [1, 2, 3, 4]);

  label = computed(() => {
    switch (this.score()) {
      case 0:
        return 'Muito fraca';
      case 1:
        return 'Fraca';
      case 2:
        return 'Média';
      case 3:
        return 'Forte';
      default:
        return 'Muito forte';
    }
  });

  colorClass = computed(() => {
    if (this.score() <= 1) return 'bg-error';
    if (this.score() === 2) return 'bg-tertiary';
    return 'bg-primary-container';
  });

  labelColorClass = computed(() => {
    if (this.score() <= 1) return 'text-error';
    if (this.score() === 2) return 'text-tertiary';
    return 'text-primary-container';
  });
}
