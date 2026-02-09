import React, { createContext, useContext, useState } from 'react';
import { View, Pressable, Text, StyleSheet, ViewStyle, TextStyle, ScrollView } from 'react-native';
import { useTheme } from '../../theme/ThemeContext';
import { borderRadius, fontSize, spacing } from '../../theme';

// Context for tabs
interface TabsContextType {
    value: string;
    onValueChange: (value: string) => void;
}

const TabsContext = createContext<TabsContextType | null>(null);

function useTabs() {
    const ctx = useContext(TabsContext);
    if (!ctx) throw new Error('Tabs components must be used within Tabs');
    return ctx;
}

// Root Tabs component
interface TabsProps {
    value: string;
    onValueChange: (value: string) => void;
    children: React.ReactNode;
    style?: ViewStyle;
}

export function Tabs({ value, onValueChange, children, style }: TabsProps) {
    return (
        <TabsContext.Provider value={{ value, onValueChange }}>
            <View style={[styles.tabs, style]}>{children}</View>
        </TabsContext.Provider>
    );
}

// TabsList - container for triggers
interface TabsListProps {
    children: React.ReactNode;
    style?: ViewStyle;
}

export function TabsList({ children, style }: TabsListProps) {
    const { colors } = useTheme();
    return (
        <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={[styles.tabsList, { backgroundColor: colors.muted }, style]}
        >
            {children}
        </ScrollView>
    );
}

// TabsTrigger - individual tab button
interface TabsTriggerProps {
    value: string;
    children: React.ReactNode;
    style?: ViewStyle;
    textStyle?: TextStyle;
}

export function TabsTrigger({ value, children, style, textStyle }: TabsTriggerProps) {
    const { colors } = useTheme();
    const { value: activeValue, onValueChange } = useTabs();
    const isActive = activeValue === value;

    return (
        <Pressable
            style={[
                styles.trigger,
                isActive && { backgroundColor: colors.background },
                style,
            ]}
            onPress={() => onValueChange(value)}
        >
            <Text
                style={[
                    styles.triggerText,
                    { color: isActive ? colors.foreground : colors.mutedForeground },
                    textStyle,
                ]}
            >
                {children}
            </Text>
        </Pressable>
    );
}

// TabsContent - content panel
interface TabsContentProps {
    value: string;
    children: React.ReactNode;
    style?: ViewStyle;
}

export function TabsContent({ value, children, style }: TabsContentProps) {
    const { value: activeValue } = useTabs();
    if (activeValue !== value) return null;
    return <View style={[styles.content, style]}>{children}</View>;
}

const styles = StyleSheet.create({
    tabs: {
        flex: 1,
    },
    tabsList: {
        flexDirection: 'row',
        padding: spacing.xs,
        borderRadius: borderRadius.lg,
        gap: spacing.xs,
    },
    trigger: {
        paddingHorizontal: spacing.md,
        paddingVertical: spacing.sm,
        borderRadius: borderRadius.md,
    },
    triggerText: {
        fontSize: fontSize.sm,
        fontWeight: '500',
    },
    content: {
        flex: 1,
        marginTop: spacing.md,
    },
});
