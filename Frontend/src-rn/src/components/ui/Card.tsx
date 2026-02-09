import React from 'react';
import { View, Text, StyleSheet, ViewStyle, TextStyle } from 'react-native';
import { useTheme } from '../../theme/ThemeContext';
import { borderRadius, fontSize, spacing } from '../../theme';

interface CardProps {
    children: React.ReactNode;
    style?: ViewStyle;
}

export function Card({ children, style }: CardProps) {
    const { colors } = useTheme();
    return (
        <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }, style]}>
            {children}
        </View>
    );
}

interface CardHeaderProps {
    children: React.ReactNode;
    style?: ViewStyle;
}

export function CardHeader({ children, style }: CardHeaderProps) {
    return <View style={[styles.header, style]}>{children}</View>;
}

interface CardTitleProps {
    children: React.ReactNode;
    style?: TextStyle;
}

export function CardTitle({ children, style }: CardTitleProps) {
    const { colors } = useTheme();
    return <Text style={[styles.title, { color: colors.cardForeground }, style]}>{children}</Text>;
}

interface CardDescriptionProps {
    children: React.ReactNode;
    style?: TextStyle;
}

export function CardDescription({ children, style }: CardDescriptionProps) {
    const { colors } = useTheme();
    return <Text style={[styles.description, { color: colors.mutedForeground }, style]}>{children}</Text>;
}

interface CardContentProps {
    children: React.ReactNode;
    style?: ViewStyle;
}

export function CardContent({ children, style }: CardContentProps) {
    return <View style={[styles.content, style]}>{children}</View>;
}

interface CardFooterProps {
    children: React.ReactNode;
    style?: ViewStyle;
}

export function CardFooter({ children, style }: CardFooterProps) {
    return <View style={[styles.footer, style]}>{children}</View>;
}

const styles = StyleSheet.create({
    card: {
        borderRadius: borderRadius.xl,
        borderWidth: 1,
        overflow: 'hidden',
    },
    header: {
        padding: spacing.lg,
        gap: spacing.xs,
    },
    title: {
        fontSize: fontSize.lg,
        fontWeight: '600',
    },
    description: {
        fontSize: fontSize.sm,
    },
    content: {
        paddingHorizontal: spacing.lg,
        paddingBottom: spacing.lg,
    },
    footer: {
        flexDirection: 'row',
        alignItems: 'center',
        padding: spacing.lg,
        paddingTop: 0,
    },
});
