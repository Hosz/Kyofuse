import { Component, Input, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-post-content',
  standalone: true,
  imports: [RouterLink],
  template: `
    <p class="text-body-md text-on-surface whitespace-pre-wrap break-words">
      @for (segment of segments; track $index) {
        @if (segment.type === 'mention') {
          <a
            [routerLink]="segment.link"
            (click)="$event.stopPropagation()"
            class="text-primary font-medium hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-container"
          >{{ segment.value }}</a>
        } @else {
          <span>{{ segment.value }}</span>
        }
      }
    </p>
  `
})
export class PostContentComponent implements OnInit {
  @Input({ required: true }) content!: string;

  segments: { type: 'text' | 'mention'; value: string; link?: string }[] = [];

  ngOnInit() {
    this.parseContent();
  }

  private parseContent() {
    const mentionRegex = /(@[A-Za-z0-9_.-]+|\$[A-Za-z0-9_.-]+|\/\/[A-Za-z0-9_.-]+)/g;
    const parts = this.content.split(mentionRegex);

    this.segments = parts.map(part => {
      if (part.startsWith('@')) {
        return { type: 'mention', value: part, link: '/perfil/' + part.substring(1) };
      } else if (part.startsWith('$')) {
        return { type: 'mention', value: part, link: '/times/' + part.substring(1) };
      } else if (part.startsWith('//')) {
        return { type: 'mention', value: part, link: '/comunidade/' + part.substring(2) };
      } else {
        return { type: 'text', value: part };
      }
    });
  }
}
