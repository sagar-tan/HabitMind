import React, { useState } from 'react';
import {
    View,
    Text,
    Pressable,
    ScrollView,
    TextInput,
    StyleSheet,
    Modal,
    KeyboardAvoidingView,
    Platform,
} from 'react-native';
import {
    Plus, GanttChart, List, Clock, X, Check,
} from 'lucide-react-native';
import { useApp, Task } from '../context/AppContext';
import { useTheme } from '../theme/ThemeContext';
import { Card } from '../components/ui/Card';
import { Progress } from '../components/ui/Progress';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { borderRadius, fontSize, spacing } from '../theme';
import Slider from '@react-native-community/slider';

interface TaskCardProps {
    task: Task;
    onUpdate: (id: string, updates: Partial<Task>) => void;
    onDelete: (id: string) => void;
}

function TaskCard({ task, onUpdate, onDelete }: TaskCardProps) {
    const { colors } = useTheme();
    // Note: Swipe-to-delete can be implemented with react-native-gesture-handler's Swipeable
    // For now, using a simple delete button on long-press

    const [showDelete, setShowDelete] = useState(false);

    return (
        <Pressable
            onLongPress={() => setShowDelete(true)}
            delayLongPress={500}
        >
            <Card style={styles.taskCard}>
                <View style={styles.taskContent}>
                    {/* Checkbox */}
                    <Pressable
                        style={[
                            styles.checkbox,
                            {
                                backgroundColor: task.completed ? colors.primary : 'transparent',
                                borderColor: task.completed ? colors.primary : `${colors.mutedForeground}50`,
                            },
                        ]}
                        onPress={() => onUpdate(task.id, {
                            completed: !task.completed,
                            progress: task.completed ? task.progress : 100,
                        })}
                    >
                        {task.completed && <Check size={14} color={colors.primaryForeground} />}
                    </Pressable>

                    <View style={styles.taskInfo}>
                        <Text
                            style={[
                                styles.taskTitle,
                                { color: colors.foreground },
                                task.completed && styles.taskTitleCompleted,
                                task.completed && { color: colors.mutedForeground },
                            ]}
                        >
                            {task.title}
                        </Text>
                        <View style={styles.taskMeta}>
                            <Clock size={12} color={colors.mutedForeground} />
                            <Text style={[styles.taskMetaText, { color: colors.mutedForeground }]}>
                                {task.timeEstimate}min
                            </Text>
                            <Text style={[styles.taskProgress, { color: colors.primary }]}>
                                {task.progress}%
                            </Text>
                        </View>

                        {/* Progress slider */}
                        {!task.completed && (
                            <View style={styles.sliderContainer}>
                                <Progress value={task.progress} />
                                <Slider
                                    style={styles.slider}
                                    minimumValue={0}
                                    maximumValue={100}
                                    step={5}
                                    value={task.progress}
                                    onValueChange={(val) => onUpdate(task.id, {
                                        progress: val,
                                        completed: val === 100,
                                    })}
                                    minimumTrackTintColor="transparent"
                                    maximumTrackTintColor="transparent"
                                    thumbTintColor={colors.primary}
                                />
                            </View>
                        )}
                    </View>

                    {/* Delete button */}
                    {showDelete && (
                        <Pressable
                            style={[styles.deleteButton, { backgroundColor: `${colors.destructive}20` }]}
                            onPress={() => {
                                onDelete(task.id);
                                setShowDelete(false);
                            }}
                        >
                            <X size={16} color={colors.destructive} />
                        </Pressable>
                    )}
                </View>
            </Card>
        </Pressable>
    );
}

interface GanttViewProps {
    tasks: Task[];
}

function GanttView({ tasks }: GanttViewProps) {
    const { colors } = useTheme();
    const totalMinutes = tasks.reduce((sum, t) => sum + t.timeEstimate, 0);

    return (
        <View style={styles.ganttContainer}>
            <View style={styles.ganttHeader}>
                <Text style={[styles.ganttLabel, { color: colors.mutedForeground }]}>0h</Text>
                <Text style={[styles.ganttLabel, { color: colors.mutedForeground }]}>
                    {Math.round((totalMinutes / 60) * 10) / 10}h total
                </Text>
            </View>
            {tasks.map((task) => {
                const widthPercent = Math.max((task.timeEstimate / Math.max(totalMinutes, 1)) * 100, 15);
                return (
                    <View key={task.id} style={styles.ganttRow}>
                        <Text
                            style={[styles.ganttTaskName, { color: colors.mutedForeground }]}
                            numberOfLines={1}
                        >
                            {task.title}
                        </Text>
                        <View style={[styles.ganttBar, { backgroundColor: colors.surface }]}>
                            <View
                                style={[
                                    styles.ganttProgress,
                                    {
                                        width: `${widthPercent}%`,
                                        backgroundColor: `${colors.primary}30`,
                                    },
                                ]}
                            />
                            <View
                                style={[
                                    styles.ganttFilled,
                                    {
                                        width: `${(widthPercent * task.progress) / 100}%`,
                                        backgroundColor: `${colors.primary}80`,
                                    },
                                ]}
                            />
                            <Text style={[styles.ganttTime, { color: `${colors.foreground}CC` }]}>
                                {task.timeEstimate}m
                            </Text>
                        </View>
                    </View>
                );
            })}
        </View>
    );
}

interface AddTaskSheetProps {
    visible: boolean;
    onClose: () => void;
    onAdd: (task: Omit<Task, 'id'>) => void;
}

function AddTaskSheet({ visible, onClose, onAdd }: AddTaskSheetProps) {
    const { colors } = useTheme();
    const [title, setTitle] = useState('');
    const [time, setTime] = useState('30');

    const handleAdd = () => {
        if (title.trim()) {
            onAdd({
                title: title.trim(),
                timeEstimate: Number(time) || 30,
                progress: 0,
                date: new Date().toISOString().split('T')[0],
                completed: false,
                deferred: false,
            });
            setTitle('');
            setTime('30');
            onClose();
        }
    };

    return (
        <Modal visible={visible} transparent animationType="slide">
            <Pressable style={styles.sheetOverlay} onPress={onClose}>
                <KeyboardAvoidingView
                    behavior={Platform.OS === 'ios' ? 'padding' : undefined}
                    style={styles.sheetKeyboard}
                >
                    <Pressable
                        style={[styles.sheetContent, { backgroundColor: colors.card }]}
                        onPress={(e) => e.stopPropagation()}
                    >
                        <View style={[styles.sheetHandle, { backgroundColor: `${colors.mutedForeground}50` }]} />

                        <View style={styles.sheetHeader}>
                            <Text style={[styles.sheetTitle, { color: colors.foreground }]}>New Task</Text>
                            <Pressable onPress={onClose} style={styles.sheetClose}>
                                <X size={20} color={colors.mutedForeground} />
                            </Pressable>
                        </View>

                        <View style={styles.sheetForm}>
                            <Input
                                value={title}
                                onChangeText={setTitle}
                                placeholder="What needs to be done?"
                                autoFocus
                            />
                            <View style={styles.timeRow}>
                                <Clock size={16} color={colors.mutedForeground} />
                                <TextInput
                                    value={time}
                                    onChangeText={setTime}
                                    keyboardType="number-pad"
                                    style={[
                                        styles.timeInput,
                                        { backgroundColor: colors.surface, color: colors.foreground, borderColor: colors.border },
                                    ]}
                                />
                                <Text style={[styles.timeLabel, { color: colors.mutedForeground }]}>minutes</Text>
                            </View>
                        </View>

                        <Button
                            variant={title.trim() ? 'default' : 'secondary'}
                            disabled={!title.trim()}
                            onPress={handleAdd}
                            style={styles.addButton}
                        >
                            Add Task
                        </Button>
                    </Pressable>
                </KeyboardAvoidingView>
            </Pressable>
        </Modal>
    );
}

export function PlanScreen() {
    const { colors } = useTheme();
    const { tasks, addTask, updateTask, deleteTask } = useApp();
    const [view, setView] = useState<'list' | 'gantt'>('list');
    const [showAdd, setShowAdd] = useState(false);

    const today = new Date().toISOString().split('T')[0];
    const todayTasks = tasks.filter((t) => t.date === today);
    const completedCount = todayTasks.filter((t) => t.completed).length;
    const progressPercent = todayTasks.length > 0
        ? (completedCount / todayTasks.length) * 100
        : 0;

    return (
        <View style={[styles.container, { backgroundColor: colors.background }]}>
            <ScrollView contentContainerStyle={styles.scrollContent}>
                {/* Header */}
                <View style={styles.header}>
                    <Text style={[styles.title, { color: colors.foreground }]}>Plan</Text>
                    <View style={[styles.viewToggle, { backgroundColor: colors.surface }]}>
                        <Pressable
                            style={[
                                styles.viewButton,
                                view === 'list' && { backgroundColor: `${colors.primary}20` },
                            ]}
                            onPress={() => setView('list')}
                        >
                            <List size={20} color={view === 'list' ? colors.primary : colors.mutedForeground} />
                        </Pressable>
                        <Pressable
                            style={[
                                styles.viewButton,
                                view === 'gantt' && { backgroundColor: `${colors.primary}20` },
                            ]}
                            onPress={() => setView('gantt')}
                        >
                            <GanttChart size={20} color={view === 'gantt' ? colors.primary : colors.mutedForeground} />
                        </Pressable>
                    </View>
                </View>

                {/* Summary */}
                <View style={styles.summary}>
                    <Text style={[styles.summaryText, { color: colors.mutedForeground }]}>
                        {completedCount}/{todayTasks.length} tasks
                    </Text>
                    <View style={styles.summaryProgress}>
                        <Progress value={progressPercent} />
                    </View>
                </View>

                {/* Content */}
                {view === 'list' ? (
                    <View style={styles.taskList}>
                        {todayTasks.length === 0 ? (
                            <View style={styles.emptyState}>
                                <View style={[styles.emptyIcon, { backgroundColor: colors.surface }]}>
                                    <List size={32} color={`${colors.mutedForeground}80`} />
                                </View>
                                <Text style={[styles.emptyText, { color: colors.mutedForeground }]}>
                                    No tasks for today yet
                                </Text>
                                <Button onPress={() => setShowAdd(true)}>Add your first task</Button>
                            </View>
                        ) : (
                            todayTasks.map((task) => (
                                <TaskCard
                                    key={task.id}
                                    task={task}
                                    onUpdate={updateTask}
                                    onDelete={deleteTask}
                                />
                            ))
                        )}
                    </View>
                ) : (
                    <Card style={styles.ganttCard}>
                        <Text style={[styles.cardTitle, { color: colors.foreground }]}>Day Load</Text>
                        {todayTasks.length > 0 ? (
                            <GanttView tasks={todayTasks} />
                        ) : (
                            <Text style={[styles.emptyGantt, { color: colors.mutedForeground }]}>
                                No tasks to visualize
                            </Text>
                        )}
                    </Card>
                )}
            </ScrollView>

            {/* FAB */}
            <Pressable
                style={[styles.fab, { backgroundColor: colors.primary }]}
                onPress={() => setShowAdd(true)}
            >
                <Plus size={24} color={colors.primaryForeground} />
            </Pressable>

            {/* Add Sheet */}
            <AddTaskSheet
                visible={showAdd}
                onClose={() => setShowAdd(false)}
                onAdd={addTask}
            />
        </View>
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
    viewToggle: {
        flexDirection: 'row',
        borderRadius: borderRadius.xl,
        padding: spacing.xs,
    },
    viewButton: {
        paddingHorizontal: spacing.lg,
        paddingVertical: spacing.sm,
        borderRadius: borderRadius.lg,
    },
    summary: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.lg,
        marginBottom: spacing.xl,
    },
    summaryText: {
        fontSize: fontSize.sm,
    },
    summaryProgress: {
        flex: 1,
    },
    taskList: {
        gap: spacing.sm,
    },
    taskCard: {
        marginBottom: spacing.sm,
    },
    taskContent: {
        flexDirection: 'row',
        alignItems: 'flex-start',
        padding: spacing.lg,
        gap: spacing.md,
    },
    checkbox: {
        width: 24,
        height: 24,
        borderRadius: borderRadius.lg,
        borderWidth: 2,
        alignItems: 'center',
        justifyContent: 'center',
    },
    taskInfo: {
        flex: 1,
    },
    taskTitle: {
        fontSize: fontSize.sm,
        marginBottom: spacing.xs,
    },
    taskTitleCompleted: {
        textDecorationLine: 'line-through',
    },
    taskMeta: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.sm,
    },
    taskMetaText: {
        fontSize: fontSize.xs,
    },
    taskProgress: {
        fontSize: fontSize.xs,
    },
    sliderContainer: {
        marginTop: spacing.md,
        position: 'relative',
    },
    slider: {
        position: 'absolute',
        left: -12,
        right: -12,
        top: -16,
        height: 40,
    },
    deleteButton: {
        width: 32,
        height: 32,
        borderRadius: borderRadius.lg,
        alignItems: 'center',
        justifyContent: 'center',
    },
    ganttCard: {
        padding: spacing.lg,
    },
    cardTitle: {
        fontSize: fontSize.md,
        fontWeight: '600',
        marginBottom: spacing.lg,
    },
    ganttContainer: {
        gap: spacing.sm,
    },
    ganttHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: spacing.md,
    },
    ganttLabel: {
        fontSize: fontSize.xs,
    },
    ganttRow: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.md,
    },
    ganttTaskName: {
        width: 80,
        fontSize: fontSize.xs,
    },
    ganttBar: {
        flex: 1,
        height: 32,
        borderRadius: borderRadius.lg,
        overflow: 'hidden',
        justifyContent: 'center',
    },
    ganttProgress: {
        position: 'absolute',
        left: 0,
        top: 0,
        bottom: 0,
        borderRadius: borderRadius.lg,
    },
    ganttFilled: {
        position: 'absolute',
        left: 0,
        top: 0,
        bottom: 0,
        borderRadius: borderRadius.lg,
    },
    ganttTime: {
        fontSize: fontSize.xs,
        paddingLeft: spacing.sm,
    },
    emptyGantt: {
        textAlign: 'center',
        paddingVertical: spacing['2xl'],
    },
    emptyState: {
        alignItems: 'center',
        paddingVertical: spacing['5xl'],
    },
    emptyIcon: {
        width: 64,
        height: 64,
        borderRadius: borderRadius['2xl'],
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: spacing.lg,
    },
    emptyText: {
        fontSize: fontSize.md,
        marginBottom: spacing.lg,
    },
    fab: {
        position: 'absolute',
        bottom: spacing['3xl'],
        right: spacing.xl,
        width: 56,
        height: 56,
        borderRadius: 28,
        alignItems: 'center',
        justifyContent: 'center',
        elevation: 4,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.25,
        shadowRadius: 4,
    },
    // Sheet styles
    sheetOverlay: {
        flex: 1,
        backgroundColor: 'rgba(0,0,0,0.5)',
        justifyContent: 'flex-end',
    },
    sheetKeyboard: {
        width: '100%',
    },
    sheetContent: {
        borderTopLeftRadius: borderRadius['3xl'],
        borderTopRightRadius: borderRadius['3xl'],
        padding: spacing.xl,
        paddingBottom: spacing['3xl'],
    },
    sheetHandle: {
        width: 40,
        height: 4,
        borderRadius: 2,
        alignSelf: 'center',
        marginBottom: spacing.xl,
    },
    sheetHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: spacing.xl,
    },
    sheetTitle: {
        fontSize: fontSize.lg,
        fontWeight: '600',
    },
    sheetClose: {
        padding: spacing.sm,
    },
    sheetForm: {
        gap: spacing.lg,
        marginBottom: spacing.xl,
    },
    timeRow: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: spacing.md,
    },
    timeInput: {
        width: 80,
        paddingVertical: spacing.sm,
        paddingHorizontal: spacing.md,
        borderRadius: borderRadius.lg,
        borderWidth: 1,
        textAlign: 'center',
        fontSize: fontSize.md,
    },
    timeLabel: {
        fontSize: fontSize.sm,
    },
    addButton: {
        width: '100%',
    },
});
