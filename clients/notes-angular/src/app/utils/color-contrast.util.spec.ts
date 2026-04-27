import { describe, it, expect } from 'vitest';
import { normalizeHexColor, mixHexWithBase } from './color-contrast.util';

describe('color-contrast.util', () => {
  describe('normalizeHexColor', () => {
    it('should return null for undefined color', () => {
      expect(normalizeHexColor(undefined)).toBeNull();
    });

    it('should return null for null color', () => {
      expect(normalizeHexColor(null)).toBeNull();
    });

    it('should return null for empty string', () => {
      expect(normalizeHexColor('')).toBeNull();
    });

    it('should return null for whitespace-only string', () => {
      expect(normalizeHexColor('   ')).toBeNull();
    });

    it('should return null for invalid hex format', () => {
      expect(normalizeHexColor('rgb(255, 0, 0)')).toBeNull();
      expect(normalizeHexColor('#GGGGGG')).toBeNull();
      expect(normalizeHexColor('#12')).toBeNull();
      expect(normalizeHexColor('#1234567')).toBeNull();
    });

    it('should normalize 6-character hex color to lowercase', () => {
      expect(normalizeHexColor('#FF0000')).toBe('#ff0000');
      expect(normalizeHexColor('#AbCdEf')).toBe('#abcdef');
    });

    it('should normalize 3-character hex color to 6-character lowercase', () => {
      expect(normalizeHexColor('#F00')).toBe('#ff0000');
      expect(normalizeHexColor('#0F0')).toBe('#00ff00');
      expect(normalizeHexColor('#00F')).toBe('#0000ff');
      expect(normalizeHexColor('#ABC')).toBe('#aabbcc');
    });

    it('should ignore leading/trailing whitespace', () => {
      expect(normalizeHexColor('  #FF0000  ')).toBe('#ff0000');
      expect(normalizeHexColor(' #F00 ')).toBe('#ff0000');
    });

    it('should accept lowercase hex colors', () => {
      expect(normalizeHexColor('#ff0000')).toBe('#ff0000');
      expect(normalizeHexColor('#fff')).toBe('#ffffff');
    });

    it('should handle mixed case hex colors', () => {
      expect(normalizeHexColor('#FfFfFf')).toBe('#ffffff');
      expect(normalizeHexColor('#FFffFF')).toBe('#ffffff');
    });
  });

  describe('mixHexWithBase', () => {
    it('should return base color when color is null', () => {
      expect(mixHexWithBase(null, 'var(--base-color)')).toBe('var(--base-color)');
    });

    it('should return base color when color is undefined', () => {
      expect(mixHexWithBase(undefined, 'var(--base-color)')).toBe('var(--base-color)');
    });

    it('should return base color for invalid color', () => {
      expect(mixHexWithBase('invalid', 'var(--base-color)')).toBe('var(--base-color)');
    });

    it('should generate color-mix with default accent percent (16%)', () => {
      const result = mixHexWithBase('#ff0000', 'var(--base)');
      expect(result).toContain('color-mix(in srgb');
      expect(result).toContain('var(--base) 84%');
      expect(result).toContain('#ff0000 16%');
    });

    it('should generate color-mix with custom accent percent', () => {
      const result = mixHexWithBase('#ff0000', 'var(--base)', 30);
      expect(result).toContain('var(--base) 70%');
      expect(result).toContain('#ff0000 30%');
    });

    it('should clamp accent percent to 0-100 range (below 0)', () => {
      const result = mixHexWithBase('#ff0000', 'var(--base)', -50);
      expect(result).toContain('var(--base) 100%');
      expect(result).toContain('#ff0000 0%');
    });

    it('should clamp accent percent to 0-100 range (above 100)', () => {
      const result = mixHexWithBase('#ff0000', 'var(--base)', 150);
      expect(result).toContain('var(--base) 0%');
      expect(result).toContain('#ff0000 100%');
    });

    it('should handle edge percent values', () => {
      const result0 = mixHexWithBase('#ff0000', 'var(--base)', 0);
      expect(result0).toContain('var(--base) 100%');
      expect(result0).toContain('#ff0000 0%');

      const result100 = mixHexWithBase('#ff0000', 'var(--base)', 100);
      expect(result100).toContain('var(--base) 0%');
      expect(result100).toContain('#ff0000 100%');
    });

    it('should normalize short hex color before mixing', () => {
      const result = mixHexWithBase('#F00', 'var(--base)', 20);
      expect(result).toContain('#ff0000');
      expect(result).toContain('var(--base) 80%');
      expect(result).toContain('#ff0000 20%');
    });

    it('should work with various CSS variable formats', () => {
      const result1 = mixHexWithBase('#00ff00', 'var(--primary)');
      expect(result1).toContain('var(--primary)');

      const result2 = mixHexWithBase('#00ff00', 'var(--p-content-background)');
      expect(result2).toContain('var(--p-content-background)');

      const result3 = mixHexWithBase('#00ff00', 'rgba(255, 255, 255, 0.5)');
      expect(result3).toContain('rgba(255, 255, 255, 0.5)');
    });

    it('should handle whitespace in color input', () => {
      const result = mixHexWithBase('  #ff0000  ', 'var(--base)', 25);
      expect(result).toContain('#ff0000 25%');
      expect(result).toContain('var(--base) 75%');
    });
  });
});

