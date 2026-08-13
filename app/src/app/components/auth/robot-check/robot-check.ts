import { Component, DestroyRef, inject, output, signal } from '@angular/core';

@Component({
  selector: 'app-robot-check',
  imports: [],
  templateUrl: './robot-check.html',
  styleUrl: './robot-check.css',
})
export class RobotCheckComponent {
  private readonly destroyRef = inject(DestroyRef);

  verifiedChange = output<boolean>();

  verified = signal(false);
  checking = signal(false);

  onToggle(): void {
    if (this.verified()) {
      this.verified.set(false);
      this.verifiedChange.emit(false);
      return;
    }

    this.checking.set(true);
    const timer = setTimeout(() => {
      this.checking.set(false);
      this.verified.set(true);
      this.verifiedChange.emit(true);
    }, 500);

    this.destroyRef.onDestroy(() => clearTimeout(timer));
  }
}
