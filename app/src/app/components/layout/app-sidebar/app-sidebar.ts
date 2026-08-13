import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SidebarNavComponent } from '../sidebar-nav/sidebar-nav';
import { UserProfileCardComponent } from '../user-profile-card/user-profile-card';
import { AuthService } from '../../../core/services/auth/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, SidebarNavComponent, UserProfileCardComponent],
  templateUrl: './app-sidebar.html',
  styleUrl: './app-sidebar.css',
})
export class AppSidebarComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.authService.clearToken();
        this.router.navigateByUrl('');
      },
      error: (error) => {
        console.error('Logout failed:', error);
        this.authService.clearToken();
        this.router.navigateByUrl('');
      },
    });
  }
}
