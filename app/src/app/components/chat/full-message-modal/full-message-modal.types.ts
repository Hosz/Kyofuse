import { MessageMediaResponse } from '../../../models/media/message-media-response.model';

export interface FullMessageViewData {
  id: string;
  content: string;
  author: 'me' | 'them';
  senderName: string;
  senderHandle?: string;
  senderAvatarUrl?: string;
  timestamp: string;
  tooltipTime?: string;
  media?: MessageMediaResponse[];
}
