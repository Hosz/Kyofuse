import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { computed, signal } from '@angular/core';
import { of, Subject } from 'rxjs';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { ChatComponent } from './chat';
import { ConversationService } from '../../core/services/chat/conversation.service';
import { MessageService } from '../../core/services/chat/message.service';
import { PresenceService } from '../../core/services/presence/presence.service';
import { ProfileService } from '../../core/services/profile/profile.service';
import { ToastService } from '../../core/services/ui/toast.service';
import { NotificationService } from '../../core/services/notifications/notification.service';
import { AuthService } from '../../core/services/auth/auth.service';
import { PageResponse } from '../../models/page-response.model';
import { ConversationResponse, MessageResponse, MessageStatusEvent } from '../../models/chat/chat.model';
import { gamerProfileResponse } from '../../models/profile/gamer-profile.model';

describe('ChatComponent', () => {
  let fixture: ComponentFixture<ChatComponent>;
  let component: ChatComponent;

  let conversationServiceMock: any;
  let messageServiceMock: any;
  let profileServiceMock: any;
  let toastServiceMock: any;
  let notificationServiceMock: any;
  let authServiceMock: any;

  const mockProfile: gamerProfileResponse = {
    id: 'p1',
    userId: 'user-1',
    username: 'testuser',
    nickname: 'Test User',
    bio: '',
    avatarUrl: '',
    bannerUrl: '',
    country: 'BR',
    city: 'SP',
    state: 'SP',
    mainRole: '',
    secondaryRole: '',
    premierRating: 0,
    faceitLevel: 0,
    gcRank: 0,
    playstyle: '',
    lookingForTeam: false,
    lookingForDuo: false,
    setupStatus: 'COMPLETED',
    favoriteMaps: [],
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  };

  const mockConversationResponse: ConversationResponse = {
    id: 'conv-1',
    type: 'DIRECT',
    name: null,
    avatarUrl: null,
    createdById: 'user-2',
    createdByUsername: 'otheruser',
    communityId: null,
    communityName: null,
    communityAvatarUrl: null,
    directUserOneId: 'user-1',
    directUserOneUsername: 'testuser',
    directUserOneNickname: 'Test User',
    directUserOneAvatarUrl: null,
    directUserTwoId: 'user-2',
    directUserTwoUsername: 'otheruser',
    directUserTwoNickname: 'Other User',
    directUserTwoAvatarUrl: null,
    directMessageStatus: 'ACCEPTED',
    lastMessageContent: 'Olá',
    lastMessageSenderUsername: 'otheruser',
    lastMessageSenderNickname: 'Other User',
    lastMessageCreatedAt: '2026-08-01T12:00:00Z',
    lastMessageHasMedia: false,
    lastMessageMediaType: null,
    unreadCount: 1,
    revokedById: null,
    createdAt: '2026-08-01T10:00:00Z',
    updatedAt: '2026-08-01T12:00:00Z',
  };

  const mockPageEmpty: PageResponse<ConversationResponse> = {
    content: [],
    number: 0,
    size: 50,
    totalElements: 0,
    totalPages: 0,
    last: true,
  };

  const mockPageWithConv: PageResponse<ConversationResponse> = {
    content: [mockConversationResponse],
    number: 0,
    size: 50,
    totalElements: 1,
    totalPages: 1,
    last: true,
  };

  const mockMessageResponse: MessageResponse = {
    id: 'msg-1',
    conversationId: 'conv-1',
    senderId: 'user-2',
    senderUsername: 'otheruser',
    senderNickname: 'Other User',
    senderAvatarUrl: null,
    content: 'Olá',
    createdAt: '2026-08-01T12:00:00Z',
    media: [],
    status: 'SENT',
  };

  const mockOlderMessageResponse: MessageResponse = {
    id: 'msg-0',
    conversationId: 'conv-1',
    senderId: 'user-2',
    senderUsername: 'otheruser',
    senderNickname: 'Other User',
    senderAvatarUrl: null,
    content: 'Mensagem mais antiga',
    createdAt: '2026-08-01T11:00:00Z',
    media: [],
    status: 'SENT',
  };

  const mockMessagesPage0: PageResponse<MessageResponse> = {
    content: [mockMessageResponse],
    number: 0,
    size: 50,
    totalElements: 2,
    totalPages: 2,
    last: false,
  };

  const mockMessagesPage1: PageResponse<MessageResponse> = {
    content: [mockOlderMessageResponse],
    number: 1,
    size: 50,
    totalElements: 2,
    totalPages: 2,
    last: true,
  };

  const messageStatus$ = new Subject<MessageStatusEvent>();
  const newMessage$ = new Subject<MessageResponse>();
  const unreadCountSignal = signal(0);

  const typingSubjects = new Map<string, Subject<any>>();

  beforeEach(async () => {
    unreadCountSignal.set(0);
    typingSubjects.clear();

    conversationServiceMock = {
      unreadCount: unreadCountSignal,
      hasUnread: computed(() => unreadCountSignal() > 0),
      markAsRead: vi.fn(),
      decrementUnread: vi.fn(),
      refreshUnreadStatus: vi.fn(),
      refreshUnreadCount: vi.fn(),
      newMessage$,
      listDirectConversations: vi.fn().mockReturnValue(of(mockPageWithConv)),
      listGroupConversations: vi.fn().mockReturnValue(of(mockPageEmpty)),
      listCommunityConversations: vi.fn().mockReturnValue(of(mockPageEmpty)),
      getConversationDetails: vi.fn().mockReturnValue(of(mockConversationResponse)),
    };

    messageServiceMock = {
      messageStatus$,
      getMessages: vi.fn().mockImplementation((_convId, page) => {
        return of(page === 0 ? mockMessagesPage0 : mockMessagesPage1);
      }),
      watchConversation: vi.fn().mockReturnValue(of(mockMessageResponse)),
      watchTyping: vi.fn().mockImplementation((id: string) => {
        if (!typingSubjects.has(id)) {
          typingSubjects.set(id, new Subject<any>());
        }
        return typingSubjects.get(id)!;
      }),
      sendTyping: vi.fn(),
      markAsRead: vi.fn().mockReturnValue(of(undefined)),
      markAsDelivered: vi.fn().mockReturnValue(of(undefined)),
      sendMessage: vi.fn().mockReturnValue(of({
        id: 'msg-sent',
        conversationId: 'conv-1',
        senderId: 'user-1',
        senderUsername: 'testuser',
        senderNickname: 'Test User',
        senderAvatarUrl: null,
        content: 'Minha resposta',
        createdAt: '2026-08-01T12:05:00Z',
        media: [],
        status: 'SENT',
      })),
    };

    profileServiceMock = {
      myProfile: vi.fn().mockReturnValue(of(mockProfile)),
    };

    toastServiceMock = {
      success: vi.fn(),
      error: vi.fn(),
      info: vi.fn(),
    };

    notificationServiceMock = {
      unreadCount: signal(0),
      hasUnread: computed(() => false),
      refreshUnreadStatus: vi.fn(),
      refreshUnreadCount: vi.fn(),
      newNotification$: new Subject(),
    };

    authServiceMock = {
      isAuthenticated: vi.fn().mockReturnValue(true),
      currentUser: signal({ id: 'user-1', username: 'testuser' }),
      logout: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [ChatComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ConversationService, useValue: conversationServiceMock },
        { provide: MessageService, useValue: messageServiceMock },
        { provide: PresenceService, useValue: { presenceMap: signal(new Map()), fetchBatchPresence: vi.fn() } },
        { provide: ProfileService, useValue: profileServiceMock },
        { provide: ToastService, useValue: toastServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();
  });

  it('should initialize and load conversations without crashing', () => {
    fixture = TestBed.createComponent(ChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(component.allConversations().length).toBe(1);
    expect(component.allConversations()[0].id).toBe('conv-1');
  });

  it('should select a conversation, load its messages and set unread divider', async () => {
    fixture = TestBed.createComponent(ChatComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('chatId', 'conv-1');
    fixture.detectChanges();

    expect(messageServiceMock.getMessages).toHaveBeenCalledWith('conv-1', 0, 50);
    expect(messageServiceMock.watchConversation).toHaveBeenCalledWith('conv-1');
    expect(messageServiceMock.getMessages).toHaveBeenCalledTimes(1);

    const selected = component.selectedConversation();
    expect(selected).toBeTruthy();
    expect(selected?.id).toBe('conv-1');
    expect(selected?.messages.length).toBe(1);
    expect(selected?.hasMoreMessages).toBe(true);
    expect(selected?.unreadDividerMessageId).toBe('msg-1');
  });

  it('should clear unread divider when sending a message', async () => {
    fixture = TestBed.createComponent(ChatComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('chatId', 'conv-1');
    fixture.detectChanges();

    expect(component.selectedConversation()?.unreadDividerMessageId).toBe('msg-1');

    component.onSendMessage({ content: 'Minha resposta' });
    fixture.detectChanges();

    expect(component.selectedConversation()?.unreadDividerMessageId).toBeNull();
  });

  it('should load older messages and prepend them to the conversation', async () => {
    fixture = TestBed.createComponent(ChatComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('chatId', 'conv-1');
    fixture.detectChanges();

    expect(component.selectedConversation()?.messages.length).toBe(1);

    component.onLoadOlderMessages();
    fixture.detectChanges();

    expect(messageServiceMock.getMessages).toHaveBeenCalledWith('conv-1', 1, 50);

    const selected = component.selectedConversation();
    expect(selected?.messages.length).toBe(2);
    expect(selected?.messages[0].id).toBe('msg-0');
    expect(selected?.messages[1].id).toBe('msg-1');
    expect(selected?.hasMoreMessages).toBe(false);
  });

  it('should update typing status in direct conversation to "Está digitando..."', async () => {
    fixture = TestBed.createComponent(ChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const typingSub = typingSubjects.get('conv-1');
    expect(typingSub).toBeTruthy();

    // Outro usuário começa a digitar
    typingSub!.next({
      conversationId: 'conv-1',
      userId: 'user-2',
      username: 'otheruser',
      nickname: 'Other User',
      isTyping: true,
    });
    fixture.detectChanges();

    const conv = component.allConversations().find((c) => c.id === 'conv-1');
    expect(conv?.isTyping).toBe(true);
    expect(conv?.typingText).toBe('Está digitando...');

    // Outro usuário para de digitar
    typingSub!.next({
      conversationId: 'conv-1',
      userId: 'user-2',
      username: 'otheruser',
      nickname: 'Other User',
      isTyping: false,
    });
    fixture.detectChanges();

    const convStopped = component.allConversations().find((c) => c.id === 'conv-1');
    expect(convStopped?.isTyping).toBe(false);
    expect(convStopped?.typingText).toBeUndefined();
  });

  it('should update typing status in group with correct user names and count', async () => {
    const mockGroupResponse: ConversationResponse = {
      ...mockConversationResponse,
      id: 'conv-group',
      type: 'GROUP',
      name: 'Amigos do CS',
    };

    conversationServiceMock.listGroupConversations.mockReturnValue(
      of({
        content: [mockGroupResponse],
        number: 0,
        size: 50,
        totalElements: 1,
        totalPages: 1,
        last: true,
      }),
    );

    fixture = TestBed.createComponent(ChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const groupTypingSub = typingSubjects.get('conv-group');
    expect(groupTypingSub).toBeTruthy();

    // 1 pessoa digitando
    groupTypingSub!.next({
      conversationId: 'conv-group',
      userId: 'user-2',
      username: 'fallen',
      nickname: 'FalleN',
      isTyping: true,
    });
    fixture.detectChanges();

    let conv = component.allConversations().find((c) => c.id === 'conv-group');
    expect(conv?.isTyping).toBe(true);
    expect(conv?.typingText).toBe('FalleN está digitando...');

    // 2 pessoas digitando ao mesmo tempo
    groupTypingSub!.next({
      conversationId: 'conv-group',
      userId: 'user-3',
      username: 'coldzera',
      nickname: 'Coldzera',
      isTyping: true,
    });
    fixture.detectChanges();

    conv = component.allConversations().find((c) => c.id === 'conv-group');
    expect(conv?.isTyping).toBe(true);
    expect(conv?.typingText).toBe('FalleN e Coldzera estão digitando...');

    // 3 pessoas digitando ao mesmo tempo
    groupTypingSub!.next({
      conversationId: 'conv-group',
      userId: 'user-4',
      username: 'fer',
      nickname: 'Fer',
      isTyping: true,
    });
    fixture.detectChanges();

    conv = component.allConversations().find((c) => c.id === 'conv-group');
    expect(conv?.isTyping).toBe(true);
    expect(conv?.typingText).toBe('3 pessoas estão digitando...');

    // FalleN para de digitar: volta para 2 pessoas
    groupTypingSub!.next({
      conversationId: 'conv-group',
      userId: 'user-2',
      username: 'fallen',
      nickname: 'FalleN',
      isTyping: false,
    });
    fixture.detectChanges();

    conv = component.allConversations().find((c) => c.id === 'conv-group');
    expect(conv?.isTyping).toBe(true);
    expect(conv?.typingText).toBe('Coldzera e Fer estão digitando...');
  });

  it('should clear typing indicator when a message arrives from the typing user', async () => {
    fixture = TestBed.createComponent(ChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const typingSub = typingSubjects.get('conv-1');
    typingSub!.next({
      conversationId: 'conv-1',
      userId: 'user-2',
      username: 'otheruser',
      nickname: 'Other User',
      isTyping: true,
    });
    fixture.detectChanges();

    expect(component.allConversations()[0].isTyping).toBe(true);

    // Chega uma nova mensagem do usuário que estava digitando
    newMessage$.next({
      ...mockMessageResponse,
      id: 'msg-new',
      content: 'Cheguei!',
    });
    fixture.detectChanges();

    expect(component.allConversations()[0].isTyping).toBe(false);
    expect(component.allConversations()[0].typingText).toBeUndefined();
  });
});
