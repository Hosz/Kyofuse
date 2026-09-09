import { Component, ElementRef, ViewChild, inject, output, signal } from '@angular/core';
import { MentionInputComponent } from '../../../shared/components/mention-input/mention-input';
import { ProfileService } from '../../../core/services/profile/profile.service';
import { PostsService } from '../../../core/services/posts/posts.service';
import { MediaService } from '../../../core/services/media/media.service';
import { ToastService } from '../../../core/services/ui/toast.service';
import { PostMediaItemRequest } from '../../../models/posts/post-request.model';
import { FALLBACK_AVATAR_URL } from '../../../shared/utils/format.util';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

@Component({
  selector: 'app-post-composer',
  imports: [MentionInputComponent, TranslatePipe],
  templateUrl: './post-composer.html',
  styleUrl: './post-composer.css',
})
export class PostComposerComponent {
  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

  postService = inject(PostsService);
  userService = inject(ProfileService);
  mediaService = inject(MediaService);
  toastService = inject(ToastService);

  readonly fallbackAvatar = FALLBACK_AVATAR_URL;
  currentUserAvatar = signal<string>('');

  publish = output<string>();

  content = signal('');
  postType = signal('TEXT');
  visibility = signal('PUBLIC');
  maps = signal<string[]>([]);
  mediaItems = signal<PostMediaItemRequest[]>([]);
  uploading = signal(false);
  publishing = signal(false);

  comingSoon(feature: string): void {
    this.toastService.info(`${feature} estará disponível em breve!`);
  }

  onContentChange(value: string): void {
    this.content.set(value);
  }

  triggerFileInput(): void {
    if (this.uploading() || this.publishing()) return;
    this.fileInput?.nativeElement.click();
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const files = Array.from(input.files);
    const availableSlots = 4 - this.mediaItems().length;

    if (availableSlots <= 0) {
      this.toastService.info('Você pode adicionar até 4 imagens por publicação.');
      input.value = '';
      return;
    }

    const filesToUpload = files.slice(0, availableSlots);
    if (files.length > availableSlots) {
      this.toastService.info(`Limite máximo de 4 imagens. Apenas as primeiras ${availableSlots} foram adicionadas.`);
    }

    const MAX_UPLOAD_SIZE = 25 * 1024 * 1024;
    const validFiles = filesToUpload.filter((file) => {
      if (file.size > MAX_UPLOAD_SIZE) {
        this.toastService.error(`A imagem "${file.name}" ultrapassa o limite de 25MB.`);
        return false;
      }
      return true;
    });

    if (validFiles.length === 0) {
      input.value = '';
      return;
    }

    this.uploading.set(true);

    let completed = 0;
    validFiles.forEach((file) => {
      this.mediaService.uploadImage(file).subscribe({
        next: (res) => {
          const item: PostMediaItemRequest = {
            fileKey: res.fileKey,
            url: res.url,
            thumbnailUrl: res.thumbnailUrl,
            contentType: res.contentType,
            fileSizeBytes: res.fileSizeBytes,
            width: res.width,
            height: res.height,
            displayOrder: this.mediaItems().length,
          };
          this.mediaItems.update((items) => [...items, item]);
          completed++;
          if (completed === validFiles.length) {
            this.uploading.set(false);
            if (this.fileInput) this.fileInput.nativeElement.value = '';
          }
        },
        error: (err) => {
          console.error('Failed to upload image:', err);
          this.toastService.error('Erro ao fazer upload da imagem.');
          completed++;
          if (completed === validFiles.length) {
            this.uploading.set(false);
            if (this.fileInput) this.fileInput.nativeElement.value = '';
          }
        },
      });
    });
  }

  removeMedia(index: number): void {
    this.mediaItems.update((items) => items.filter((_, i) => i !== index));
  }

  onPublish(): void {
    const hasText = !!this.content().trim();
    const hasMedia = this.mediaItems().length > 0;

    if (!hasText && !hasMedia) {
      this.toastService.info('Escreva algo ou adicione uma imagem para publicar.');
      return;
    }

    if (this.uploading() || this.publishing()) return;

    this.publishing.set(true);

    this.postService
      .postContent({
        content: this.content(),
        postType: this.postType(),
        visibility: this.visibility(),
        maps: this.maps(),
        media: this.mediaItems(),
      })
      .subscribe({
        next: (response) => {
          this.publish.emit(response.id);
          this.content.set('');
          this.mediaItems.set([]);
          this.publishing.set(false);
          this.toastService.success('Publicação realizada com sucesso!');
        },
        error: (error) => {
          console.error('Failed to publish post:', error);
          this.publishing.set(false);
          this.toastService.error('Não foi possível publicar.');
        },
      });
  }

  ngOnInit() {
    this.userService.myProfile().subscribe({
      next: (response) => {
        this.currentUserAvatar.set(response.avatarUrl);
      },
      error: (error) => {
        console.error('Failed to fetch user profile:', error);
      },
    });
  }
}