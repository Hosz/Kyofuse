import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UserAccountService } from '../../../../core/services/account/user-account.service';
import { UserAccountResponse } from '../../../../models/account/user-account.model';
import { SkeletonComponent } from '../../../../components/shared/skeleton/skeleton';

@Component({
  selector: 'app-password-settings',
  imports: [FormsModule, SkeletonComponent],
  templateUrl: './password-settings.html',
  styleUrl: './password-settings.css',
})
export class PasswordSettingsSectionComponent implements OnInit {
  private readonly accountService = inject(UserAccountService);

  loading = signal(true);
  account = signal<UserAccountResponse | null>(null);
  loadError = signal<string | null>(null);

  currentPassword = signal('');
  newPassword = signal('');
  confirmPassword = signal('');

  showCurrentPassword = signal(false);
  showNewPassword = signal(false);
  showConfirmPassword = signal(false);

  saving = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal(false);

  ngOnInit(): void {
    this.accountService.getAccount().subscribe({
      next: (acc) => {
        this.account.set(acc);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.loadError.set(err?.error?.message ?? 'Falha ao carregar status da conta.');
      },
    });
  }

  onSubmit(): void {
    const acc = this.account();
    const current = this.currentPassword().trim();
    const next = this.newPassword().trim();
    const confirm = this.confirmPassword().trim();

    this.errorMessage.set(null);
    this.successMessage.set(false);

    if (acc?.hasPassword && !current) {
      this.errorMessage.set('Informe sua senha atual.');
      return;
    }

    if (!next) {
      this.errorMessage.set('Informe a nova senha.');
      return;
    }

    if (next.length < 8) {
      this.errorMessage.set('A nova senha deve ter no mínimo 8 caracteres.');
      return;
    }

    if (next !== confirm) {
      this.errorMessage.set('A confirmação da senha não coincide com a nova senha.');
      return;
    }

    this.saving.set(true);

    this.accountService.changePassword({
      currentPassword: current || undefined,
      newPassword: next,
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.successMessage.set(true);
        this.currentPassword.set('');
        this.newPassword.set('');
        this.confirmPassword.set('');
        setTimeout(() => this.successMessage.set(false), 4000);
      },
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Falha ao alterar senha. Verifique os dados informados.');
      },
    });
  }
}
