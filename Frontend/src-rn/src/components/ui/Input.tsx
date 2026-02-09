import React from 'react';
import { TextInput as RNTextInput, StyleSheet, TextInputProps as RNTextInputProps, ViewStyle } from 'react-native';
import { useTheme } from '../../theme/ThemeContext';
import { borderRadius, fontSize, spacing } from '../../theme';

interface InputProps extends RNTextInputProps {
    containerStyle?: ViewStyle;
}

export function Input({ style, containerStyle, ...props }: InputProps) {
    const { colors } = useTheme();

    return (
        <RNTextInput
            style={[
                styles.input,
                {
                    backgroundColor: colors.background,
                    borderColor: colors.border,
                    color: colors.foreground,
                },
                style,
            ]}
            placeholderTextColor={colors.mutedForeground}
            {...props}
        />
    );
}

const styles = StyleSheet.create({
    input: {
        height: 40,
        borderWidth: 1,
        borderRadius: borderRadius.lg,
        paddingHorizontal: spacing.md,
        fontSize: fontSize.base,
    },
});
