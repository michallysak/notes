import { Component, OnDestroy, signal, ElementRef, NgZone, OnInit } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';
import { ToolbarModule } from 'primeng/toolbar';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { NoteCardComponent } from '../note-card/note-card.component';
import { NotesAPIService, NoteResponse, NoteUpdateRequest } from '@notes/notes_service';
import { AuthService } from '../../services/auth/auth.service';
import { NoteService } from '../../services/note/note.service';
import { Note } from '../../types/note';
import { BehaviorSubject, Subject, takeUntil, debounceTime } from 'rxjs';
import { Router } from '@angular/router';

export interface SearchSection {
  data: Note[];
  page: number;
  hasMore: boolean;
  loading?: boolean;
}

interface FilterOption {
  label: string;
  value: boolean | undefined;
}

type SearchFilterForm = {
  searchQuery: FormControl<string>;
  sharedFilter: FormControl<boolean | undefined>;
  pinnedFilter: FormControl<boolean | undefined>;
};

@Component({
  selector: 'app-note-search',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    InputTextModule,
    ButtonModule,
    DialogModule,
    ProgressSpinnerModule,
    SelectModule,
    ToolbarModule,
    IconFieldModule,
    InputIconModule,
    TranslatePipe,
    NoteCardComponent,
  ],
  templateUrl: './note-search.component.html',
  styleUrls: ['./note-search.component.scss'],
})
export class NoteSearchComponent implements OnDestroy, OnInit {
  form: FormGroup<SearchFilterForm>;
  visible = signal(false);

  filterOptions: FilterOption[] = [];

  searchResults = new BehaviorSubject<SearchSection>({ data: [], page: 0, hasMore: true });
  searchResults$ = toSignal(this.searchResults);

  private destroy$ = new Subject<void>();
  private pageSize = 25;
  private scrollListener: (() => void) | null = null;

  constructor(
    private notesApi: NotesAPIService,
    private auth: AuthService,
    private router: Router,
    private el: ElementRef,
    private ngZone: NgZone,
    private translate: TranslateService,
    private noteService: NoteService,
  ) {
    this.form = new FormGroup<SearchFilterForm>({
      searchQuery: new FormControl('', { nonNullable: true }),
      sharedFilter: new FormControl<boolean | undefined>(undefined, { nonNullable: true }),
      pinnedFilter: new FormControl<boolean | undefined>(undefined, { nonNullable: true }),
    });

    this.form.get('searchQuery')?.valueChanges
      .pipe(
        debounceTime(500),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.resetAndSearchIfValid();
      });
  }

  ngOnInit() {
    this.initializeFilterOptions();
  }

  private initializeFilterOptions() {
    this.filterOptions = [
      { label: this.translate.instant('NOTES.SEARCH_DIALOG.FILTER.ANY'), value: undefined },
      { label: this.translate.instant('NOTES.SEARCH_DIALOG.FILTER.YES'), value: true },
      { label: this.translate.instant('NOTES.SEARCH_DIALOG.FILTER.NO'), value: false },
    ];
  }

  ngOnDestroy() {
    this.removeScrollListener();
    this.destroy$.next();
    this.destroy$.complete();
  }

  onDialogShow() {
    this.resetSearch();
    if (this.form.get('searchQuery')?.value) {
      this.performSearch();
    }

    setTimeout(() => this.attachScrollListener(), 100);
  }

  onDialogHide() {
    this.removeScrollListener();
    this.resetFormAndSearch();
  }

  private resetFormAndSearch() {
    this.form.reset();
    this.resetSearch();
  }

  private attachScrollListener() {
    this.removeScrollListener();

    const dialogContent = this.el.nativeElement.querySelector('.search-results-inner');
    if (!dialogContent) return;

    const handler = () => {
      const position = dialogContent.scrollTop + dialogContent.clientHeight;
      const scrollHeight = dialogContent.scrollHeight;
      const distanceFromBottom = scrollHeight - position;

      const current = this.searchResults.value;

      if (distanceFromBottom < 300 && current.hasMore && !current.loading) {
        this.ngZone.run(() => this.loadMore());
      }
    };

    dialogContent.addEventListener('scroll', handler, { passive: true });
    this.scrollListener = () => dialogContent.removeEventListener('scroll', handler);
  }

  private removeScrollListener() {
    if (this.scrollListener) {
      this.scrollListener();
      this.scrollListener = null;
    }
  }

  onSharedFilterChange(value: boolean | undefined) {
    this.form.get('sharedFilter')?.setValue(value);
    this.resetAndSearchIfValid();
  }

  onPinnedFilterChange(value: boolean | undefined) {
    this.form.get('pinnedFilter')?.setValue(value);
    this.resetAndSearchIfValid();
  }

  private resetAndSearchIfValid() {
    this.resetSearch();
    const query = this.form.get('searchQuery')?.value;
    if (query && query.length >= 3) {
      this.performSearch();
    }
  }

  private performSearch() {
    const current = this.searchResults.value;
    if (current.loading) return;
    const query = this.form.get('searchQuery')?.value;
    if (query && query.length < 3) {
      return;
    }

    this.searchResults.next({ ...current, loading: true });

    this.notesApi
      .searchNotes(
        this.form.get('pinnedFilter')?.value,
        this.form.get('sharedFilter')?.value,
        current.page,
        query || undefined,
        this.pageSize,
      )
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          const data = res?.data || [];
          const mappedData = data.map((n) => this.mapToNote(n));

          const nextPage = current.page + 1;
          const hasMore = nextPage * this.pageSize < (res?.total || 0);

          this.searchResults.next({
            data: [...current.data, ...mappedData],
            page: nextPage,
            hasMore,
            loading: false,
          });
        },
        error: (err) => {
          console.error('Search failed', err);
          this.searchResults.next({ ...current, loading: false });
        },
      });
  }

  loadMore() {
    const current = this.searchResults.value;
    if (!current.hasMore || current.loading) return;
    this.performSearch();
  }

  private resetSearch() {
    this.searchResults.next({ data: [], page: 0, hasMore: true });
  }

  private mapToNote(res: NoteResponse): Note {
    const shares = res.shares || [];
    const currentUser = this.auth.getCurrentUserValue();
    const currentUserId = currentUser?.id;

    const shared = shares.length > 0;
    const isAuthor = res.authorId === currentUserId;
    const canEdit = isAuthor || shares.some(
      (p: any) => p.userId === currentUserId && (p.permissions ?? []).includes('EDIT')
    );

    return { ...res, shared, canEdit };
  }

  onNoteClick(note: Note) {
    this.visible.set(false);
    this.router.navigate(['/', note.id]);
  }

   onNoteShare(_note: Note) {}

   openSearch() {
     this.visible.set(true);
   }
}
