import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/**
 * Quando o perfil é privado (e o visitante não segue/é amigo) ou o visitante está
 * bloqueado, o backend recusa o pedido inteiro (403) sem devolver nenhum dado —
 * nem nome nem avatar. Por isso essa tela é genérica, sem informações do dono do perfil.
 */
@Component({
  selector: 'app-profile-locked',
  imports: [RouterLink],
  templateUrl: './profile-locked.html',
  styleUrl: './profile-locked.css',
})
export class ProfileLockedComponent {}
