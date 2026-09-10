import {
  Component,
  ElementRef,
  EventEmitter,
  inject,
  Input,
  Output,
  ViewChild,
  OnInit,
  OnDestroy,
  AfterViewInit,
  HostListener
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Subject, Subscription, of } from 'rxjs';
import { debounceTime, switchMap, tap, catchError } from 'rxjs/operators';
import { API_URL } from '../../../models/api-url.model';

@Component({
  selector: 'app-mention-input',
  standalone: true,
  template: `
    <div class="relative w-full" (click)="focus()">
      <div class="grid w-full">
        <!-- Camada de fundo com o marca-texto perfeitamente alinhado -->
        @if (isOverLimit) {
          <div
            #backdrop
            aria-hidden="true"
            class="col-start-1 row-start-1 pointer-events-none select-none whitespace-pre-wrap break-words m-0 font-sans"
            [class]="computedClass"
            style="box-sizing: border-box; word-break: break-word; overflow-wrap: break-word; border: 0 !important; overflow: hidden;"
          ><span class="opacity-0 select-none" style="font: inherit; line-height: inherit; letter-spacing: inherit;">{{ textWithinLimit }}</span><mark
              class="overflow-mark select-none inline p-0 m-0 rounded-xs"
              style="
                background-color: rgba(227, 106, 0, 0.35);
                color: transparent;
                font: inherit;
                line-height: inherit;
                letter-spacing: inherit;
                border: 0;
                box-decoration-break: clone;
                -webkit-box-decoration-break: clone;
              "
            >{{ textBeyondLimit }}</mark>{{ endsWithNewline ? '\n ' : '' }}</div>
        }

        <!-- Textarea nativo que expande gradativamente até preencher a tela como no X -->
        <textarea
          #textarea
          [rows]="rows"
          [placeholder]="placeholder"
          [value]="content"
          (input)="onInput($event)"
          (keydown)="onKeyDown($event)"
          (scroll)="onScroll()"
          class="col-start-1 row-start-1 w-full resize-none border-0 bg-transparent text-on-surface placeholder:text-on-surface-variant focus:outline-none focus:ring-0 whitespace-pre-wrap break-words m-0 font-sans z-10 scrollbar-minimal"
          [class]="computedClass"
          style="box-sizing: border-box; word-break: break-word; overflow-wrap: break-word; border: 0 !important; outline: none !important;"
        ></textarea>
      </div>

      @if (showAutocomplete) {
        <div
          class="absolute z-50 mt-1 max-h-48 w-64 overflow-y-auto rounded-xl border border-outline-variant bg-surface-container shadow-lg"
          [style.top]="caretTop + 'px'"
          [style.left]="caretLeft + 'px'"
        >
          @if (loading) {
            <div class="p-3 text-center text-label-sm text-on-surface-variant">Buscando...</div>
          } @else if (suggestions.length === 0) {
            <div class="p-3 text-center text-label-sm text-on-surface-variant">Nenhum resultado</div>
          } @else {
            <ul class="flex flex-col py-1">
              @for (suggestion of suggestions; track suggestion.slug; let i = $index) {
                <li
                  class="flex cursor-pointer items-center gap-2 px-3 py-2 transition-colors hover:bg-surface-container-high"
                  [class.bg-surface-container-high]="i === selectedIndex"
                  (click)="selectSuggestion(suggestion)"
                >
                  <img
                    [src]="suggestion.avatarUrl || '/assets/default-avatar.png'"
                    class="h-6 w-6 rounded-full object-cover"
                  />
                  <div class="flex flex-col overflow-hidden">
                    <span class="truncate text-label-md font-bold text-on-surface">{{ suggestion.name }}</span>
                    <span class="truncate text-label-sm text-on-surface-variant">{{ currentPrefix }}{{ suggestion.slug }}</span>
                  </div>
                </li>
              }
            </ul>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
    }
  `]
})
export class MentionInputComponent implements OnInit, OnDestroy, AfterViewInit {
  @Input() set content(val: string) {
    const nextVal = val || '';
    if (this._content !== nextVal) {
      this._content = nextVal;
      if (this.textareaRef) {
        this.textareaRef.nativeElement.value = nextVal;
        setTimeout(() => this.adjustHeight(), 0);
      }
    }
  }
  get content(): string {
    return this._content;
  }
  private _content: string = '';

  @Input() placeholder: string = '';
  @Input() rows: number = 2;
  @Input() maxLength?: number;
  @Input() inputClass: string = '';
  @Output() contentChange = new EventEmitter<string>();

  @ViewChild('textarea') textareaRef!: ElementRef<HTMLTextAreaElement>;
  @ViewChild('backdrop') backdropRef?: ElementRef<HTMLDivElement>;

  get isOverLimit(): boolean {
    return !!(this.maxLength && this.content && this.content.length > this.maxLength);
  }

  get textWithinLimit(): string {
    if (!this.maxLength || !this.content) return this.content || '';
    return this.content.slice(0, this.maxLength);
  }

  get textBeyondLimit(): string {
    if (!this.maxLength || !this.content) return '';
    return this.content.slice(this.maxLength);
  }

  get endsWithNewline(): boolean {
    return !!(this.content && this.content.endsWith('\n'));
  }

  get computedClass(): string {
    return this.inputClass ? this.inputClass : 'p-0 text-body-md text-on-surface';
  }

  private get minHeightPx(): number {
    const isSm = this.computedClass.includes('text-body-sm');
    const lineHeight = isSm ? 20 : 24;
    return (this.rows || 2) * lineHeight + 8;
  }

  private get maxHeightPx(): number {
    if (typeof window !== 'undefined' && window.innerHeight) {
      return Math.round(window.innerHeight * 0.6); // 60vh like X
    }
    return 500;
  }

  private http = inject(HttpClient);
  private searchSubject = new Subject<{ prefix: string; query: string }>();
  private searchSub?: Subscription;
  private static cache = new Map<string, any[]>();

  showAutocomplete = false;
  loading = false;
  suggestions: any[] = [];
  selectedIndex = 0;

  currentPrefix = '';
  searchQuery = '';
  mentionStartIndex = -1;

  caretTop = 40;
  caretLeft = 0;

  @HostListener('window:resize')
  onResize(): void {
    this.adjustHeight();
  }

  ngOnInit(): void {
    this.searchSub = this.searchSubject.pipe(
      debounceTime(120),
      switchMap(({ prefix, query }) => {
        const cacheKey = `${prefix}:${query.toLowerCase()}`;
        return this.http.get<any[]>(`${API_URL}/api/mentions/search?prefix=${encodeURIComponent(prefix)}&query=${encodeURIComponent(query)}`).pipe(
          tap(res => MentionInputComponent.cache.set(cacheKey, res)),
          catchError(() => of([]))
        );
      })
    ).subscribe(res => {
      this.suggestions = res;
      this.loading = false;
      if (this.selectedIndex >= res.length) this.selectedIndex = 0;
    });
  }

  ngAfterViewInit(): void {
    this.adjustHeight();
  }

  ngOnDestroy(): void {
    this.searchSub?.unsubscribe();
  }

  focus(): void {
    if (this.textareaRef) {
      this.textareaRef.nativeElement.focus();
    }
  }

  onScroll(): void {
    if (this.backdropRef && this.textareaRef) {
      this.backdropRef.nativeElement.scrollTop = this.textareaRef.nativeElement.scrollTop;
      this.backdropRef.nativeElement.scrollLeft = this.textareaRef.nativeElement.scrollLeft;
    }
  }

  adjustHeight(): void {
    const textarea = this.textareaRef?.nativeElement;
    if (!textarea) return;

    textarea.style.height = 'auto';
    const scrollHeight = textarea.scrollHeight;

    const minPx = this.minHeightPx;
    const maxPx = this.maxHeightPx;

    const targetHeight = Math.max(minPx, Math.min(scrollHeight, maxPx));
    textarea.style.height = `${targetHeight}px`;
    textarea.style.overflowY = scrollHeight > maxPx ? 'auto' : 'hidden';

    const backdrop = this.backdropRef?.nativeElement;
    if (backdrop) {
      backdrop.style.height = `${targetHeight}px`;
      backdrop.style.overflowY = 'hidden';
    }
  }

  onInput(event: Event): void {
    const val = (event.target as HTMLTextAreaElement).value;
    this._content = val;
    this.contentChange.emit(this._content);

    const cursorPosition = (event.target as HTMLTextAreaElement).selectionStart;
    const textBeforeCursor = val.substring(0, cursorPosition);

    const match = textBeforeCursor.match(/(?:^|\s)([@$]|(?:\/\/))([A-Za-z0-9_.-]*)$/);
    if (match) {
      this.currentPrefix = match[1];
      this.searchQuery = match[2];
      this.mentionStartIndex = cursorPosition - this.currentPrefix.length - this.searchQuery.length;

      this.showAutocomplete = true;
      this.updateCaretPosition();

      const cacheKey = `${this.currentPrefix}:${this.searchQuery.toLowerCase()}`;
      if (MentionInputComponent.cache.has(cacheKey)) {
        this.suggestions = MentionInputComponent.cache.get(cacheKey)!;
        this.loading = false;
      } else {
        this.loading = true;
      }

      this.searchSubject.next({ prefix: this.currentPrefix, query: this.searchQuery });
    } else {
      this.showAutocomplete = false;
    }

    this.adjustHeight();
    setTimeout(() => this.onScroll(), 0);
  }

  onKeyDown(event: KeyboardEvent): void {
    if (!this.showAutocomplete) return;

    if (event.key === 'Escape') {
      event.preventDefault();
      this.showAutocomplete = false;
    } else if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.selectedIndex = (this.selectedIndex + 1) % this.suggestions.length;
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.selectedIndex = (this.selectedIndex - 1 + this.suggestions.length) % this.suggestions.length;
    } else if (event.key === 'Enter') {
      if (this.suggestions.length > 0) {
        event.preventDefault();
        this.selectSuggestion(this.suggestions[this.selectedIndex]);
      }
    }
  }

  selectSuggestion(suggestion: any): void {
    const textarea = this.textareaRef?.nativeElement;
    if (!textarea) return;

    const cursor = textarea.selectionStart;
    const textBeforeCursor = this.content.substring(0, cursor);
    const match = textBeforeCursor.match(/(\s*)([@$]|(?:\/\/))([A-Za-z0-9_.-]*)$/);

    if (match) {
      const space = match[1] || '';
      const beforeSpace = this.content.substring(0, cursor - match[0].length);
      const afterCursor = this.content.substring(cursor);

      const newText = beforeSpace + space + this.currentPrefix + suggestion.slug + ' ' + afterCursor;
      this._content = newText;
      this.contentChange.emit(this._content);
      this.showAutocomplete = false;

      setTimeout(() => {
        const newCursorPos = (beforeSpace + space + this.currentPrefix + suggestion.slug + ' ').length;
        textarea.focus();
        textarea.setSelectionRange(newCursorPos, newCursorPos);
        this.adjustHeight();
        this.onScroll();
      });
    }
  }

  private updateCaretPosition(): void {
    this.caretTop = 40;
    this.caretLeft = 0;
  }
}
