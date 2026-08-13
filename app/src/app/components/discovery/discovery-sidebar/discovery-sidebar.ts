import { Component } from '@angular/core';
import { SearchBarComponent } from '../search-bar/search-bar';
import { SuggestedFollowsComponent } from '../suggested-follows/suggested-follows';
import { FeaturedTeamsComponent } from '../featured-teams/featured-teams';
import { FooterLinksComponent } from '../../layout/footer-links/footer-links';

@Component({
  selector: 'app-discovery-sidebar',
  imports: [SearchBarComponent, SuggestedFollowsComponent, FeaturedTeamsComponent, FooterLinksComponent],
  templateUrl: './discovery-sidebar.html',
  styleUrl: './discovery-sidebar.css',
})
export class DiscoverySidebarComponent {}
