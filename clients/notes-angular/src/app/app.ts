import { Component, signal, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MyService } from './my-service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ButtonModule],
  templateUrl: './app.html',
  styleUrls: ['./app.scss'],
})
export class App {
  protected readonly title = signal('notes-angular');
  protected readonly iterator = signal(0);

  private readonly noteApi = inject(MyService);

  btnClick() {
    this.iterator.update((value) => value + 1);
    this.noteApi.getNotes();
  }
}
