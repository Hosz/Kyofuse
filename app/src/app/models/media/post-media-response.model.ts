export interface PostMediaResponse {
  id: string;
  postId: string;
  fileKey: string;
  url: string;
  thumbnailUrl?: string;
  contentType: string;
  fileSizeBytes: number;
  width?: number;
  height?: number;
  displayOrder: number;
  createdAt: string;
}
