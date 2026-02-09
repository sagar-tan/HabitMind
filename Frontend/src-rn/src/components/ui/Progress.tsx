import React from 'react';
import { View, StyleSheet, ViewStyle, Animated } from 'react-native';
import { useTheme } from '../../theme/ThemeContext';
import { borderRadius } from '../../theme';

interface ProgressProps {
    value: number; // 0-100
    style?: ViewStyle;
    indicatorStyle?: ViewStyle;
}

export function Progress({ value, style, indicatorStyle }: ProgressProps) {
    const { colors } = useTheme();
    const clampedValue = Math.min(100, Math.max(0, value));

    return (
        <View style={[styles.track, { backgroundColor: colors.muted }, style]}>
            <View
                style={[
                    styles.indicator,
                    { backgroundColor: colors.primary, width: `${clampedValue}%` },
                    indicatorStyle,
                ]}
            />
        </View>
    );
}

const styles = StyleSheet.create({
    track: {
        height: 8,
        borderRadius: borderRadius.full,
        overflow: 'hidden',
    },
    indicator: {
        height: '100%',
        borderRadius: borderRadius.full,
    },
});
