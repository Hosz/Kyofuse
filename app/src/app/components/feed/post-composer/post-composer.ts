import { Component, inject, input, output, signal } from '@angular/core';
import { ProfileService } from '../../../core/services/profile/profile.service';
import { PostsService } from '../../../core/services/posts/posts.service';

@Component({
  selector: 'app-post-composer',
  imports: [],
  templateUrl: './post-composer.html',
  styleUrl: './post-composer.css',
})
export class PostComposerComponent {

  postService = inject(PostsService);
  userService = inject(ProfileService);

  currentUserAvatar = signal<string>('');
  
  publish = output<string>();
 
  content = signal('');
  postType = signal('TEXT');
  visibility = signal('PUBLIC');
  maps = signal<string[]>([]);
 
  onContentChange(value: string): void {
    this.content.set(value);
  }
 
  onPublish(): void {
    this.postService.postContent({
      content: this.content(),
      postType: this.postType(),
      visibility: this.visibility(),
      maps: this.maps()
    }).subscribe({
      next: (response) => {
        console.log('Post published successfully:', response);
        this.publish.emit(response.id);
        this.content.set('');
      },
      error: (error) => {
        console.error('Failed to publish post:', error);
      }
    });
  }

  ngOnInit() {
    this.userService.myProfile().subscribe({
    next: (response) => {
      this.currentUserAvatar.set(response.avatarUrl);
    },
    error: (error) => {
      console.error('Failed to fetch user profile:', error);
    }
    });
  }
}