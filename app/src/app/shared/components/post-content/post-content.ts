import { Component, computed, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';

export interface ContentSegment {
  type: 'text' | 'mention';
  value: string;
  link?: string;
}

@Component({
  selector: 'app-post-content',
  standalone: true,
  imports: [RouterLink, TranslatePipe],
  template: `
    <p class="text-body-md text-on-surface whitespace-pre-wrap break-words [overflow-wrap:anywhere] min-w-0">
      @for (segment of displaySegments(); track $index) {
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
      @if (analysis().isLong) {
        @if (!expanded()) {
          <span class="text-on-surface-variant select-none">... </span>
          <button
            type="button"
            (click)="toggleExpanded($event)"
            class="inline-flex cursor-pointer items-center font-bold text-primary hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-container"
          >
            {{ 'feed.showMore' | trans }}
          </button>
        } @else {
          <button
            type="button"
            (click)="toggleExpanded($event)"
            class="ml-2 inline-flex cursor-pointer items-center font-bold text-primary hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-container"
          >
            {{ 'feed.showLess' | trans }}
          </button>
        }
      }
    </p>
  `,
})
export class PostContentComponent {
  readonly content = input.required<string>();
  readonly collapsible = input(true);
  readonly maxCharacters = input(280);
  readonly maxLines = input(5);

  readonly expanded = signal(false);

  readonly fullSegments = computed<ContentSegment[]>(() => this.parseSegments(this.content() ?? ''));

  readonly analysis = computed(() => {
    const text = this.content() ?? '';
    if (!this.collapsible() || !text) {
      return { isLong: false, truncatedText: text };
    }

    const lines = text.split('\n');
    const isLongByLines = lines.length > this.maxLines();
    const isLongByChars = text.length > this.maxCharacters();

    if (!isLongByLines && !isLongByChars) {
      return { isLong: false, truncatedText: text };
    }

    let cutIndex = this.maxCharacters();
    if (isLongByLines) {
      const lineSlice = lines.slice(0, this.maxLines()).join('\n');
      if (lineSlice.length < cutIndex) {
        cutIndex = lineSlice.length;
      }
    }

    if (cutIndex > text.length) {
      cutIndex = text.length;
    }

    // Cut at last space if within reasonable distance (last 40 chars)
    const lastSpace = text.lastIndexOf(' ', cutIndex);
    if (lastSpace > cutIndex - 40 && lastSpace > 0) {
      cutIndex = lastSpace;
    }

    // Prevent cutting inside a mention token: @mention, $team, //community
    const mentionRegex = /(@[A-Za-z0-9_.-]+|\$[A-Za-z0-9_.-]+|\/\/[A-Za-z0-9_.-]+)/g;
    let match: RegExpExecArray | null;
    while ((match = mentionRegex.exec(text)) !== null) {
      const start = match.index;
      const end = start + match[0].length;
      if (cutIndex > start && cutIndex < end) {
        cutIndex = cutIndex - start >= end - cutIndex ? end : start;
        break;
      }
    }

    const truncatedText = text.slice(0, cutIndex).trimEnd();
    return { isLong: true, truncatedText };
  });

  readonly truncatedSegments = computed<ContentSegment[]>(() =>
    this.parseSegments(this.analysis().truncatedText)
  );

  readonly displaySegments = computed<ContentSegment[]>(() => {
    if (!this.analysis().isLong || this.expanded()) {
      return this.fullSegments();
    }
    return this.truncatedSegments();
  });

  toggleExpanded(event: MouseEvent): void {
    event.stopPropagation();
    event.preventDefault();
    this.expanded.update((v) => !v);
  }

  private parseSegments(text: string): ContentSegment[] {
    if (!text) return [];
    const mentionRegex = /(@[A-Za-z0-9_.-]+|\$[A-Za-z0-9_.-]+|\/\/[A-Za-z0-9_.-]+)/g;
    const parts = text.split(mentionRegex);

    return parts.map((part) => {
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

