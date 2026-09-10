import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SidebarNavComponent } from '../sidebar-nav/sidebar-nav';
import { UserProfileCardComponent } from '../user-profile-card/user-profile-card';
import { AuthService } from '../../../core/services/auth/auth.service';
import { AccountManagerService } from '../../../core/services/auth/account-manager.service';
import { Router } from '@angular/router';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-sidebar',
  imports: [
    RouterLink,
    SidebarNavComponent,
    UserProfileCardComponent,
    TranslatePipe,
  ],
  templateUrl: './app-sidebar.html',
  styleUrl: './app-sidebar.css',
})
export class AppSidebarComponent {
  private readonly authService = inject(AuthService);
  private readonly accountManager = inject(AccountManagerService);
  private readonly router = inject(Router);

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.handleLogoutRedirect();
      },
      error: (error) => {
        console.error('Logout failed:', error);
        this.authService.clearSession();
        this.handleLogoutRedirect();
      },
    });
  }

  private handleLogoutRedirect(): void {
    const hasOtherAccounts = this.accountManager.savedAccounts().length > 0;
    if (hasOtherAccounts) {
      this.router.navigate(['/auth'], { queryParams: { accountsPrompt: 'true' } });
    } else {
      this.router.navigateByUrl('');
    }
  }
}
