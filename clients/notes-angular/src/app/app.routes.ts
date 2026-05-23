import { Routes } from '@angular/router';
import { NotesPage } from './pages/notes-page/notes-page';
import { ForbiddenPage } from './pages/forbidden-page/forbidden-page';

export const routes: Routes = [
  { path: '403', component: ForbiddenPage },
  { path: ':id', component: NotesPage },
  { path: '', component: NotesPage },
];
