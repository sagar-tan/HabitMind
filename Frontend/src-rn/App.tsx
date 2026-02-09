import React from 'react';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { NavigationContainer } from '@react-navigation/native';
import { GestureHandlerRootView } from 'react-native-gesture-handler';

import { ThemeProvider, useTheme } from './src/theme/ThemeContext';
import { AppProvider, useApp } from './src/context/AppContext';
import { RootNavigator } from './src/navigation';

// Main App Content
function AppContent() {
    const { mode } = useTheme();
    const { loaded } = useApp();

    if (!loaded) {
        // Could show a splash screen here
        return null;
    }

    return (
        <>
            <StatusBar style={mode === 'dark' ? 'light' : 'dark'} />
            <NavigationContainer>
                <RootNavigator />
            </NavigationContainer>
        </>
    );
}

// Root App with Providers
export default function App() {
    return (
        <GestureHandlerRootView style={{ flex: 1 }}>
            <SafeAreaProvider>
                <ThemeProvider>
                    <AppProvider>
                        <AppContent />
                    </AppProvider>
                </ThemeProvider>
            </SafeAreaProvider>
        </GestureHandlerRootView>
    );
}
