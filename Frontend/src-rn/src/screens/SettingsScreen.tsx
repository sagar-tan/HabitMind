import React, { useState } from 'react';
import {
    View,
    Text,
    Pressable,
    ScrollView,
    StyleSheet,
    Alert,
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {
    ArrowLeft, Bell, Download, Upload, Palette,
    RotateCcw, ChevronRight, Shield, Info, Sun, Moon,
} from 'lucide-react-native';
import { useNavigation } from '@react-navigation/native';
import { useApp } from '../context/AppContext';
import { useTheme } from '../theme/ThemeContext';
import { Switch } from '../components/ui/Switch';
import { borderRadius, fontSize, spacing } from '../theme';

export function SettingsScreen() {
    const navigation = useNavigation();
    const { setOnboarded } = useApp();
    const { colors, mode, toggleTheme } = useTheme();
    const [notifications, setNotifications] = useState(true);

    const handleExportData = async () => {
        try {
            const data = await AsyncStorage.getItem('habitmind-app-state');
            if (data) {
                // In React Native, sharing would be done differently
                // For now, just show an alert with success
                Alert.alert('Export', 'Data export functionality requires additional setup for file sharing.');
            }
        } catch (error) {
            Alert.alert('Error', 'Failed to export data');
        }
    };

    const handleImportData = () => {
        Alert.alert('Import', 'Data import functionality requires additional setup for file picking.');
    };

    const handleResetApp = () => {
        Alert.alert(
            'Reset App',
            'This will delete all your data. Are you sure?',
            [
                { text: 'Cancel', style: 'cancel' },
                {
                    text: 'Reset',
                    style: 'destructive',
                    onPress: async () => {
                        await AsyncStorage.removeItem('habitmind-app-state');
                        setOnboarded(false);
                    },
                },
            ]
        );
    };

    const settingGroups = [
        {
            title: 'Preferences',
            items: [
                {
                    icon: Bell,
                    label: 'Notifications',
                    description: 'Reminders and weekly reviews',
                    toggle: true,
                    value: notifications,
                    onChange: () => setNotifications(!notifications),
                },
                {
                    icon: mode === 'dark' ? Moon : Sun,
                    label: 'Theme',
                    description: mode === 'dark' ? 'Dark mode' : 'Light mode',
                    toggle: true,
                    value: mode === 'dark',
                    onChange: toggleTheme,
                },
            ],
        },
        {
            title: 'Data',
            items: [
                {
                    icon: Download,
                    label: 'Export Data',
                    description: 'Download all your data',
                    action: true,
                    onClick: handleExportData,
                },
                {
                    icon: Upload,
                    label: 'Import Data',
                    description: 'Restore from a backup',
                    action: true,
                    onClick: handleImportData,
                },
            ],
        },
        {
            title: 'About',
            items: [
                {
                    icon: Shield,
                    label: 'Privacy',
                    description: 'All data stored locally on device',
                    action: true,
                },
                {
                    icon: Info,
                    label: 'About HabitMind',
                    description: 'Version 1.0.0',
                    action: true,
                },
            ],
        },
        {
            title: 'Danger Zone',
            items: [
                {
                    icon: RotateCcw,
                    label: 'Reset App',
                    description: 'Delete all data and start fresh',
                    destructive: true,
                    onClick: handleResetApp,
                },
            ],
        },
    ];

    return (
        <ScrollView
            style={[styles.container, { backgroundColor: colors.background }]}
            contentContainerStyle={styles.scrollContent}
        >
            {/* Header */}
            <View style={styles.header}>
                <Pressable
                    style={[styles.backButton, { backgroundColor: colors.surface }]}
                    onPress={() => navigation.goBack()}
                >
                    <ArrowLeft size={20} color={colors.foreground} />
                </Pressable>
                <Text style={[styles.title, { color: colors.foreground }]}>Settings</Text>
                <View style={styles.placeholder} />
            </View>

            {/* Setting Groups */}
            {settingGroups.map((group) => (
                <View key={group.title} style={styles.group}>
                    <Text style={[styles.groupTitle, { color: colors.mutedForeground }]}>
                        {group.title}
                    </Text>
                    <View style={[styles.groupCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
                        {group.items.map((item, index) => (
                            <Pressable
                                key={item.label}
                                style={[
                                    styles.settingRow,
                                    index < group.items.length - 1 && { borderBottomWidth: 1, borderBottomColor: colors.border },
                                ]}
                                onPress={item.onClick}
                                disabled={item.toggle}
                            >
                                <View
                                    style={[
                                        styles.iconContainer,
                                        { backgroundColor: item.destructive ? `${colors.destructive}15` : colors.surface },
                                    ]}
                                >
                                    <item.icon
                                        size={18}
                                        color={item.destructive ? colors.destructive : colors.mutedForeground}
                                    />
                                </View>
                                <View style={styles.settingInfo}>
                                    <Text
                                        style={[
                                            styles.settingLabel,
                                            { color: item.destructive ? colors.destructive : colors.foreground },
                                        ]}
                                    >
                                        {item.label}
                                    </Text>
                                    {item.description && (
                                        <Text style={[styles.settingDescription, { color: colors.mutedForeground }]}>
                                            {item.description}
                                        </Text>
                                    )}
                                </View>
                                {item.toggle ? (
                                    <Switch
                                        value={item.value}
                                        onValueChange={item.onChange}
                                    />
                                ) : (
                                    <ChevronRight size={16} color={`${colors.mutedForeground}80`} />
                                )}
                            </Pressable>
                        ))}
                    </View>
                </View>
            ))}
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    scrollContent: {
        padding: spacing.lg,
        paddingBottom: spacing['5xl'],
    },
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: spacing.xl,
    },
    backButton: {
        width: 40,
        height: 40,
        borderRadius: borderRadius.xl,
        alignItems: 'center',
        justifyContent: 'center',
    },
    title: {
        fontSize: fontSize.lg,
        fontWeight: '600',
    },
    placeholder: {
        width: 40,
    },
    group: {
        marginBottom: spacing.xl,
    },
    groupTitle: {
        fontSize: fontSize.xs,
        textTransform: 'uppercase',
        letterSpacing: 1,
        marginBottom: spacing.sm,
        paddingLeft: spacing.xs,
    },
    groupCard: {
        borderRadius: borderRadius['2xl'],
        borderWidth: 1,
        overflow: 'hidden',
    },
    settingRow: {
        flexDirection: 'row',
        alignItems: 'center',
        padding: spacing.lg,
        gap: spacing.md,
    },
    iconContainer: {
        width: 36,
        height: 36,
        borderRadius: borderRadius.xl,
        alignItems: 'center',
        justifyContent: 'center',
    },
    settingInfo: {
        flex: 1,
    },
    settingLabel: {
        fontSize: fontSize.sm,
        fontWeight: '500',
    },
    settingDescription: {
        fontSize: fontSize.xs,
        marginTop: 2,
    },
});
