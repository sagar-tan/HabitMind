/**
 * Theme system replacing CSS variables
 * Usage: const { colors } = useTheme();
 */

export const colors = {
  light: {
    background: '#ffffff',
    foreground: '#0a0a0a',
    primary: '#6366f1',
    primaryForeground: '#ffffff',
    muted: '#f4f4f5',
    mutedForeground: '#71717a',
    border: '#e4e4e7',
    surface: '#fafafa',
    card: '#ffffff',
    cardForeground: '#0a0a0a',
    destructive: '#ef4444',
    destructiveForeground: '#ffffff',
    success: '#22c55e',
    warning: '#f59e0b',
    navBg: 'rgba(255, 255, 255, 0.85)',
    navBorder: 'rgba(0, 0, 0, 0.06)',
  },
  dark: {
    background: '#0a0a0a',
    foreground: '#fafafa',
    primary: '#818cf8',
    primaryForeground: '#0a0a0a',
    muted: '#27272a',
    mutedForeground: '#a1a1aa',
    border: '#3f3f46',
    surface: '#18181b',
    card: '#18181b',
    cardForeground: '#fafafa',
    destructive: '#dc2626',
    destructiveForeground: '#fafafa',
    success: '#22c55e',
    warning: '#f59e0b',
    navBg: 'rgba(10, 10, 10, 0.85)',
    navBorder: 'rgba(255, 255, 255, 0.06)',
  },
} as const;

export type ThemeColors = typeof colors.light;
export type ThemeMode = 'light' | 'dark';

export const spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  '2xl': 24,
  '3xl': 32,
  '4xl': 40,
  '5xl': 48,
} as const;

export const fontSize = {
  xs: 10,
  sm: 12,
  base: 14,
  md: 16,
  lg: 18,
  xl: 20,
  '2xl': 24,
  '3xl': 30,
} as const;

export const borderRadius = {
  sm: 4,
  md: 8,
  lg: 12,
  xl: 16,
  '2xl': 20,
  full: 9999,
} as const;
