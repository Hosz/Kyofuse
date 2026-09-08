import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UserAccountService } from '../../../../core/services/account/user-account.service';
import { AuthService } from '../../../../core/services/auth/auth.service';
import { AccountManagerService } from '../../../../core/services/auth/account-manager.service';
import { UserAccountResponse } from '../../../../models/account/user-account.model';
import { SkeletonComponent } from '../../../../components/shared/skeleton/skeleton';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-account-management',
  imports: [FormsModule, SkeletonComponent, TranslatePipe],
  templateUrl: './account-management.html',
  styleUrl: './account-management.css',
})
export class AccountManagementSectionComponent implements OnInit {
  private readonly accountService = inject(UserAccountService);
  private readonly authService = inject(AuthService);
  private readonly accountManager = inject(AccountManagerService);
  private readonly router = inject(Router);

  loading = signal(true);
  account = signal<UserAccountResponse | null>(null);
  loadError = signal<string | null>(null);

  // Desativação de Conta
  deactivateModalOpen = signal(false);
  deactivatePassword = signal('');
  deactivateReason = signal('');
  deactivating = signal(false);
  deactivateError = signal<string | null>(null);

  // Exclusão de Conta
  deleteModalOpen = signal(false);
  deletePassword = signal('');
  deleteReason = signal('');
  deleting = signal(false);
  deleteError = signal<string | null>(null);

  ngOnInit(): void {
    this.loadAccount();
  }

  loadAccount(): void {
    this.loading.set(true);
    this.loadError.set(null);

    this.accountService.getAccount().subscribe({
      next: (acc) => {
        this.account.set(acc);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.loadError.set(err?.error?.message ?? 'Falha ao carregar informações da conta.');
      },
    });
  }

  openDeactivateModal(): void {
    this.deactivatePassword.set('');
    this.deactivateReason.set('');
    this.deactivateError.set(null);
    this.deactivateModalOpen.set(true);
  }

  closeDeactivateModal(): void {
    this.deactivateModalOpen.set(false);
    this.deactivatePassword.set('');
    this.deactivateReason.set('');
    this.deactivateError.set(null);
  }

  confirmDeactivate(): void {
    const acc = this.account();
    if (!acc) return;

    if (acc.hasPassword && !this.deactivatePassword()) {
      this.deactivateError.set('Informe sua senha para confirmar a desativação.');
      return;
    }

    this.deactivating.set(true);
    this.deactivateError.set(null);

    this.accountService.deactivateAccount({
      password: this.deactivatePassword() || undefined,
      reason: this.deactivateReason() || undefined,
    }).subscribe({
      next: () => {
        this.deactivating.set(false);
        this.deactivateModalOpen.set(false);
        this.authService.clearSession();
        if (acc.id) {
          this.accountManager.removeLocalAccount(acc.id);
        }
        this.router.navigateByUrl('/login');
      },
      error: (err) => {
        this.deactivating.set(false);
        this.deactivateError.set(err?.error?.message ?? 'Falha ao desativar conta.');
      },
    });
  }

  openDeleteModal(): void {
    this.deletePassword.set('');
    this.deleteReason.set('');
    this.deleteError.set(null);
    this.deleteModalOpen.set(true);
  }

  closeDeleteModal(): void {
    this.deleteModalOpen.set(false);
    this.deletePassword.set('');
    this.deleteReason.set('');
    this.deleteError.set(null);
  }

  confirmDelete(): void {
    const acc = this.account();
    if (!acc) return;

    if (acc.hasPassword && !this.deletePassword()) {
      this.deleteError.set('Informe sua senha para confirmar a solicitação de exclusão.');
      return;
    }

    this.deleting.set(true);
    this.deleteError.set(null);

    this.accountService.scheduleDeletion({
      password: this.deletePassword() || undefined,
      reason: this.deleteReason() || undefined,
    }).subscribe({
      next: () => {
        this.deleting.set(false);
        this.deleteModalOpen.set(false);
        this.authService.clearSession();
        if (acc.id) {
          this.accountManager.removeLocalAccount(acc.id);
        }
        this.router.navigateByUrl('/login');
      },
      error: (err) => {
        this.deleting.set(false);
        this.deleteError.set(err?.error?.message ?? 'Falha ao agendar exclusão da conta.');
      },
    });
  }
}
