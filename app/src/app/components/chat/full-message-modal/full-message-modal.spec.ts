import { ComponentFixture, TestBed } from '@angular/core/testing';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { FullMessageModalComponent } from './full-message-modal';
import { ToastService } from '../../../core/services/ui/toast.service';
import { I18nService } from '../../../core/i18n/i18n.service';
import { FullMessageViewData } from './full-message-modal.types';

describe('FullMessageModalComponent', () => {
  let fixture: ComponentFixture<FullMessageModalComponent>;
  let component: FullMessageModalComponent;
  let toastServiceMock: { success: any; error: any };

  const mockData: FullMessageViewData = {
    id: 'msg-123',
    content: 'Esta é uma mensagem extremamente longa com vários detalhes e instruções táticas para a partida.',
    author: 'them',
    senderName: 'Fallen',
    senderHandle: 'fallen',
    senderAvatarUrl: 'https://example.com/avatar.png',
    timestamp: '15:30',
    tooltipTime: '09/09/2026 15:30:00',
  };

  beforeEach(async () => {
    toastServiceMock = {
      success: vi.fn(),
      error: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [FullMessageModalComponent],
      providers: [
        { provide: ToastService, useValue: toastServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FullMessageModalComponent);
    component = fixture.componentInstance;
  });

  it('should not render message details when data is null', () => {
    fixture.componentRef.setInput('data', null);
    fixture.detectChanges();

    const host = fixture.nativeElement as HTMLElement;
    expect(host.textContent).not.toContain('Fallen');
  });

  it('should display sender details and full message text when data is provided', () => {
    fixture.componentRef.setInput('data', mockData);
    fixture.detectChanges();

    const host = fixture.nativeElement as HTMLElement;
    expect(host.textContent).toContain('Fallen');
    expect(host.textContent).toContain('@fallen');
    expect(host.textContent).toContain(mockData.content);
    expect(host.textContent).toContain('09/09/2026 15:30:00');
  });

  it('should display "Você" indicator when author is me', () => {
    const myData: FullMessageViewData = {
      ...mockData,
      author: 'me',
      senderName: 'Você',
      senderHandle: undefined,
    };

    fixture.componentRef.setInput('data', myData);
    fixture.detectChanges();

    const host = fixture.nativeElement as HTMLElement;
    expect(host.textContent).toContain('Você');
  });

  it('should emit closed event when close button is clicked', () => {
    fixture.componentRef.setInput('data', mockData);
    fixture.detectChanges();

    let closedEmitted = false;
    component.closed.subscribe(() => {
      closedEmitted = true;
    });

    // Close button in footer
    const buttons = fixture.nativeElement.querySelectorAll('button');
    const closeBtn = Array.from(buttons).find((b: any) => b.textContent?.includes('Fechar') || b.textContent?.includes('Close'));
    expect(closeBtn).toBeDefined();
    (closeBtn as HTMLElement).click();

    expect(closedEmitted).toBe(true);
  });

  it('should copy message to clipboard and show toast', async () => {
    const writeTextSpy = vi.fn().mockResolvedValue(undefined);
    Object.assign(navigator, {
      clipboard: {
        writeText: writeTextSpy,
      },
    });

    fixture.componentRef.setInput('data', mockData);
    fixture.detectChanges();

    await component.copyMessage(mockData.content);

    expect(writeTextSpy).toHaveBeenCalledWith(mockData.content);
    expect(toastServiceMock.success).toHaveBeenCalled();
  });
});
