export interface MediaUploadResponse {
  fileKey: string;
  url: string;
  thumbnailUrl?: string;
  contentType: string;
  fileSizeBytes: number;
  width?: number;
  height?: number;
}
