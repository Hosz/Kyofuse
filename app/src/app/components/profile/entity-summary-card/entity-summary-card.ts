import { Component, computed, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProfileEntity } from '../../../shared/models/profile.model';

@Component({
  selector: 'app-entity-summary-card',
  imports: [RouterLink],
  templateUrl: './entity-summary-card.html',
  styleUrl: './entity-summary-card.css',
})
export class EntitySummaryCardComponent {
  title = input.required<string>();
  items = input<ProfileEntity[]>([]);
  previewCount = input(3);

  viewAll = output<void>();

  previewItems = computed(() => this.items().slice(0, this.previewCount()));
  hasMore = computed(() => this.items().length > this.previewCount());
}
