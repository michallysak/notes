import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-text-range',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="text-range">
      <span [class.red]="isRed" [class.yellow]="isYellow" [class.green]="isGreen">
        {{ current }} / {{ max }}
      </span>
    </div>
  `,
  styles: [
    `
      .text-range {
        font-size: 0.85rem;
        margin-top: 0.25rem;
        span {
          font-weight: 600;
        }
        .red {
          color: #d9534f;
        }
        .yellow {
          color: #f0ad4e;
        }
        .green {
          color: #5cb85c;
        }
      }
    `,
  ],
})
export class TextRangeComponent {
  @Input({ required: true }) min!: number;
  @Input({ required: true }) max!: number;
  @Input({ required: true }) current!: number;

  get percent(): number {
    if (!this.max) return 0;
    return (this.current / this.max) * 100;
  }

  get isRed(): boolean {
    return this.current < this.min || this.percent >= 100;
  }

  get isYellow(): boolean {
    return !this.isRed && this.percent >= 75;
  }

  get isGreen(): boolean {
    return !this.isRed && !this.isYellow;
  }
}
