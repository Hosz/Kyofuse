export interface MessageMediaResponse {
  id: string;
  messageId: string;
  fileKey: string;
  url: string;
  thumbnailUrl?: string;
  contentType: string;
  fileSizeBytes: number;
  width?: number;
  height?: number;
  createdAt: string;
}
