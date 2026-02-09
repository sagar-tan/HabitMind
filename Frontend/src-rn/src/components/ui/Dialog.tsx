/**
 * PLACEHOLDER: Dialog / Modal Component
 * 
 * Web version uses @radix-ui/react-dialog
 * 
 * For React Native, implement using one of:
 * - @gorhom/bottom-sheet (recommended for mobile UX)
 * - react-native Modal component
 * - react-native-modal
 * 
 * Installation:
 * npx expo install @gorhom/bottom-sheet react-native-reanimated react-native-gesture-handler
 * 
 * Example usage of BottomSheet:
 * 
 * import BottomSheet from '@gorhom/bottom-sheet';
 * 
 * const snapPoints = ['25%', '50%', '90%'];
 * 
 * <BottomSheet snapPoints={snapPoints}>
 *   <View>
 *     <Text>Modal content here</Text>
 *   </View>
 * </BottomSheet>
 */

import React from 'react';
import { View, Text, Modal, Pressable, StyleSheet, ViewStyle } from 'react-native';
import { X } from 'lucide-react-native';
import { useTheme } from '../../theme/ThemeContext';
import { borderRadius, fontSize, spacing } from '../../theme';

interface DialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    children: React.ReactNode;
}

export function Dialog({ open, onOpenChange, children }: DialogProps) {
    const { colors } = useTheme();

    return (
        <Modal visible={open} transparent animationType="fade" onRequestClose={() => onOpenChange(false)}>
            <View style={styles.overlay}>
                <Pressable style={styles.backdrop} onPress={() => onOpenChange(false)} />
                <View style={[styles.content, { backgroundColor: colors.card }]}>{children}</View>
            </View>
        </Modal>
    );
}

interface DialogHeaderProps {
    children: React.ReactNode;
    onClose?: () => void;
}

export function DialogHeader({ children, onClose }: DialogHeaderProps) {
    const { colors } = useTheme();
    return (
        <View style={styles.header}>
            <Text style={[styles.title, { color: colors.foreground }]}>{children}</Text>
            {onClose && (
                <Pressable onPress={onClose} style={styles.closeButton}>
                    <X size={20} color={colors.mutedForeground} />
                </Pressable>
            )}
        </View>
    );
}

interface DialogContentProps {
    children: React.ReactNode;
    style?: ViewStyle;
}

export function DialogContent({ children, style }: DialogContentProps) {
    return <View style={[styles.body, style]}>{children}</View>;
}

interface DialogFooterProps {
    children: React.ReactNode;
}

export function DialogFooter({ children }: DialogFooterProps) {
    return <View style={styles.footer}>{children}</View>;
}

const styles = StyleSheet.create({
    overlay: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    backdrop: {
        ...StyleSheet.absoluteFillObject,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
    },
    content: {
        width: '85%',
        maxWidth: 400,
        borderRadius: borderRadius.xl,
        overflow: 'hidden',
    },
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: spacing.lg,
        paddingBottom: spacing.sm,
    },
    title: {
        fontSize: fontSize.lg,
        fontWeight: '600',
        flex: 1,
    },
    closeButton: {
        padding: spacing.xs,
    },
    body: {
        paddingHorizontal: spacing.lg,
        paddingBottom: spacing.lg,
    },
    footer: {
        flexDirection: 'row',
        justifyContent: 'flex-end',
        padding: spacing.lg,
        gap: spacing.sm,
    },
});
