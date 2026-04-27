const HEX_COLOR_PATTERN = /^#([\da-fA-F]{3}|[\da-fA-F]{6})$/;

export function normalizeHexColor(color: string | null | undefined) {
  const value = color?.trim();
  if (!value || !HEX_COLOR_PATTERN.test(value)) {
    return null;
  }

  if (value.length === 4) {
    const [, r, g, b] = value
    return `#${r}${r}${g}${g}${b}${b}`.toLowerCase();
  }

  return value.toLowerCase();
}

export function mixHexWithBase(
  color: string | null | undefined,
  baseColor: string,
  accentPercent = 16,
) {
  const normalized = normalizeHexColor(color);
  if (!normalized) {
    return baseColor;
  }

  const safeAccentPercent = Math.max(0, Math.min(100, accentPercent));
  const basePercent = 100 - safeAccentPercent;

  return `color-mix(in srgb, ${baseColor} ${basePercent}%, ${normalized} ${safeAccentPercent}%)`;
}
