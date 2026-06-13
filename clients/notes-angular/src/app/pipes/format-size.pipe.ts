import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'formatSize',
  standalone: true,
})
export class FormatSizePipe implements PipeTransform {
  transform(bytes: number): string {
    const k = 1024;
    const dm = 3;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    if (bytes === 0) {
      return `0 ${sizes[0]}`;
    }

    const i = Math.floor(Math.log(bytes) / Math.log(k));
    const formattedSize = parseFloat((bytes / Math.pow(k, i)).toFixed(dm));

    return `${formattedSize} ${sizes[i]}`;
  }
}

