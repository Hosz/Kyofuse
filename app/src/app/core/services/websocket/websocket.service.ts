import { effect, inject, Injectable, OnDestroy } from '@angular/core';
import { RxStomp, RxStompState } from '@stomp/rx-stomp';
import { Observable, map } from 'rxjs';
import { API_URL } from '../../../models/api-url.model';
import { AuthService } from '../auth/auth.service';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService implements OnDestroy {
  private readonly rxStomp = new RxStomp();
  private readonly authService = inject(AuthService);
  private isConfigured = false;

  constructor() {
    // Escuta reativamente as alterações de autenticação: conecta quando logado, desconecta no logout
    effect(() => {
      const isAuth = this.authService.isAuthenticated();
      if (isAuth) {
        this.connect();
      } else {
        this.disconnect();
      }
    });
  }

  public connect(): void {
    if (!this.isConfigured) {
      this.configure();
      this.isConfigured = true;
    }
    if (this.rxStomp.connectionState$.value === RxStompState.CLOSED) {
      this.rxStomp.activate();
    }
  }

  public disconnect(): void {
    if (this.rxStomp.connectionState$.value !== RxStompState.CLOSED) {
      this.rxStomp.deactivate();
    }
  }

  private configure(): void {
    const isHttps = window.location.protocol === 'https:';
    const wsProtocol = isHttps ? 'wss:' : 'ws:';
    const host = window.location.host;

    // Se API_URL for absoluta (ex: http://localhost:8080), substitui http por ws.
    // Caso contrário, usa o host da página atual (ex: ws://localhost:4200/ws proxied).
    let brokerURL: string;
    if (API_URL && API_URL.startsWith('http')) {
      brokerURL = API_URL.replace(/^http/, 'ws') + '/ws';
    } else {
      brokerURL = `${wsProtocol}//${host}/ws`;
    }

    this.rxStomp.configure({
      brokerURL,
      heartbeatIncoming: 0,
      heartbeatOutgoing: 20000,
      reconnectDelay: 5000,
      debug: (msg: string) => {
        if (!msg.includes('PING') && !msg.includes('PONG')) {
          console.debug('[WebSocket]', msg);
        }
      },
    });
  }

  /**
   * Assina um canal/tópico STOMP e emite o payload JSON parseado a cada nova mensagem.
   * Ex: watch<NotificationResponse>('/user/queue/notifications')
   * Ex: watch<MessageResponse>('/topic/conversations/' + conversationId)
   */
  public watch<T = unknown>(destination: string): Observable<T> {
    return this.rxStomp.watch({ destination }).pipe(
      map((message) => JSON.parse(message.body) as T),
    );
  }

  /**
   * Publica uma mensagem em um destino STOMP.
   */
  public publish(destination: string, body: unknown): void {
    this.rxStomp.publish({
      destination,
      body: typeof body === 'string' ? body : JSON.stringify(body),
    });
  }

  /**
   * Observa o estado da conexão (CLOSED, CONNECTING, OPEN, CLOSING).
   */
  public get connectionState$(): Observable<RxStompState> {
    return this.rxStomp.connectionState$;
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
