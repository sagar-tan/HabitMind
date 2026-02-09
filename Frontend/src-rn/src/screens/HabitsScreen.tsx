import React, { useState } from 'react';
import {
    View,
    Text,
    Pressable,
    ScrollView,
    StyleSheet,
    FlatList,
} from 'react-native';
import {
    Brain, Dumbbell, BookOpen, Droplets, Moon, PenLine,
    Flame, Camera, ArrowLeft, Check,
} from 'lucide-react-native';
import { useNavigation } from '@react-navigation/native';
import { useApp, Habit } from '../context/AppContext';
import { useTheme } from '../theme/ThemeContext';
import { Card } from '../components/ui/Card';
import { Progress } from '../components/ui/Progress';
import { borderRadius, fontSize, spacing } from '../theme';

// Icon map
const iconMap: Record<string, React.ComponentType<any>> = {
    brain: Brain,
    dumbbell: Dumbbell,
    'book-open': BookOpen,
    droplets: Droplets,
    moon: Moon,
    'pen-line': PenLine,
};

interface HabitCardProps {
    habit: Habit;
    isCompleted: boolean;
    onToggle: () => void;
    onSelect: () => void;
}

function HabitCard({ habit, isCompleted, onToggle, onSelect }: HabitCardProps) {
    const { colors } = useTheme();
    const Icon = iconMap[habit.icon] || Brain;

    return (
        <Card style={styles.habitCard}>
            <View style={styles.habitHeader}>
                <Pressable style={styles.habitInfo} onPress={onSelect}>
                    <View
                        style={[
                            styles.habitIcon,
                            { backgroundColor: isCompleted ? `${colors.primary}20` : colors.surface },
                        ]}
                    >
                        <Icon
                            size={20}
                            color={isCompleted ? colors.primary : colors.mutedForeground}
                        />
                    </View>
                    <View>
                        <Text
                            style={[
                                styles.habitName,
                                { color: isCompleted ? colors.foreground : `${colors.foreground}CC` },
                            ]}
                        >
                            {habit.name}
                        </Text>
                        <Text style={[styles.habitCategory, { color: colors.mutedForeground }]}>
                            {habit.category}
                        </Text>
                    </View>
                </Pressable>

                {/* Streak */}
                <View style={[styles.streakBadge, { backgroundColor: colors.surface }]}>
                    <Flame size={14} color={colors.primary} />
                    <Text style={[styles.streakText, { color: colors.foreground }]}>{habit.streak}</Text>
                </View>
            </View>

            {/* Toggle button */}
            <Pressable
                style={[
                    styles.toggleButton,
                    {
                        backgroundColor: isCompleted ? `${colors.primary}15` : colors.surface,
                        borderColor: isCompleted ? `${colors.primary}30` : 'transparent',
                    },
                ]}
                onPress={onToggle}
            >
                <Text
                    style={[
                        styles.toggleText,
                        { color: isCompleted ? colors.primary : colors.mutedForeground },
                    ]}
                >
                    {isCompleted ? 'Completed' : 'Mark Done'}
                </Text>
            </Pressable>
        </Card>
    );
}

interface HabitDetailProps {
    habit: Habit;
    onBack: () => void;
}

function HabitDetail({ habit, onBack }: HabitDetailProps) {
    const { colors } = useTheme();
    const Icon = iconMap[habit.icon] || Brain;
    const today = new Date().toISOString().split('T')[0];
    const isCompleted = habit.completedDates.includes(today);

    // Generate last 7 days
    const last7 = Array.from({ length: 7 }, (_, i) => {
        const d = new Date();
        d.setDate(d.getDate() - (6 - i));
        return d.toISOString().split('T')[0];
    });

    const dayLabels = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];

    // Generate last 30 days for streak map
    const last30 = Array.from({ length: 30 }, (_, i) => {
        const d = new Date();
        d.setDate(d.getDate() - (29 - i));
        return d.toISOString().split('T')[0];
    });

    return (
        <ScrollView
            style={[styles.container, { backgroundColor: colors.background }]}
            contentContainerStyle={styles.contentContainer}
        >
            {/* Header */}
            <View style={styles.detailHeader}>
                <Pressable
                    style={[styles.backButton, { backgroundColor: colors.surface }]}
                    onPress={onBack}
                >
                    <ArrowLeft size={20} color={colors.foreground} />
                </Pressable>
                <Text style={[styles.detailTitle, { color: colors.foreground }]}>{habit.name}</Text>
                <View style={styles.placeholder} />
            </View>

            {/* Habit Info Card */}
            <Card style={styles.card}>
                <View style={styles.infoHeader}>
                    <View
                        style={[
                            styles.largeIcon,
                            { backgroundColor: isCompleted ? `${colors.primary}20` : colors.surface },
                        ]}
                    >
                        <Icon size={28} color={isCompleted ? colors.primary : colors.mutedForeground} />
                    </View>
                    <View>
                        <Text style={[styles.habitName, { color: colors.foreground }]}>{habit.name}</Text>
                        <Text style={[styles.habitCategory, { color: colors.mutedForeground }]}>
                            {habit.category}
                        </Text>
                    </View>
                </View>

                <View style={styles.statsRow}>
                    <View style={[styles.statBox, { backgroundColor: colors.surface }]}>
                        <View style={styles.statValue}>
                            <Flame size={16} color={colors.primary} />
                            <Text style={[styles.statNumber, { color: colors.foreground }]}>{habit.streak}</Text>
                        </View>
                        <Text style={[styles.statLabel, { color: colors.mutedForeground }]}>Day streak</Text>
                    </View>
                    <View style={[styles.statBox, { backgroundColor: colors.surface }]}>
                        <Text style={[styles.statNumber, { color: colors.foreground }]}>
                            {habit.completedDates.length}
                        </Text>
                        <Text style={[styles.statLabel, { color: colors.mutedForeground }]}>Total days</Text>
                    </View>
                </View>
            </Card>

            {/* Streak Map */}
            <Card style={styles.card}>
                <Text style={[styles.sectionTitle, { color: colors.foreground }]}>Streak Map</Text>
                <View style={styles.streakGrid}>
                    {last30.map((date) => (
                        <View
                            key={date}
                            style={[
                                styles.streakDot,
                                {
                                    backgroundColor: habit.completedDates.includes(date)
                                        ? `${colors.primary}99`
                                        : colors.surface,
                                },
                            ]}
                        />
                    ))}
                </View>
            </Card>

            {/* This Week */}
            <Card style={styles.card}>
                <Text style={[styles.sectionTitle, { color: colors.foreground }]}>This Week</Text>
                <View style={styles.weekRow}>
                    {last7.map((date) => {
                        const done = habit.completedDates.includes(date);
                        const isToday = date === today;
                        return (
                            <View key={date} style={styles.dayColumn}>
                                <Text
                                    style={[
                                        styles.dayLabel,
                                        { color: isToday ? colors.primary : colors.mutedForeground },
                                    ]}
                                >
                                    {dayLabels[new Date(date).getDay()]}
                                </Text>
                                <View
                                    style={[
                                        styles.dayCircle,
                                        {
                                            backgroundColor: done ? `${colors.primary}30` : colors.surface,
                                            borderColor: isToday && !done ? `${colors.primary}50` : 'transparent',
                                            borderWidth: isToday && !done ? 1 : 0,
                                        },
                                    ]}
                                >
                                    {done && <Check size={16} color={colors.primary} />}
                                </View>
                            </View>
                        );
                    })}
                </View>
            </Card>

            {/* Progress Photos placeholder */}
            <Card style={styles.card}>
                <View style={styles.photoHeader}>
                    <Text style={[styles.sectionTitle, { color: colors.foreground }]}>Progress Photos</Text>
                    <Pressable style={[styles.cameraButton, { backgroundColor: colors.surface }]}>
                        <Camera size={20} color={colors.mutedForeground} />
                    </Pressable>
                </View>
                <View style={styles.photoGrid}>
                    {['Front', 'Left', 'Right', 'Back', 'Face'].map((label) => (
                        <View key={label} style={[styles.photoPlaceholder, { backgroundColor: colors.surface }]}>
                            <Camera size={16} color={`${colors.mutedForeground}66`} />
                            <Text style={[styles.photoLabel, { color: `${colors.mutedForeground}66` }]}>
                                {label}
                            </Text>
                        </View>
                    ))}
                </View>
            </Card>
        </ScrollView>
    );
}

export function HabitsScreen() {
    const { colors } = useTheme();
    const navigation = useNavigation();
    const { habits, toggleHabit, selectedHabitId, setSelectedHabitId } = useApp();
    const [showDetail, setShowDetail] = useState(false);
    const today = new Date().toISOString().split('T')[0];

    const selectedHabit = habits.find((h) => h.id === selectedHabitId);

    if (showDetail && selectedHabit) {
        return (
            <HabitDetail
                habit={selectedHabit}
                onBack={() => {
                    setSelectedHabitId(null);
                    setShowDetail(false);
                }}
            />
        );
    }

    const completedCount = habits.filter((h) => h.completedDates.includes(today)).length;

    return (
        <ScrollView
            style={[styles.container, { backgroundColor: colors.background }]}
            contentContainerStyle={styles.contentContainer}
        >
            {/* Header */}
            <View style={styles.header}>
                <Text style={[styles.title, { color: colors.foreground }]}>Habits</Text>
                <View style={[styles.countBadge, { backgroundColor: colors.surface }]}>
                    <Flame size={16} color={colors.primary} />
                    <Text style={[styles.countText, { color: colors.foreground }]}>
                        {completedCount}/{habits.length}
                    </Text>
                </View>
            </View>

            {/* Progress bar */}
            <View style={styles.progressContainer}>
                <Progress value={(completedCount / habits.length) * 100} />
            </View>

            {/* Habits list */}
            <View style={styles.habitsList}>
                {habits.map((habit) => (
                    <HabitCard
                        key={habit.id}
                        habit={habit}
                        isCompleted={habit.completedDates.includes(today)}
                        onToggle={() => toggleHabit(habit.id, today)}
                        onSelect={() => {
                            setSelectedHabitId(habit.id);
                            setShowDetail(true);
                        }}
                    />
                ))}
            </View>
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
    title: {
        fontSize: fontSize['2xl'],
        fontWeight: '600',
    },
    countBadge: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.xs,
        paddingVertical: spacing.xs,
        paddingHorizontal: spacing.md,
        borderRadius: borderRadius.full,
    },
    countText: {
        fontSize: fontSize.sm,
    },
    progressContainer: {
        marginBottom: spacing.xl,
    },
    habitsList: {
        gap: spacing.md,
    },
    habitCard: {
        marginBottom: spacing.md,
    },
    habitHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'flex-start',
        padding: spacing.lg,
        paddingBottom: spacing.md,
    },
    habitInfo: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.md,
        flex: 1,
    },
    habitIcon: {
        width: 40,
        height: 40,
        borderRadius: borderRadius.xl,
        alignItems: 'center',
        justifyContent: 'center',
    },
    habitName: {
        fontSize: fontSize.sm,
        fontWeight: '500',
    },
    habitCategory: {
        fontSize: fontSize.xs,
        marginTop: 2,
    },
    streakBadge: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.xs,
        paddingVertical: spacing.xs,
        paddingHorizontal: spacing.sm,
        borderRadius: borderRadius.lg,
    },
    streakText: {
        fontSize: fontSize.xs,
    },
    toggleButton: {
        marginHorizontal: spacing.lg,
        marginBottom: spacing.lg,
        paddingVertical: spacing.md,
        borderRadius: borderRadius.xl,
        alignItems: 'center',
        borderWidth: 1,
    },
    toggleText: {
        fontSize: fontSize.sm,
        fontWeight: '500',
    },
    // Detail styles
    detailHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: spacing.lg,
    },
    backButton: {
        width: 40,
        height: 40,
        borderRadius: borderRadius.xl,
        alignItems: 'center',
        justifyContent: 'center',
    },
    detailTitle: {
        fontSize: fontSize.lg,
        fontWeight: '600',
    },
    placeholder: {
        width: 40,
    },
    card: {
        marginBottom: spacing.lg,
    },
    infoHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.lg,
        padding: spacing.lg,
        paddingBottom: spacing.md,
    },
    largeIcon: {
        width: 56,
        height: 56,
        borderRadius: borderRadius['2xl'],
        alignItems: 'center',
        justifyContent: 'center',
    },
    statsRow: {
        flexDirection: 'row',
        gap: spacing.md,
        paddingHorizontal: spacing.lg,
        paddingBottom: spacing.lg,
    },
    statBox: {
        flex: 1,
        borderRadius: borderRadius.xl,
        padding: spacing.md,
        alignItems: 'center',
    },
    statValue: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.xs,
        marginBottom: spacing.xs,
    },
    statNumber: {
        fontSize: fontSize.xl,
        fontWeight: '600',
    },
    statLabel: {
        fontSize: fontSize.xs,
    },
    sectionTitle: {
        fontSize: fontSize.md,
        fontWeight: '600',
        paddingHorizontal: spacing.lg,
        paddingTop: spacing.lg,
        paddingBottom: spacing.md,
    },
    streakGrid: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: spacing.xs,
        paddingHorizontal: spacing.lg,
        paddingBottom: spacing.lg,
    },
    streakDot: {
        width: '9%',
        aspectRatio: 1,
        borderRadius: borderRadius.md,
    },
    weekRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        paddingHorizontal: spacing.lg,
        paddingBottom: spacing.lg,
    },
    dayColumn: {
        alignItems: 'center',
        gap: spacing.sm,
    },
    dayLabel: {
        fontSize: fontSize.xs,
    },
    dayCircle: {
        width: 32,
        height: 32,
        borderRadius: borderRadius.lg,
        alignItems: 'center',
        justifyContent: 'center',
    },
    photoHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingRight: spacing.lg,
    },
    cameraButton: {
        width: 40,
        height: 40,
        borderRadius: borderRadius.xl,
        alignItems: 'center',
        justifyContent: 'center',
    },
    photoGrid: {
        flexDirection: 'row',
        gap: spacing.sm,
        paddingHorizontal: spacing.lg,
        paddingBottom: spacing.lg,
    },
    photoPlaceholder: {
        flex: 1,
        aspectRatio: 1,
        borderRadius: borderRadius.xl,
        alignItems: 'center',
        justifyContent: 'center',
        gap: spacing.xs,
    },
    photoLabel: {
        fontSize: 9,
    },
});
