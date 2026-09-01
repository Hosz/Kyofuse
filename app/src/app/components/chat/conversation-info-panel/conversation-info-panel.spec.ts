import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { ConversationInfoPanelComponent } from './conversation-info-panel';
import { ConversationService } from '../../../core/services/chat/conversation.service';
import { ConversationMemberService } from '../../../core/services/chat/conversation-member.service';
import { FriendshipService } from '../../../core/services/friendship/friendship.service';
import { FollowService } from '../../../core/services/follow/follow.service';
import { ToastService } from '../../../core/services/ui/toast.service';
import { MediaService } from '../../../core/services/media/media.service';
import { Conversation } from '../../../shared/models/chat.model';

describe('ConversationInfoPanelComponent', () => {
  let fixture: ComponentFixture<ConversationInfoPanelComponent>;
  let component: ConversationInfoPanelComponent;

  let conversationMemberServiceMock: any;
  let friendshipServiceMock: any;
  let followServiceMock: any;
  let toastServiceMock: any;

  const mockGroupConversation: Conversation = {
    id: 'group-1',
    type: 'GROUP',
    participant: {
      id: undefined,
      name: 'Time de Teste',
      handle: 'time-de-teste',
      avatarUrl: '',
    },
    relationship: 'mutual',
    lastMessageAt: '2026-08-01T12:00:00Z',
    messages: [],
  };

  const mockMembersResponse = {
    content: [
      {
        id: 'cm-1',
        conversationId: 'group-1',
        userId: 'user-1',
        username: 'user1',
        nickname: 'User 1',
        avatarUrl: '',
        role: 'ADMIN' as const,
        status: 'ACTIVE' as const,
        joinedAt: '2026-08-01T10:00:00Z',
      },
    ],
    number: 0,
    size: 50,
    totalElements: 1,
    totalPages: 1,
    last: true,
  };

  const mockFriendsResponse = {
    content: [
      {
        friendId: 'user-2',
        friendUsername: 'friend2',
        avatarUrl: '',
      },
    ],
    number: 0,
    size: 100,
    totalElements: 1,
    totalPages: 1,
    last: true,
  };

  const mockFollowingResponse = {
    content: [
      {
        followedId: 'user-3',
        followedUsername: 'mutual3',
        avatarUrl: '',
        status: 'ACTIVE',
      },
    ],
    number: 0,
    size: 100,
    totalElements: 1,
    totalPages: 1,
    last: true,
  };

  const mockFollowersResponse = {
    content: [
      {
        followerId: 'user-3',
        followerUsername: 'mutual3',
        avatarUrl: '',
        status: 'ACTIVE',
      },
    ],
    number: 0,
    size: 100,
    totalElements: 1,
    totalPages: 1,
    last: true,
  };

  beforeEach(async () => {
    conversationMemberServiceMock = {
      listConversationMembers: vi.fn().mockReturnValue(of(mockMembersResponse)),
      addMember: vi.fn().mockReturnValue(of(undefined)),
      promoteMemberToAdmin: vi.fn().mockReturnValue(of(undefined)),
      demoteAdminToMember: vi.fn().mockReturnValue(of(undefined)),
      removeMember: vi.fn().mockReturnValue(of(undefined)),
    };

    friendshipServiceMock = {
      showMyFriends: vi.fn().mockReturnValue(of(mockFriendsResponse)),
    };

    followServiceMock = {
      showMyFollowing: vi.fn().mockReturnValue(of(mockFollowingResponse)),
      showMyFollowers: vi.fn().mockReturnValue(of(mockFollowersResponse)),
    };

    toastServiceMock = {
      success: vi.fn(),
      error: vi.fn(),
      info: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [ConversationInfoPanelComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ConversationMemberService, useValue: conversationMemberServiceMock },
        { provide: FriendshipService, useValue: friendshipServiceMock },
        { provide: FollowService, useValue: followServiceMock },
        { provide: ToastService, useValue: toastServiceMock },
        { provide: ConversationService, useValue: { editGroupConversation: vi.fn() } },
        { provide: MediaService, useValue: { uploadImage: vi.fn() } },
      ],
    }).compileComponents();
  });

  it('should initialize and load group members', () => {
    fixture = TestBed.createComponent(ConversationInfoPanelComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('open', true);
    fixture.componentRef.setInput('conversation', mockGroupConversation);
    fixture.componentRef.setInput('myUserId', 'user-1');
    fixture.detectChanges();

    expect(conversationMemberServiceMock.listConversationMembers).toHaveBeenCalledWith('group-1');
    expect(component.members().length).toBe(1);
    expect(component.isGroup()).toBe(true);
    expect(component.iAmAdmin()).toBe(true);
  });

  it('should open add-members view, load candidates and filter out existing members', () => {
    fixture = TestBed.createComponent(ConversationInfoPanelComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('open', true);
    fixture.componentRef.setInput('conversation', mockGroupConversation);
    fixture.componentRef.setInput('myUserId', 'user-1');
    fixture.detectChanges();

    component.openAddMembers();
    fixture.detectChanges();

    expect(component.view()).toBe('add-members');
    expect(friendshipServiceMock.showMyFriends).toHaveBeenCalled();
    expect(followServiceMock.showMyFollowing).toHaveBeenCalled();
    expect(followServiceMock.showMyFollowers).toHaveBeenCalled();

    const available = component.availableCandidates();
    expect(available.length).toBe(2);
    expect(available.map((c) => c.id)).toContain('user-2');
    expect(available.map((c) => c.id)).toContain('user-3');
  });

  it('should toggle selection and add selected candidates to the group', () => {
    fixture = TestBed.createComponent(ConversationInfoPanelComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('open', true);
    fixture.componentRef.setInput('conversation', mockGroupConversation);
    fixture.componentRef.setInput('myUserId', 'user-1');
    fixture.detectChanges();

    component.openAddMembers();
    fixture.detectChanges();

    component.toggleCandidateSelection('user-2');
    expect(component.isCandidateSelected('user-2')).toBe(true);
    expect(component.selectedCandidateCount()).toBe(1);

    component.submitAddMembers();
    fixture.detectChanges();

    expect(conversationMemberServiceMock.addMember).toHaveBeenCalledWith('group-1', 'user-2');
    expect(toastServiceMock.success).toHaveBeenCalledWith('Membro adicionado ao grupo com sucesso.');
    expect(component.view()).toBe('members');
  });
});
