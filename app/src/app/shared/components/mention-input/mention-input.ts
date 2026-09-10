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
  AfterViewInit
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
      @if (!content || content.length === 0) {
        <div
          class="pointer-events-none absolute inset-0 select-none text-on-surface-variant/60 font-sans"
          [class]="computedClass"
        >
          {{ placeholder }}
        </div>
      }

      <div
        #editor
        contenteditable="true"
        role="textbox"
        aria-multiline="true"
        [attr.aria-label]="placeholder"
        (input)="onInput($event)"
        (keydown)="onKeyDown($event)"
        (paste)="onPaste($event)"
        class="w-full break-words whitespace-pre-wrap outline-none focus:outline-none focus:ring-0 max-h-72 overflow-y-auto scrollbar-minimal font-sans"
        [class]="computedClass"
        [style.min-height]="minHeight"
      ></div>

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
    }
    [contenteditable]:empty:before {
      display: none;
    }
    mark.overflow-mark {
      background-color: rgba(227, 106, 0, 0.28);
      color: #c45a00;
      border-radius: 2px;
      padding-inline: 1px;
    }
    :host-context(.dark) mark.overflow-mark {
      background-color: rgba(227, 106, 0, 0.32);
      color: #ff9d47;
    }
  `]
})
export class MentionInputComponent implements OnInit, OnDestroy, AfterViewInit {
  @Input() set content(val: string) {
    const nextVal = val || '';
    if (this._content !== nextVal) {
      this._content = nextVal;
      if (this.editorRef && this.extractText() !== nextVal) {
        this.renderDom(nextVal);
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

  @ViewChild('editor') editorRef!: ElementRef<HTMLDivElement>;

  get textareaRef(): ElementRef<any> {
    return this.editorRef;
  }

  private hasOverflowMark = false;

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

  get computedClass(): string {
    return this.inputClass ? this.inputClass : 'p-0 text-body-md text-on-surface';
  }

  get minHeight(): string {
    const isSm = this.computedClass.includes('text-body-sm');
    const lineHeight = isSm ? 1.25 : 1.5;
    return `${(this.rows || 2) * lineHeight}rem`;
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
    if (this._content) {
      this.renderDom(this._content);
    }
  }

  ngOnDestroy(): void {
    this.searchSub?.unsubscribe();
  }

  focus(): void {
    if (this.editorRef) {
      this.editorRef.nativeElement.focus();
    }
  }

  onInput(event: Event): void {
    const text = this.extractText();
    this._content = text;
    this.contentChange.emit(this._content);

    const editor = this.editorRef?.nativeElement;
    const cursorPosition = editor ? this.getCaretOffset(editor) : 0;
    const textBeforeCursor = text.substring(0, cursorPosition);

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

    if (this.isOverLimit) {
      this.renderOverflow(text);
    } else if (this.hasOverflowMark) {
      this.clearOverflow(text);
    }
  }

  onPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const text = event.clipboardData?.getData('text/plain') || '';
    if (!text) return;

    const sel = window.getSelection();
    if (!sel || sel.rangeCount === 0) return;
    const range = sel.getRangeAt(0);
    range.deleteContents();
    const textNode = document.createTextNode(text);
    range.insertNode(textNode);
    range.setStartAfter(textNode);
    range.collapse(true);
    sel.removeAllRanges();
    sel.addRange(range);

    this.onInput(event);
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
    const editor = this.editorRef?.nativeElement;
    if (!editor) return;

    const cursor = this.getCaretOffset(editor);
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

      this.renderDom(newText);

      setTimeout(() => {
        const newCursorPos = (beforeSpace + space + this.currentPrefix + suggestion.slug + ' ').length;
        editor.focus();
        this.setCaretOffset(editor, newCursorPos);
      });
    }
  }

  private updateCaretPosition(): void {
    this.caretTop = 40;
    this.caretLeft = 0;
  }

  private renderDom(text: string): void {
    if (!this.editorRef) return;
    const editor = this.editorRef.nativeElement;
    if (!text) {
      editor.innerHTML = '';
      this.hasOverflowMark = false;
      return;
    }
    if (this.maxLength && text.length > this.maxLength) {
      const valid = text.slice(0, this.maxLength);
      const overflow = text.slice(this.maxLength);
      editor.innerHTML = `${this.escapeHtml(valid)}<mark class="overflow-mark rounded-xs bg-primary/25 text-primary font-medium select-text">${this.escapeHtml(overflow)}</mark>`;
      this.hasOverflowMark = true;
    } else {
      editor.innerHTML = this.escapeHtml(text);
      this.hasOverflowMark = false;
    }
  }

  private renderOverflow(text: string): void {
    if (!this.maxLength || !this.editorRef) return;
    const editor = this.editorRef.nativeElement;
    const caretOffset = this.getCaretOffset(editor);

    const valid = text.slice(0, this.maxLength);
    const overflow = text.slice(this.maxLength);

    editor.innerHTML = `${this.escapeHtml(valid)}<mark class="overflow-mark rounded-xs bg-primary/25 text-primary font-medium select-text">${this.escapeHtml(overflow)}</mark>`;
    this.hasOverflowMark = true;

    this.setCaretOffset(editor, caretOffset);
  }

  private clearOverflow(text: string): void {
    if (!this.editorRef) return;
    const editor = this.editorRef.nativeElement;
    const caretOffset = this.getCaretOffset(editor);

    editor.innerHTML = this.escapeHtml(text);
    this.hasOverflowMark = false;

    this.setCaretOffset(editor, caretOffset);
  }

  private extractText(): string {
    const el = this.editorRef?.nativeElement;
    if (!el) return '';
    return this.extractTextFromNode(el);
  }

  private extractTextFromNode(node: Node): string {
    let text = '';
    for (let i = 0; i < node.childNodes.length; i++) {
      const child = node.childNodes[i];
      if (child.nodeType === Node.TEXT_NODE) {
        text += child.nodeValue || '';
      } else if (child.nodeName === 'BR') {
        text += '\n';
      } else if (child.nodeName === 'DIV' || child.nodeName === 'P') {
        if (text.length > 0 && !text.endsWith('\n')) text += '\n';
        if (child.childNodes.length === 1 && child.childNodes[0].nodeName === 'BR') {
          text += '\n';
        } else {
          text += this.extractTextFromNode(child);
        }
      } else {
        text += this.extractTextFromNode(child);
      }
    }
    return text;
  }

  private escapeHtml(str: string): string {
    return str
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;')
      .replace(/\n/g, '<br>');
  }

  private getCaretOffset(editor: HTMLElement): number {
    const sel = window.getSelection();
    if (!sel || sel.rangeCount === 0) return 0;
    const range = sel.getRangeAt(0);
    const preCaretRange = range.cloneRange();
    preCaretRange.selectNodeContents(editor);
    preCaretRange.setEnd(range.endContainer, range.endOffset);

    let count = 0;
    const walker = document.createTreeWalker(
      preCaretRange.cloneContents(),
      NodeFilter.SHOW_TEXT | NodeFilter.SHOW_ELEMENT
    );
    let node = walker.nextNode();
    while (node) {
      if (node.nodeType === Node.TEXT_NODE) {
        count += node.nodeValue?.length || 0;
      } else if (node.nodeName === 'BR') {
        count += 1;
      }
      node = walker.nextNode();
    }
    return count;
  }

  private setCaretOffset(editor: HTMLElement, targetOffset: number): void {
    const sel = window.getSelection();
    if (!sel) return;
    let currentOffset = 0;
    let targetNode: Node | null = null;
    let nodeOffset = 0;

    const walker = document.createTreeWalker(
      editor,
      NodeFilter.SHOW_TEXT | NodeFilter.SHOW_ELEMENT
    );
    let node = walker.nextNode();
    while (node) {
      if (node.nodeType === Node.TEXT_NODE) {
        const len = node.nodeValue?.length || 0;
        if (currentOffset + len >= targetOffset) {
          targetNode = node;
          nodeOffset = Math.max(0, Math.min(targetOffset - currentOffset, len));
          break;
        }
        currentOffset += len;
      } else if (node.nodeName === 'BR') {
        if (currentOffset + 1 >= targetOffset) {
          targetNode = node.parentNode;
          nodeOffset = Array.from(targetNode?.childNodes || []).indexOf(node as ChildNode) + 1;
          break;
        }
        currentOffset += 1;
      }
      node = walker.nextNode();
    }

    const range = document.createRange();
    if (targetNode) {
      range.setStart(targetNode, nodeOffset);
      range.collapse(true);
    } else {
      range.selectNodeContents(editor);
      range.collapse(false);
    }
    sel.removeAllRanges();
    sel.addRange(range);
  }
}
