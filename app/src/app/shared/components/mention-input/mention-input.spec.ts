import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MentionInputComponent } from './mention-input';

describe('MentionInputComponent', () => {
  let component: MentionInputComponent;
  let fixture: ComponentFixture<MentionInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MentionInputComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(MentionInputComponent);
    component = fixture.componentInstance;
  });

  it('não deve exibir marcação quando o texto estiver dentro do limite', () => {
    component.content = 'Texto dentro do limite';
    component.maxLength = 500;
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('mark')).toBeNull();
    expect(component.isOverLimit).toBe(false);
  });

  it('deve exibir marca-texto laranja quando o texto ultrapassar o limite', () => {
    const within = 'A'.repeat(500);
    const beyond = 'EXCEDE';
    component.content = within + beyond;
    component.maxLength = 500;
    fixture.detectChanges();

    expect(component.isOverLimit).toBe(true);
    expect(component.textWithinLimit).toBe(within);
    expect(component.textBeyondLimit).toBe(beyond);

    const compiled = fixture.nativeElement as HTMLElement;
    const mark = compiled.querySelector('mark');
    expect(mark).not.toBeNull();
    expect(mark?.textContent).toBe(beyond);
  });
});
