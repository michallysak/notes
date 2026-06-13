import { FormatSizePipe } from './format-size.pipe';

describe('FormatSizePipe', () => {
  let pipe: FormatSizePipe;

  beforeEach(() => {
    pipe = new FormatSizePipe();
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should format 0 bytes as "0 B"', () => {
    expect(pipe.transform(0)).toBe('0 B');
  });

  it('should format bytes correctly', () => {
    // 1 byte
    expect(pipe.transform(1)).toBe('1 B');

    // 512 bytes
    expect(pipe.transform(512)).toBe('512 B');

    // 1023 bytes
    expect(pipe.transform(1023)).toBe('1023 B');
  });

  it('should format kilobytes correctly', () => {
    // 1 KB
    expect(pipe.transform(1024)).toBe('1 KB');

    // 1.5 KB
    expect(pipe.transform(1536)).toBe('1.5 KB');

    // 100 KB
    expect(pipe.transform(102400)).toBe('100 KB');
  });

  it('should format megabytes correctly', () => {
    // 1 MB
    expect(pipe.transform(1024 * 1024)).toBe('1 MB');

    // 2.5 MB
    expect(pipe.transform(2.5 * 1024 * 1024)).toBe('2.5 MB');

    // 100 MB
    expect(pipe.transform(100 * 1024 * 1024)).toBe('100 MB');
  });

  it('should format gigabytes correctly', () => {
    // 1 GB
    expect(pipe.transform(1024 * 1024 * 1024)).toBe('1 GB');

    // 2.5 GB
    expect(pipe.transform(2.5 * 1024 * 1024 * 1024)).toBe('2.5 GB');

    // 10 GB
    expect(pipe.transform(10 * 1024 * 1024 * 1024)).toBe('10 GB');
  });

  it('should handle large file sizes', () => {
    // 1000 GB (should overflow to GB range)
    expect(pipe.transform(1000 * 1024 * 1024 * 1024)).toBe('1000 GB');
  });

  it('should format with proper decimal places', () => {
    // 1.234 MB
    const result = pipe.transform(1.234 * 1024 * 1024);
    expect(result).toMatch(/1\.234 MB/);

    // 512.512 KB
    const result2 = pipe.transform(512.512 * 1024);
    expect(result2).toMatch(/512\.512 KB/);
  });
});

