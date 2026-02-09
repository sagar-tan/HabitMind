import React from 'react';
import { Pressable, Text, StyleSheet, ViewStyle, TextStyle, PressableProps } from 'react-native';
import { useTheme } from '../../theme/ThemeContext';
import { borderRadius, fontSize, spacing } from '../../theme';

type ButtonVariant = 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link';
type ButtonSize = 'default' | 'sm' | 'lg' | 'icon';

interface ButtonProps extends Omit<PressableProps, 'style'> {
    variant?: ButtonVariant;
    size?: ButtonSize;
    children: React.ReactNode;
    style?: ViewStyle;
    textStyle?: TextStyle;
}

export function Button({
    variant = 'default',
    size = 'default',
    children,
    disabled,
    style,
    textStyle,
    ...props
}: ButtonProps) {
    const { colors } = useTheme();

    const variantStyles: Record<ButtonVariant, { container: ViewStyle; text: TextStyle }> = {
        default: {
            container: { backgroundColor: colors.primary },
            text: { color: colors.primaryForeground },
        },
        destructive: {
            container: { backgroundColor: colors.destructive },
            text: { color: colors.destructiveForeground },
        },
        outline: {
            container: { backgroundColor: 'transparent', borderWidth: 1, borderColor: colors.border },
            text: { color: colors.foreground },
        },
        secondary: {
            container: { backgroundColor: colors.muted },
            text: { color: colors.foreground },
        },
        ghost: {
            container: { backgroundColor: 'transparent' },
            text: { color: colors.foreground },
        },
        link: {
            container: { backgroundColor: 'transparent' },
            text: { color: colors.primary, textDecorationLine: 'underline' },
        },
    };

    const sizeStyles: Record<ButtonSize, { container: ViewStyle; text: TextStyle }> = {
        default: {
            container: { height: 40, paddingHorizontal: spacing.lg },
            text: { fontSize: fontSize.base },
        },
        sm: {
            container: { height: 36, paddingHorizontal: spacing.md },
            text: { fontSize: fontSize.sm },
        },
        lg: {
            container: { height: 44, paddingHorizontal: spacing['2xl'] },
            text: { fontSize: fontSize.md },
        },
        icon: {
            container: { height: 40, width: 40, paddingHorizontal: 0 },
            text: { fontSize: fontSize.base },
        },
    };

    const currentVariant = variantStyles[variant];
    const currentSize = sizeStyles[size];

    return (
        <Pressable
            style={({ pressed }) => [
                styles.base,
                currentVariant.container,
                currentSize.container,
                pressed && styles.pressed,
                disabled && styles.disabled,
                style,
            ]}
            disabled={disabled}
            {...props}
        >
            {typeof children === 'string' ? (
                <Text style={[styles.text, currentVariant.text, currentSize.text, textStyle]}>{children}</Text>
            ) : (
                children
            )}
        </Pressable>
    );
}

const styles = StyleSheet.create({
    base: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        borderRadius: borderRadius.lg,
        gap: spacing.sm,
    },
    text: {
        fontWeight: '500',
    },
    pressed: {
        opacity: 0.8,
    },
    disabled: {
        opacity: 0.5,
    },
});
