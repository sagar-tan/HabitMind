import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';

// Types
export interface Task {
    id: string;
    title: string;
    timeEstimate: number; // minutes
    progress: number; // 0-100
    date: string;
    completed: boolean;
    deferred: boolean;
}

export interface Habit {
    id: string;
    name: string;
    icon: string;
    streak: number;
    completedDates: string[];
    category: string;
}

export interface JournalEntry {
    id: string;
    type: 'text' | 'voice' | 'image';
    content: string;
    tags: string[];
    timestamp: string;
    date: string;
}

export interface DailyLog {
    id: string;
    text: string;
    timestamp: string;
}

export interface Goal {
    id: string;
    title: string;
    progress: number;
    nextUpdate: string;
    notes: string[];
    weeklyProgress: { week: string; value: number }[];
}

export interface UserProfile {
    age: string;
    height: string;
    weight: string;
}

export type TabName = 'home' | 'plan' | 'habits' | 'journal' | 'insights';
export type ScreenName = 'main' | 'settings' | 'weekly-review' | 'habit-detail' | 'goal-detail' | 'journal-entry';
export type ThemeMode = 'light' | 'dark';

interface AppState {
    onboarded: boolean;
    profile: UserProfile;
    tasks: Task[];
    habits: Habit[];
    journal: JournalEntry[];
    logs: DailyLog[];
    goals: Goal[];
    selectedHabitId: string | null;
    selectedGoalId: string | null;
    mood: number; // 1-5
}

interface AppContextType extends AppState {
    setOnboarded: (v: boolean) => void;
    setProfile: (p: UserProfile) => void;
    setSelectedHabitId: (id: string | null) => void;
    setSelectedGoalId: (id: string | null) => void;
    setMood: (m: number) => void;
    addTask: (t: Omit<Task, 'id'>) => void;
    updateTask: (id: string, updates: Partial<Task>) => void;
    deleteTask: (id: string) => void;
    toggleHabit: (id: string, date: string) => void;
    addJournalEntry: (e: Omit<JournalEntry, 'id'>) => void;
    addLog: (text: string) => void;
    addGoal: (g: Omit<Goal, 'id'>) => void;
    updateGoal: (id: string, updates: Partial<Goal>) => void;
}

const today = new Date().toISOString().split('T')[0];

const defaultTasks: Task[] = [
    { id: 't1', title: 'Morning meditation', timeEstimate: 15, progress: 100, date: today, completed: true, deferred: false },
    { id: 't2', title: 'Review weekly goals', timeEstimate: 20, progress: 60, date: today, completed: false, deferred: false },
    { id: 't3', title: 'Workout session', timeEstimate: 45, progress: 0, date: today, completed: false, deferred: false },
    { id: 't4', title: 'Read 30 pages', timeEstimate: 30, progress: 30, date: today, completed: false, deferred: false },
    { id: 't5', title: 'Meal prep for tomorrow', timeEstimate: 40, progress: 0, date: today, completed: false, deferred: false },
];

const defaultHabits: Habit[] = [
    { id: 'h1', name: 'Meditation', icon: 'brain', streak: 12, completedDates: [today], category: 'Mind' },
    { id: 'h2', name: 'Exercise', icon: 'dumbbell', streak: 8, completedDates: [], category: 'Body' },
    { id: 'h3', name: 'Reading', icon: 'book-open', streak: 15, completedDates: [today], category: 'Mind' },
    { id: 'h4', name: 'Hydration', icon: 'droplets', streak: 20, completedDates: [today], category: 'Body' },
    { id: 'h5', name: 'Sleep 8hrs', icon: 'moon', streak: 5, completedDates: [], category: 'Body' },
    { id: 'h6', name: 'Journal', icon: 'pen-line', streak: 3, completedDates: [], category: 'Mind' },
];

const defaultJournal: JournalEntry[] = [
    {
        id: 'j1',
        type: 'text',
        content: 'Feeling focused today. Morning routine went well. Need to work on consistency with evening habits.',
        tags: ['reflection', 'morning'],
        timestamp: new Date().toISOString(),
        date: today,
    },
    {
        id: 'j2',
        type: 'text',
        content: 'Had a great workout session. Energy levels are improving since I started tracking sleep.',
        tags: ['fitness', 'progress'],
        timestamp: new Date(Date.now() - 86400000).toISOString(),
        date: new Date(Date.now() - 86400000).toISOString().split('T')[0],
    },
];

const defaultGoals: Goal[] = [
    {
        id: 'g1',
        title: 'Run a 5K',
        progress: 65,
        nextUpdate: today,
        notes: ['Started C25K program', 'Week 6 completed'],
        weeklyProgress: [
            { week: 'W1', value: 20 },
            { week: 'W2', value: 35 },
            { week: 'W3', value: 45 },
            { week: 'W4', value: 55 },
            { week: 'W5', value: 65 },
        ],
    },
    {
        id: 'g2',
        title: 'Read 24 books this year',
        progress: 40,
        nextUpdate: today,
        notes: ['9 books completed', 'Currently reading Atomic Habits'],
        weeklyProgress: [
            { week: 'W1', value: 10 },
            { week: 'W2', value: 20 },
            { week: 'W3', value: 30 },
            { week: 'W4', value: 35 },
            { week: 'W5', value: 40 },
        ],
    },
    {
        id: 'g3',
        title: 'Meditate daily for 90 days',
        progress: 80,
        nextUpdate: today,
        notes: ['72 days streak', 'Moved to 20min sessions'],
        weeklyProgress: [
            { week: 'W1', value: 30 },
            { week: 'W2', value: 45 },
            { week: 'W3', value: 60 },
            { week: 'W4', value: 70 },
            { week: 'W5', value: 80 },
        ],
    },
];

const STORAGE_KEY = 'habitmind-app-state';

async function loadState(): Promise<Partial<AppState>> {
    try {
        const saved = await AsyncStorage.getItem(STORAGE_KEY);
        if (saved) return JSON.parse(saved);
    } catch { }
    return {};
}

async function saveState(state: Partial<AppState>) {
    try {
        await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    } catch { }
}

const AppContext = createContext<AppContextType | null>(null);

export function AppProvider({ children }: { children: React.ReactNode }) {
    const [loaded, setLoaded] = useState(false);
    const [onboarded, setOnboarded] = useState(false);
    const [profile, setProfile] = useState<UserProfile>({ age: '', height: '', weight: '' });
    const [tasks, setTasks] = useState<Task[]>(defaultTasks);
    const [habits, setHabits] = useState<Habit[]>(defaultHabits);
    const [journal, setJournal] = useState<JournalEntry[]>(defaultJournal);
    const [logs, setLogs] = useState<DailyLog[]>([]);
    const [goals, setGoals] = useState<Goal[]>(defaultGoals);
    const [selectedHabitId, setSelectedHabitId] = useState<string | null>(null);
    const [selectedGoalId, setSelectedGoalId] = useState<string | null>(null);
    const [mood, setMood] = useState(3);

    // Load persisted state on mount
    useEffect(() => {
        loadState().then((saved) => {
            if (saved.onboarded !== undefined) setOnboarded(saved.onboarded);
            if (saved.profile) setProfile(saved.profile);
            if (saved.tasks) setTasks(saved.tasks);
            if (saved.habits) setHabits(saved.habits);
            if (saved.journal) setJournal(saved.journal);
            if (saved.logs) setLogs(saved.logs);
            if (saved.goals) setGoals(saved.goals);
            if (saved.mood !== undefined) setMood(saved.mood);
            setLoaded(true);
        });
    }, []);

    // Persist state on changes
    useEffect(() => {
        if (!loaded) return;
        saveState({ onboarded, profile, tasks, habits, journal, logs, goals, mood });
    }, [loaded, onboarded, profile, tasks, habits, journal, logs, goals, mood]);

    const addTask = useCallback((t: Omit<Task, 'id'>) => {
        setTasks((prev) => [...prev, { ...t, id: `t${Date.now()}` }]);
    }, []);

    const updateTask = useCallback((id: string, updates: Partial<Task>) => {
        setTasks((prev) => prev.map((t) => (t.id === id ? { ...t, ...updates } : t)));
    }, []);

    const deleteTask = useCallback((id: string) => {
        setTasks((prev) => prev.filter((t) => t.id !== id));
    }, []);

    const toggleHabit = useCallback((id: string, date: string) => {
        setHabits((prev) =>
            prev.map((h) => {
                if (h.id !== id) return h;
                const completed = h.completedDates.includes(date);
                return {
                    ...h,
                    completedDates: completed ? h.completedDates.filter((d) => d !== date) : [...h.completedDates, date],
                    streak: completed ? Math.max(0, h.streak - 1) : h.streak + 1,
                };
            })
        );
    }, []);

    const addJournalEntry = useCallback((e: Omit<JournalEntry, 'id'>) => {
        setJournal((prev) => [{ ...e, id: `j${Date.now()}` }, ...prev]);
    }, []);

    const addLog = useCallback((text: string) => {
        setLogs((prev) => [{ id: `l${Date.now()}`, text, timestamp: new Date().toISOString() }, ...prev]);
    }, []);

    const addGoal = useCallback((g: Omit<Goal, 'id'>) => {
        setGoals((prev) => [...prev, { ...g, id: `g${Date.now()}` }]);
    }, []);

    const updateGoal = useCallback((id: string, updates: Partial<Goal>) => {
        setGoals((prev) => prev.map((g) => (g.id === id ? { ...g, ...updates } : g)));
    }, []);

    if (!loaded) return null; // Or a loading spinner

    return (
        <AppContext.Provider
            value={{
                onboarded,
                profile,
                tasks,
                habits,
                journal,
                logs,
                goals,
                selectedHabitId,
                selectedGoalId,
                mood,
                setOnboarded,
                setProfile,
                setSelectedHabitId,
                setSelectedGoalId,
                setMood,
                addTask,
                updateTask,
                deleteTask,
                toggleHabit,
                addJournalEntry,
                addLog,
                addGoal,
                updateGoal,
            }}
        >
            {children}
        </AppContext.Provider>
    );
}

export function useApp() {
    const ctx = useContext(AppContext);
    if (!ctx) throw new Error('useApp must be used within AppProvider');
    return ctx;
}
