import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NoteChangeDateTimeComponent } from './note-change-date-time.component';
import { NoteResponse } from '@notes/notes_service';
import { Note } from '../../types/note';

describe('NoteMetaComponent', () => {
  let component: NoteChangeDateTimeComponent;
  let fixture: ComponentFixture<NoteChangeDateTimeComponent>;

  const createNote = (overrides: Partial<Note> = {}): NoteResponse =>
    ({
      id: '1',
      title: 'Title',
      content: 'Content',
      pinned: false,
      created: new Date('2026-01-01T10:00:00Z'),
      updated: undefined,
      ...overrides,
    }) as NoteResponse;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NoteChangeDateTimeComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(NoteChangeDateTimeComponent);
    component = fixture.componentInstance;
  });

  const queryElement = (selector: string) => fixture.debugElement.query(By.css(selector));

  it('should show created date when note was not edited', () => {
    fixture.componentRef.setInput('note', createNote());
    fixture.detectChanges();

    const text = queryElement('.note-meta span').nativeElement.textContent;
    expect(text).toContain('Created');
  });

  it('should show edited date when note was edited', () => {
    fixture.componentRef.setInput(
      'note',
      createNote({ updated: new Date('2026-02-01T12:00:00Z') }),
    );
    fixture.detectChanges();

    const text = queryElement('.note-meta span').nativeElement.textContent;
    expect(text).toContain('Edited');
  });
});
