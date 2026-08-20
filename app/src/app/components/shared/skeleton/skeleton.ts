import { Component, input } from '@angular/core';

/** Placeholder animado no formato do conteúdo que está chegando. */
@Component({
  selector: 'app-skeleton',
  imports: [],
  templateUrl: './skeleton.html',
  styleUrl: './skeleton.css',
})
export class SkeletonComponent {
  variant = input<'post' | 'row' | 'conversation'>('post');
  /** Quantas repetições mostrar. */
  count = input(3);

  get items(): number[] {
    return Array.from({ length: this.count() }, (_, index) => index);
  }
}
