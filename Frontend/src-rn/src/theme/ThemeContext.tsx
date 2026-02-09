import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { useColorScheme } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { colors, ThemeColors, ThemeMode } from './index';

interface ThemeContextType {
    theme: ThemeMode;
    colors: ThemeColors;
    setTheme: (t: ThemeMode) => void;
    toggleTheme: () => void;
}

const ThemeContext = createContext<ThemeContextType | null>(null);

const THEME_KEY = 'habitmind-theme';

export function ThemeProvider({ children }: { children: React.ReactNode }) {
    const systemTheme = useColorScheme();
    const [theme, setThemeState] = useState<ThemeMode>('dark');

    useEffect(() => {
        AsyncStorage.getItem(THEME_KEY).then((saved) => {
            if (saved === 'light' || saved === 'dark') {
                setThemeState(saved);
            } else if (systemTheme) {
                setThemeState(systemTheme);
            }
        });
    }, [systemTheme]);

    const setTheme = useCallback((t: ThemeMode) => {
        setThemeState(t);
        AsyncStorage.setItem(THEME_KEY, t);
    }, []);

    const toggleTheme = useCallback(() => {
        setThemeState((prev) => {
            const next = prev === 'dark' ? 'light' : 'dark';
            AsyncStorage.setItem(THEME_KEY, next);
            return next;
        });
    }, []);

    return (
        <ThemeContext.Provider
            value={{
                theme,
                colors: colors[theme],
                setTheme,
                toggleTheme,
            }}
        >
            {children}
        </ThemeContext.Provider>
    );
}

export function useTheme() {
    const ctx = useContext(ThemeContext);
    if (!ctx) throw new Error('useTheme must be used within ThemeProvider');
    return ctx;
}
