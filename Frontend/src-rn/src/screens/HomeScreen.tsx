import React, { useState, useEffect, useRef } from 'react';
import {
    View,
    Text,
    Pressable,
    ScrollView,
    StyleSheet,
    Animated as RNAnimated,
} from 'react-native';
import {
    Smile, Meh, Frown, Laugh, Annoyed,
    PenLine, ListPlus, StickyNote, Settings, ChevronRight,
    Clock, CheckCircle2,
} from 'lucide-react-native';
import { useNavigation, NavigationProp } from '@react-navigation/native';
import { useApp } from '../context/AppContext';
import { useTheme } from '../theme/ThemeContext';
import { Card } from '../components/ui/Card';
import { Progress } from '../components/ui/Progress';
import { borderRadius, fontSize, spacing } from '../theme';

// Types for navigation - adjust based on your RootStackParamList
type RootStackParamList = {
    HomeTabs: undefined;
    Settings: undefined;
    WeeklyReview: undefined;
    JournalEntry: undefined;
};

const moodIcons = [Frown, Annoyed, Meh, Smile, Laugh];
const moodLabels = ['Rough', 'Low', 'Okay', 'Good', 'Great'];

function formatDate(date: Date) {
    const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return {
        day: days[date.getDay()],
        date: `${months[date.getMonth()]} ${date.getDate()}`,
        full: `${days[date.getDay()]}, ${months[date.getMonth()]} ${date.getDate()}`,
    };
}

// Animated number component
function AnimatedNumber({ value, suffix = '' }: { value: number; suffix?: string }) {
    const [display, setDisplay] = useState(0);

    useEffect(() => {
        const end = value;
        const duration = 600;
        const startTime = Date.now();

        const tick = () => {
            const elapsed = Date.now() - startTime;
            const progress = Math.min(elapsed / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3);
            setDisplay(Math.round(end * eased));
            if (progress < 1) {
                requestAnimationFrame(tick);
            }
        };

        requestAnimationFrame(tick);
    }, [value]);

    return (
        <Text>
            {display}
            {suffix}
        </Text>
    );
}

export function HomeScreen() {
    const navigation = useNavigation<NavigationProp<RootStackParamList>>();
    const { colors } = useTheme();
    const { tasks, habits, mood, setMood } = useApp();

    const now = new Date();
    const dateInfo = formatDate(now);
    const today = now.toISOString().split('T')[0];

    const todayTasks = tasks.filter((t) => t.date === today);
    const completedTasks = todayTasks.filter((t) => t.completed).length;
    const taskProgress =
        todayTasks.length > 0
            ? Math.round(todayTasks.reduce((sum, t) => sum + t.progress, 0) / todayTasks.length)
            : 0;
    const habitsCompleted = habits.filter((h) => h.completedDates.includes(today)).length;

    const currentHour = now.getHours();

    // Simulated schedule blocks
    const scheduleBlocks = [
        { time: '7:00', label: 'Morning routine', done: currentHour > 8 },
        { time: '8:00', label: 'Deep work', done: currentHour > 10 },
        { time: '10:30', label: 'Break & stretch', done: currentHour > 11 },
        { time: '11:00', label: 'Tasks & reviews', done: currentHour > 12 },
        { time: '13:00', label: 'Afternoon block', done: false },
        { time: '15:00', label: 'Exercise', done: false },
        { time: '17:00', label: 'Wind down', done: false },
    ];

    const currentBlockIdx = scheduleBlocks.findIndex((b) => !b.done);

    // Quick actions
    const quickActions = [
        {
            icon: PenLine,
            label: 'Journal',
            action: () => navigation.navigate('JournalEntry' as any),
        },
        {
            icon: ListPlus,
            label: 'Add Task',
            action: () => navigation.navigate('HomeTabs', { screen: 'Plan' } as any),
        },
        {
            icon: StickyNote,
            label: 'Quick Note',
            action: () => navigation.navigate('HomeTabs', { screen: 'Journal' } as any),
        },
    ];

    return (
        <ScrollView
            style={[styles.container, { backgroundColor: colors.background }]}
            contentContainerStyle={styles.contentContainer}
        >
            {/* Header */}
            <View style={styles.header}>
                <View>
                    <Text style={[styles.dayText, { color: colors.mutedForeground }]}>{dateInfo.day}</Text>
                    <Text style={[styles.dateText, { color: colors.foreground }]}>{dateInfo.date}</Text>
                </View>
                <View style={styles.headerRight}>
                    {/* Mood indicator */}
                    <View style={[styles.moodIndicator, { backgroundColor: colors.surface }]}>
                        {(() => {
                            const MoodIcon = moodIcons[mood - 1];
                            return <MoodIcon size={16} color={colors.primary} />;
                        })()}
                        <Text style={[styles.moodLabel, { color: colors.mutedForeground }]}>
                            {moodLabels[mood - 1]}
                        </Text>
                    </View>
                    <Pressable
                        style={[styles.settingsButton, { backgroundColor: colors.surface }]}
                        onPress={() => navigation.navigate('Settings')}
                    >
                        <Settings size={20} color={colors.mutedForeground} />
                    </Pressable>
                </View>
            </View>

            {/* Mood Selector */}
            <Card style={styles.card}>
                <Text style={[styles.sectionLabel, { color: colors.mutedForeground }]}>
                    How are you feeling?
                </Text>
                <View style={styles.moodSelector}>
                    {moodIcons.map((Icon, i) => (
                        <Pressable
                            key={i}
                            style={[
                                styles.moodButton,
                                {
                                    backgroundColor: mood === i + 1 ? `${colors.primary}20` : colors.surface,
                                },
                            ]}
                            onPress={() => setMood(i + 1)}
                        >
                            <Icon
                                size={24}
                                color={mood === i + 1 ? colors.primary : colors.mutedForeground}
                            />
                        </Pressable>
                    ))}
                </View>
            </Card>

            {/* Today Summary Card */}
            <Card style={styles.card}>
                <Text style={[styles.cardTitle, { color: colors.foreground }]}>Today's Progress</Text>
                <View style={styles.statsGrid}>
                    {/* Habits */}
                    <View style={[styles.statItem, { backgroundColor: colors.surface }]}>
                        <View style={styles.statValue}>
                            <Text style={[styles.statNumber, { color: colors.primary }]}>
                                <AnimatedNumber value={habitsCompleted} />
                            </Text>
                            <Text style={[styles.statTotal, { color: colors.mutedForeground }]}>
                                /{habits.length}
                            </Text>
                        </View>
                        <Text style={[styles.statLabel, { color: colors.mutedForeground }]}>Habits</Text>
                    </View>
                    {/* Tasks */}
                    <View style={[styles.statItem, { backgroundColor: colors.surface }]}>
                        <Text style={[styles.statNumber, { color: colors.primary }]}>
                            <AnimatedNumber value={taskProgress} suffix="%" />
                        </Text>
                        <Text style={[styles.statLabel, { color: colors.mutedForeground }]}>Tasks</Text>
                    </View>
                    {/* Done */}
                    <View style={[styles.statItem, { backgroundColor: colors.surface }]}>
                        <View style={styles.statValue}>
                            <Text style={[styles.statNumber, { color: colors.primary }]}>
                                <AnimatedNumber value={completedTasks} />
                            </Text>
                            <Text style={[styles.statTotal, { color: colors.mutedForeground }]}>
                                /{todayTasks.length}
                            </Text>
                        </View>
                        <Text style={[styles.statLabel, { color: colors.mutedForeground }]}>Done</Text>
                    </View>
                </View>

                {/* Overall progress bar */}
                <View style={styles.progressSection}>
                    <View style={styles.progressHeader}>
                        <Text style={[styles.progressLabel, { color: colors.mutedForeground }]}>Overall</Text>
                        <Text style={[styles.progressLabel, { color: colors.mutedForeground }]}>
                            {taskProgress}%
                        </Text>
                    </View>
                    <Progress value={taskProgress} />
                </View>
            </Card>

            {/* Quick Actions */}
            <View style={styles.quickActions}>
                {quickActions.map((item, i) => (
                    <Pressable
                        key={i}
                        style={[styles.quickActionButton, { backgroundColor: colors.card, borderColor: colors.border }]}
                        onPress={item.action}
                    >
                        <item.icon size={20} color={colors.primary} />
                        <Text style={[styles.quickActionLabel, { color: colors.mutedForeground }]}>
                            {item.label}
                        </Text>
                    </Pressable>
                ))}
            </View>

            {/* Schedule Section */}
            <Card style={styles.card}>
                <View style={styles.scheduleHeader}>
                    <Text style={[styles.cardTitle, { color: colors.foreground }]}>Schedule</Text>
                    <View style={styles.timeDisplay}>
                        <Clock size={14} color={colors.mutedForeground} />
                        <Text style={[styles.timeText, { color: colors.mutedForeground }]}>
                            {now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </Text>
                    </View>
                </View>
                <View style={styles.scheduleList}>
                    {scheduleBlocks.map((block, i) => {
                        const isCurrent = i === currentBlockIdx;
                        return (
                            <View
                                key={i}
                                style={[
                                    styles.scheduleItem,
                                    isCurrent && { backgroundColor: `${colors.primary}10`, borderColor: `${colors.primary}30`, borderWidth: 1 },
                                    block.done && styles.scheduleItemDone,
                                ]}
                            >
                                <Text
                                    style={[
                                        styles.scheduleTime,
                                        { color: isCurrent ? colors.primary : colors.mutedForeground },
                                    ]}
                                >
                                    {block.time}
                                </Text>
                                <View
                                    style={[
                                        styles.scheduleDot,
                                        {
                                            backgroundColor: block.done
                                                ? `${colors.primary}80`
                                                : isCurrent
                                                    ? colors.primary
                                                    : `${colors.mutedForeground}50`,
                                        },
                                    ]}
                                />
                                <Text
                                    style={[
                                        styles.scheduleLabel,
                                        {
                                            color: isCurrent
                                                ? colors.foreground
                                                : block.done
                                                    ? colors.mutedForeground
                                                    : `${colors.foreground}B3`,
                                        },
                                        block.done && styles.scheduleLabelDone,
                                    ]}
                                >
                                    {block.label}
                                </Text>
                                {block.done && <CheckCircle2 size={16} color={`${colors.primary}80`} />}
                            </View>
                        );
                    })}
                </View>
            </Card>

            {/* Weekly Review CTA */}
            <Pressable
                style={[styles.weeklyReviewButton, { backgroundColor: `${colors.primary}10`, borderColor: `${colors.primary}25` }]}
                onPress={() => navigation.navigate('WeeklyReview')}
            >
                <View>
                    <Text style={[styles.weeklyReviewTitle, { color: colors.foreground }]}>Weekly Review</Text>
                    <Text style={[styles.weeklyReviewSubtitle, { color: colors.mutedForeground }]}>
                        Reflect and plan ahead
                    </Text>
                </View>
                <ChevronRight size={20} color={colors.primary} />
            </Pressable>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    contentContainer: {
        padding: spacing.lg,
        paddingBottom: spacing['5xl'],
    },
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: spacing.lg,
    },
    dayText: {
        fontSize: fontSize.sm,
    },
    dateText: {
        fontSize: fontSize['2xl'],
        fontWeight: '600',
    },
    headerRight: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.md,
    },
    moodIndicator: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.xs,
        paddingVertical: spacing.xs,
        paddingHorizontal: spacing.md,
        borderRadius: borderRadius.full,
    },
    moodLabel: {
        fontSize: fontSize.xs,
    },
    settingsButton: {
        width: 40,
        height: 40,
        borderRadius: borderRadius.xl,
        alignItems: 'center',
        justifyContent: 'center',
    },
    card: {
        marginBottom: spacing.lg,
    },
    sectionLabel: {
        fontSize: fontSize.sm,
        marginBottom: spacing.md,
        paddingHorizontal: spacing.lg,
        paddingTop: spacing.lg,
    },
    moodSelector: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        paddingHorizontal: spacing.lg,
        paddingBottom: spacing.lg,
    },
    moodButton: {
        width: 48,
        height: 48,
        borderRadius: borderRadius.xl,
        alignItems: 'center',
        justifyContent: 'center',
    },
    cardTitle: {
        fontSize: fontSize.md,
        fontWeight: '600',
        marginBottom: spacing.lg,
        paddingHorizontal: spacing.lg,
        paddingTop: spacing.lg,
    },
    statsGrid: {
        flexDirection: 'row',
        gap: spacing.md,
        paddingHorizontal: spacing.lg,
    },
    statItem: {
        flex: 1,
        borderRadius: borderRadius.xl,
        padding: spacing.md,
        alignItems: 'center',
    },
    statValue: {
        flexDirection: 'row',
        alignItems: 'baseline',
    },
    statNumber: {
        fontSize: fontSize['2xl'],
        fontWeight: '600',
    },
    statTotal: {
        fontSize: fontSize.sm,
    },
    statLabel: {
        fontSize: fontSize.xs,
        marginTop: spacing.xs,
    },
    progressSection: {
        paddingHorizontal: spacing.lg,
        paddingBottom: spacing.lg,
        marginTop: spacing.lg,
    },
    progressHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: spacing.xs,
    },
    progressLabel: {
        fontSize: fontSize.xs,
    },
    quickActions: {
        flexDirection: 'row',
        gap: spacing.md,
        marginBottom: spacing.lg,
    },
    quickActionButton: {
        flex: 1,
        borderRadius: borderRadius['2xl'],
        borderWidth: 1,
        padding: spacing.lg,
        alignItems: 'center',
        gap: spacing.sm,
    },
    quickActionLabel: {
        fontSize: fontSize.xs,
    },
    scheduleHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingRight: spacing.lg,
    },
    timeDisplay: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.xs,
    },
    timeText: {
        fontSize: fontSize.xs,
    },
    scheduleList: {
        paddingHorizontal: spacing.lg,
        paddingBottom: spacing.lg,
        gap: spacing.xs,
    },
    scheduleItem: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingVertical: spacing.sm,
        paddingHorizontal: spacing.md,
        borderRadius: borderRadius.xl,
        gap: spacing.md,
    },
    scheduleItemDone: {
        opacity: 0.5,
    },
    scheduleTime: {
        fontSize: fontSize.xs,
        width: 48,
    },
    scheduleDot: {
        width: 8,
        height: 8,
        borderRadius: 4,
    },
    scheduleLabel: {
        flex: 1,
        fontSize: fontSize.sm,
    },
    scheduleLabelDone: {
        textDecorationLine: 'line-through',
    },
    weeklyReviewButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: spacing.lg,
        borderRadius: borderRadius['2xl'],
        borderWidth: 1,
    },
    weeklyReviewTitle: {
        fontSize: fontSize.sm,
        fontWeight: '500',
    },
    weeklyReviewSubtitle: {
        fontSize: fontSize.xs,
        marginTop: spacing.xs,
    },
});
