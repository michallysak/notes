import { Routes } from '@angular/router';
import { NotesPage } from './pages/notes-page/notes-page';
import { ForbiddenPage } from './pages/forbidden-page/forbidden-page';
import { PublicNotePage } from './pages/public-note-page/public-note-page';

export const routes: Routes = [
  { path: '403', component: ForbiddenPage },
  { path: 'public/:publicShareId', component: PublicNotePage },
  { path: ':id', component: NotesPage },
  { path: '', component: NotesPage },
];
