/**
 * PLACEHOLDER: Sheet / Bottom Drawer Component
 * 
 * Web version uses vaul (drawer) + radix sheet
 * 
 * For React Native, implement using:
 * - @gorhom/bottom-sheet (recommended)
 * 
 * Installation:
 * npx expo install @gorhom/bottom-sheet react-native-reanimated react-native-gesture-handler
 * 
 * This is a basic placeholder using Modal. Replace with BottomSheet for production.
 */

import React from 'react';
import { View, Modal, Pressable, StyleSheet, Dimensions } from 'react-native';
import { useTheme } from '../../theme/ThemeContext';
import { borderRadius, spacing } from '../../theme';

const { height: SCREEN_HEIGHT } = Dimensions.get('window');

interface SheetProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    children: React.ReactNode;
    snapPoint?: '25%' | '50%' | '75%' | '90%';
}

export function Sheet({ open, onOpenChange, children, snapPoint = '50%' }: SheetProps) {
    const { colors } = useTheme();

    const heightMap = {
        '25%': SCREEN_HEIGHT * 0.25,
        '50%': SCREEN_HEIGHT * 0.5,
        '75%': SCREEN_HEIGHT * 0.75,
        '90%': SCREEN_HEIGHT * 0.9,
    };

    return (
        <Modal visible={open} transparent animationType="slide" onRequestClose={() => onOpenChange(false)}>
            <View style={styles.overlay}>
                <Pressable style={styles.backdrop} onPress={() => onOpenChange(false)} />
                <View style={[styles.content, { backgroundColor: colors.card, height: heightMap[snapPoint] }]}>
                    <View style={[styles.handle, { backgroundColor: colors.border }]} />
                    {children}
                </View>
            </View>
        </Modal>
    );
}

interface SheetContentProps {
    children: React.ReactNode;
}

export function SheetContent({ children }: SheetContentProps) {
    return <View style={styles.body}>{children}</View>;
}

const styles = StyleSheet.create({
    overlay: {
        flex: 1,
        justifyContent: 'flex-end',
    },
    backdrop: {
        ...StyleSheet.absoluteFillObject,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
    },
    content: {
        borderTopLeftRadius: borderRadius['2xl'],
        borderTopRightRadius: borderRadius['2xl'],
        paddingTop: spacing.sm,
    },
    handle: {
        width: 36,
        height: 4,
        borderRadius: 2,
        alignSelf: 'center',
        marginBottom: spacing.md,
    },
    body: {
        flex: 1,
        padding: spacing.lg,
    },
});
