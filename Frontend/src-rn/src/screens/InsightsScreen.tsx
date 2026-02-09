import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    Pressable,
    ScrollView,
    StyleSheet,
} from 'react-native';
import {
    BarChart3, TrendingUp, CalendarCheck, Clock, ChevronDown, Target,
} from 'lucide-react-native';
import { useApp } from '../context/AppContext';
import { useTheme } from '../theme/ThemeContext';
import { Card } from '../components/ui/Card';
import { Progress } from '../components/ui/Progress';
import { SimpleBarChart } from '../components/ui/Chart';
import { borderRadius, fontSize, spacing } from '../theme';

// Animated number component
function AnimatedNumber({ value, suffix = '' }: { value: number; suffix?: string }) {
    const [display, setDisplay] = useState(0);

    useEffect(() => {
        const duration = 600;
        const startTime = Date.now();

        const tick = () => {
            const elapsed = Date.now() - startTime;
            const progress = Math.min(elapsed / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3);
            setDisplay(Math.round(value * eased));
            if (progress < 1) {
                requestAnimationFrame(tick);
            }
        };

        requestAnimationFrame(tick);
    }, [value]);

    return <Text>{display}{suffix}</Text>;
}

export function InsightsScreen() {
    const { colors, mode } = useTheme();
    const { habits, tasks, goals } = useApp();
    const [timeRange, setTimeRange] = useState<'week' | 'month'>('week');

    const today = new Date().toISOString().split('T')[0];
    const totalHabits = habits.length;
    const completedHabits = habits.filter((h) => h.completedDates.includes(today)).length;
    const consistencyScore = totalHabits > 0 ? Math.round((completedHabits / totalHabits) * 100) : 0;

    const completedTasks = tasks.filter((t) => t.completed).length;
    const totalTasks = tasks.length;
    const planningAccuracy = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;

    // Mock weekly data for charts
    const weeklyHabitData = [
        { label: 'Mon', value: 67 },
        { label: 'Tue', value: 83 },
        { label: 'Wed', value: 50 },
        { label: 'Thu', value: 100 },
        { label: 'Fri', value: 67 },
        { label: 'Sat', value: 83 },
        { label: 'Sun', value: consistencyScore },
    ];

    const monthlyHabitData = [
        { label: 'W1', value: 72 },
        { label: 'W2', value: 68 },
        { label: 'W3', value: 85 },
        { label: 'W4', value: 78 },
    ];

    const chartData = timeRange === 'week' ? weeklyHabitData : monthlyHabitData;

    // Stats cards
    const stats = [
        { icon: TrendingUp, label: 'Consistency', value: consistencyScore },
        { icon: CalendarCheck, label: 'Adherence', value: Math.round((completedHabits / Math.max(totalHabits, 1)) * 100) },
        { icon: BarChart3, label: 'Accuracy', value: planningAccuracy },
    ];

    return (
        <ScrollView
            style={[styles.container, { backgroundColor: colors.background }]}
            contentContainerStyle={styles.scrollContent}
        >
            {/* Header */}
            <View style={styles.header}>
                <Text style={[styles.title, { color: colors.foreground }]}>Insights</Text>
                <Pressable
                    style={[styles.timeToggle, { backgroundColor: colors.surface }]}
                    onPress={() => setTimeRange(timeRange === 'week' ? 'month' : 'week')}
                >
                    <Text style={[styles.timeText, { color: colors.mutedForeground }]}>
                        {timeRange === 'week' ? 'This Week' : 'This Month'}
                    </Text>
                    <ChevronDown size={16} color={colors.mutedForeground} />
                </Pressable>
            </View>

            {/* Overview Cards */}
            <View style={styles.statsGrid}>
                {stats.map((stat) => (
                    <Card key={stat.label} style={styles.statCard}>
                        <stat.icon size={20} color={colors.primary} style={styles.statIcon} />
                        <Text style={[styles.statValue, { color: colors.foreground }]}>
                            <AnimatedNumber value={stat.value} suffix="%" />
                        </Text>
                        <Text style={[styles.statLabel, { color: colors.mutedForeground }]}>
                            {stat.label}
                        </Text>
                    </Card>
                ))}
            </View>

            {/* Habit Trends Chart */}
            <Card style={styles.chartCard}>
                <Text style={[styles.cardTitle, { color: colors.foreground }]}>Habit Trends</Text>
                <SimpleBarChart
                    data={chartData}
                    height={180}
                    barColor={colors.primary}
                    showLabels
                />
            </Card>

            {/* Task Completion - Simplified */}
            <Card style={styles.chartCard}>
                <Text style={[styles.cardTitle, { color: colors.foreground }]}>Task Completion</Text>
                <View style={styles.taskStats}>
                    <View style={styles.taskStatRow}>
                        <Text style={[styles.taskStatLabel, { color: colors.mutedForeground }]}>
                            Completed this week
                        </Text>
                        <Text style={[styles.taskStatValue, { color: colors.foreground }]}>
                            {completedTasks}
                        </Text>
                    </View>
                    <View style={styles.taskStatRow}>
                        <Text style={[styles.taskStatLabel, { color: colors.mutedForeground }]}>
                            Total tasks
                        </Text>
                        <Text style={[styles.taskStatValue, { color: colors.foreground }]}>
                            {totalTasks}
                        </Text>
                    </View>
                    <View style={styles.progressSection}>
                        <Progress value={planningAccuracy} />
                        <Text style={[styles.progressLabel, { color: colors.mutedForeground }]}>
                            {planningAccuracy}% completion rate
                        </Text>
                    </View>
                </View>
            </Card>

            {/* Goals Section */}
            <View style={styles.goalsSection}>
                <Text style={[styles.sectionTitle, { color: colors.foreground }]}>Goals</Text>
                <View style={styles.goalsList}>
                    {goals.map((goal) => (
                        <Card key={goal.id} style={styles.goalCard}>
                            <View style={styles.goalHeader}>
                                <View style={[styles.goalIcon, { backgroundColor: `${colors.primary}15` }]}>
                                    <Target size={18} color={colors.primary} />
                                </View>
                                <View style={styles.goalInfo}>
                                    <Text style={[styles.goalTitle, { color: colors.foreground }]}>
                                        {goal.title}
                                    </Text>
                                    <Text style={[styles.goalProgress, { color: colors.mutedForeground }]}>
                                        {goal.progress}% complete
                                    </Text>
                                </View>
                            </View>
                            <Progress value={goal.progress} />
                        </Card>
                    ))}
                </View>
            </View>
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
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: spacing.lg,
    },
    title: {
        fontSize: fontSize['2xl'],
        fontWeight: '600',
    },
    timeToggle: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.xs,
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.sm,
        borderRadius: borderRadius.xl,
    },
    timeText: {
        fontSize: fontSize.sm,
    },
    statsGrid: {
        flexDirection: 'row',
        gap: spacing.md,
        marginBottom: spacing.lg,
    },
    statCard: {
        flex: 1,
        padding: spacing.md,
        alignItems: 'center',
    },
    statIcon: {
        marginBottom: spacing.sm,
    },
    statValue: {
        fontSize: fontSize.xl,
        fontWeight: '600',
    },
    statLabel: {
        fontSize: 10,
        marginTop: spacing.xs,
    },
    chartCard: {
        padding: spacing.lg,
        marginBottom: spacing.lg,
    },
    cardTitle: {
        fontSize: fontSize.md,
        fontWeight: '600',
        marginBottom: spacing.lg,
    },
    taskStats: {
        gap: spacing.md,
    },
    taskStatRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    taskStatLabel: {
        fontSize: fontSize.sm,
    },
    taskStatValue: {
        fontSize: fontSize.lg,
        fontWeight: '600',
    },
    progressSection: {
        marginTop: spacing.md,
        gap: spacing.xs,
    },
    progressLabel: {
        fontSize: fontSize.xs,
        textAlign: 'center',
    },
    goalsSection: {
        marginBottom: spacing.lg,
    },
    sectionTitle: {
        fontSize: fontSize.md,
        fontWeight: '600',
        marginBottom: spacing.md,
    },
    goalsList: {
        gap: spacing.md,
    },
    goalCard: {
        padding: spacing.lg,
    },
    goalHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.md,
        marginBottom: spacing.md,
    },
    goalIcon: {
        width: 36,
        height: 36,
        borderRadius: borderRadius.xl,
        alignItems: 'center',
        justifyContent: 'center',
    },
    goalInfo: {
        flex: 1,
    },
    goalTitle: {
        fontSize: fontSize.sm,
        fontWeight: '500',
    },
    goalProgress: {
        fontSize: fontSize.xs,
        marginTop: 2,
    },
});
