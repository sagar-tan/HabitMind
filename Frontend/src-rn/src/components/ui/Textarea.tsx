import React from 'react';
import { TextInput as RNTextInput, StyleSheet, TextInputProps as RNTextInputProps, ViewStyle } from 'react-native';
import { useTheme } from '../../theme/ThemeContext';
import { borderRadius, fontSize, spacing } from '../../theme';

interface TextareaProps extends RNTextInputProps {
    containerStyle?: ViewStyle;
}

export function Textarea({ style, containerStyle, ...props }: TextareaProps) {
    const { colors } = useTheme();

    return (
        <RNTextInput
            style={[
                styles.textarea,
                {
                    backgroundColor: colors.background,
                    borderColor: colors.border,
                    color: colors.foreground,
                },
                style,
            ]}
            placeholderTextColor={colors.mutedForeground}
            multiline
            textAlignVertical="top"
            {...props}
        />
    );
}

const styles = StyleSheet.create({
    textarea: {
        minHeight: 80,
        borderWidth: 1,
        borderRadius: borderRadius.lg,
        paddingHorizontal: spacing.md,
        paddingVertical: spacing.sm,
        fontSize: fontSize.base,
    },
});
