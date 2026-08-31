export interface PostMediaItemRequest {
    fileKey: string;
    url: string;
    thumbnailUrl?: string;
    contentType: string;
    fileSizeBytes: number;
    width?: number;
    height?: number;
    displayOrder?: number;
}

export interface postRequest {
    content?: string;
    postType: string;
    visibility: string;
    maps?: string[];
    media?: PostMediaItemRequest[];
}