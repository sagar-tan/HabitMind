import React from 'react';
import { Switch as RNSwitch, StyleSheet, SwitchProps as RNSwitchProps } from 'react-native';
import { useTheme } from '../../theme/ThemeContext';

interface SwitchProps extends Omit<RNSwitchProps, 'trackColor' | 'thumbColor' | 'ios_backgroundColor'> {
    checked: boolean;
    onCheckedChange: (checked: boolean) => void;
}

export function Switch({ checked, onCheckedChange, disabled, ...props }: SwitchProps) {
    const { colors } = useTheme();

    return (
        <RNSwitch
            value={checked}
            onValueChange={onCheckedChange}
            disabled={disabled}
            trackColor={{
                false: colors.muted,
                true: colors.primary,
            }}
            thumbColor={checked ? colors.primaryForeground : colors.foreground}
            ios_backgroundColor={colors.muted}
            {...props}
        />
    );
}
