import React from 'react';
import { Text as RNText, StyleSheet, TextProps as RNTextProps, TextStyle } from 'react-native';
import { useTheme } from '../../theme/ThemeContext';
import { fontSize } from '../../theme';

interface LabelProps extends RNTextProps {
    children: React.ReactNode;
}

export function Label({ children, style, ...props }: LabelProps) {
    const { colors } = useTheme();

    return (
        <RNText style={[styles.label, { color: colors.foreground }, style]} {...props}>
            {children}
        </RNText>
    );
}

const styles = StyleSheet.create({
    label: {
        fontSize: fontSize.base,
        fontWeight: '500',
    },
});
