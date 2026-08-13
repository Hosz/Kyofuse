import { Component, input, output } from '@angular/core';
import { FeedTab } from '../../../shared/models/social.model';

@Component({
  selector: 'app-feed-tabs',
  imports: [],
  templateUrl: './feed-tabs.html',
  styleUrl: './feed-tabs.css',
})
export class FeedTabsComponent {
  tabs = input<FeedTab[]>([
    { label: 'Para Você', active: true },
    { label: 'Seguindo' },
    { label: 'Comunidades' },
  ]);
 
  tabSelected = output<FeedTab>();
 
  onSelect(tab: FeedTab): void {
    this.tabSelected.emit(tab);
  }
}
