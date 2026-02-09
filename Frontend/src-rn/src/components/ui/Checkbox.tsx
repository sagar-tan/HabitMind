import React from 'react';
import { Pressable, View, StyleSheet, ViewStyle } from 'react-native';
import { Check } from 'lucide-react-native';
import { useTheme } from '../../theme/ThemeContext';
import { borderRadius, spacing } from '../../theme';

interface CheckboxProps {
    checked: boolean;
    onCheckedChange: (checked: boolean) => void;
    disabled?: boolean;
    style?: ViewStyle;
}

export function Checkbox({ checked, onCheckedChange, disabled, style }: CheckboxProps) {
    const { colors } = useTheme();

    return (
        <Pressable
            style={[
                styles.checkbox,
                {
                    backgroundColor: checked ? colors.primary : 'transparent',
                    borderColor: checked ? colors.primary : colors.border,
                },
                disabled && styles.disabled,
                style,
            ]}
            onPress={() => !disabled && onCheckedChange(!checked)}
            disabled={disabled}
        >
            {checked && <Check size={14} color={colors.primaryForeground} strokeWidth={3} />}
        </Pressable>
    );
}

const styles = StyleSheet.create({
    checkbox: {
        width: 20,
        height: 20,
        borderWidth: 2,
        borderRadius: borderRadius.sm,
        alignItems: 'center',
        justifyContent: 'center',
    },
    disabled: {
        opacity: 0.5,
    },
});
