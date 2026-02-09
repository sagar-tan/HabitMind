import { useState } from "react";
import { motion, AnimatePresence } from "motion/react";
import {
  Brain, Dumbbell, BookOpen, Droplets, Moon, PenLine,
  Flame, ChevronLeft, Camera, ArrowLeft
} from "lucide-react";
import { useApp, Habit } from "../AppContext";

const iconMap: Record<string, React.ElementType> = {
  brain: Brain,
  dumbbell: Dumbbell,
  "book-open": BookOpen,
  droplets: Droplets,
  moon: Moon,
  "pen-line": PenLine,
};

function HabitCard({ habit, isCompleted, onToggle, onSelect }: {
  habit: Habit;
  isCompleted: boolean;
  onToggle: () => void;
  onSelect: () => void;
}) {
  const Icon = iconMap[habit.icon] || Brain;

  return (
    <motion.div
      initial={{ y: 10, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.25, ease: "easeOut" }}
      className="bg-card border border-border rounded-2xl p-4"
    >
      <div className="flex items-start justify-between mb-3">
        <motion.button
          className="flex items-center gap-3 flex-1"
          whileTap={{ scale: 0.98 }}
          onClick={onSelect}
        >
          <div className={`w-10 h-10 rounded-xl flex items-center justify-center transition-colors duration-200 ${
            isCompleted ? "bg-primary/15" : "bg-surface"
          }`}>
            <Icon className={`w-5 h-5 transition-colors duration-200 ${
              isCompleted ? "text-primary" : "text-muted-foreground"
            }`} />
          </div>
          <div className="text-left">
            <p className={`text-sm transition-colors duration-200 ${
              isCompleted ? "text-foreground" : "text-foreground/80"
            }`}>{habit.name}</p>
            <p className="text-xs text-muted-foreground">{habit.category}</p>
          </div>
        </motion.button>

        {/* Streak */}
        <div className="flex items-center gap-1 bg-surface rounded-lg px-2 py-1">
          <Flame className="w-3.5 h-3.5 text-primary" />
          <span className="text-xs text-foreground">{habit.streak}</span>
        </div>
      </div>

      {/* Toggle */}
      <motion.button
        className={`w-full py-3 rounded-xl text-sm transition-all duration-200 ${
          isCompleted
            ? "bg-primary/10 text-primary border border-primary/20"
            : "bg-surface text-muted-foreground border border-transparent active:bg-surface-elevated"
        }`}
        whileTap={{ scale: 0.96 }}
        onClick={onToggle}
      >
        {isCompleted ? "Completed" : "Mark Done"}
      </motion.button>
    </motion.div>
  );
}

function HabitDetail({ habit, onBack }: { habit: Habit; onBack: () => void }) {
  const Icon = iconMap[habit.icon] || Brain;
  const today = new Date().toISOString().split("T")[0];
  const isCompleted = habit.completedDates.includes(today);

  // Generate last 7 days for history
  const last7 = Array.from({ length: 7 }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - (6 - i));
    return d.toISOString().split("T")[0];
  });

  const dayLabels = ["S", "M", "T", "W", "T", "F", "S"];

  // Generate streak visualization (last 30 days)
  const last30 = Array.from({ length: 30 }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - (29 - i));
    return d.toISOString().split("T")[0];
  });

  return (
    <motion.div
      initial={{ x: 60, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      exit={{ x: 60, opacity: 0 }}
      transition={{ duration: 0.3, ease: "easeOut" }}
      className="pb-28 md:pb-8 px-4 md:px-6 pt-[env(safe-area-inset-top,12px)]"
    >
      {/* Header */}
      <div className="flex items-center gap-3 py-4">
        <motion.button
          whileTap={{ scale: 0.9 }}
          onClick={onBack}
          className="w-10 h-10 rounded-xl bg-surface flex items-center justify-center"
        >
          <ArrowLeft className="w-5 h-5 text-foreground" />
        </motion.button>
        <h2 className="text-foreground">{habit.name}</h2>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Habit Info */}
        <div className="bg-card border border-border rounded-2xl p-5">
          <div className="flex items-center gap-4 mb-4">
            <div className={`w-14 h-14 rounded-2xl flex items-center justify-center ${
              isCompleted ? "bg-primary/15" : "bg-surface"
            }`}>
              <Icon className={`w-7 h-7 ${isCompleted ? "text-primary" : "text-muted-foreground"}`} />
            </div>
            <div>
              <p className="text-foreground">{habit.name}</p>
              <p className="text-muted-foreground text-sm">{habit.category}</p>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="bg-surface rounded-xl p-3 text-center">
              <div className="flex items-center justify-center gap-1 mb-1">
                <Flame className="w-4 h-4 text-primary" />
                <span className="text-foreground text-xl">{habit.streak}</span>
              </div>
              <p className="text-muted-foreground text-xs">Day streak</p>
            </div>
            <div className="bg-surface rounded-xl p-3 text-center">
              <span className="text-foreground text-xl">{habit.completedDates.length}</span>
              <p className="text-muted-foreground text-xs">Total days</p>
            </div>
          </div>
        </div>

        {/* Streak Visualization */}
        <div className="bg-card border border-border rounded-2xl p-5">
          <h3 className="text-foreground mb-3">Streak Map</h3>
          <div className="flex flex-wrap gap-1.5">
            {last30.map((date, i) => (
              <motion.div
                key={date}
                initial={{ scale: 0, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                transition={{ delay: i * 0.015, duration: 0.2 }}
                className={`w-[calc((100%-10*6px)/10)] aspect-square rounded-md ${
                  habit.completedDates.includes(date)
                    ? "bg-primary/60"
                    : "bg-surface"
                }`}
              />
            ))}
          </div>
        </div>

        {/* Weekly History */}
        <div className="bg-card border border-border rounded-2xl p-5">
          <h3 className="text-foreground mb-3">This Week</h3>
          <div className="flex justify-between">
            {last7.map((date, i) => {
              const done = habit.completedDates.includes(date);
              const isToday = date === today;
              return (
                <div key={date} className="flex flex-col items-center gap-2">
                  <span className={`text-xs ${isToday ? "text-primary" : "text-muted-foreground"}`}>
                    {dayLabels[new Date(date).getDay()]}
                  </span>
                  <div className={`w-8 h-8 rounded-lg flex items-center justify-center transition-colors ${
                    done
                      ? "bg-primary/20 text-primary"
                      : isToday
                      ? "bg-surface border border-primary/30"
                      : "bg-surface"
                  }`}>
                    {done && (
                      <motion.svg
                        className="w-4 h-4"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth={3}
                      >
                        <path d="M5 13l4 4L19 7" />
                      </motion.svg>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Image Gallery placeholder */}
        <div className="bg-card border border-border rounded-2xl p-5">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-foreground">Progress Photos</h3>
            <motion.button
              whileTap={{ scale: 0.9 }}
              className="w-10 h-10 rounded-xl bg-surface flex items-center justify-center"
            >
              <Camera className="w-5 h-5 text-muted-foreground" />
            </motion.button>
          </div>
          <div className="grid grid-cols-5 gap-2">
            {["Front", "Left", "Right", "Back", "Face"].map((label) => (
              <div key={label} className="aspect-square rounded-xl bg-surface flex flex-col items-center justify-center gap-1">
                <Camera className="w-4 h-4 text-muted-foreground/40" />
                <span className="text-[9px] text-muted-foreground/40">{label}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </motion.div>
  );
}

export function HabitsScreen() {
  const { habits, toggleHabit, selectedHabitId, setSelectedHabitId, setActiveScreen, activeScreen } = useApp();
  const today = new Date().toISOString().split("T")[0];

  const selectedHabit = habits.find(h => h.id === selectedHabitId);

  if (activeScreen === "habit-detail" && selectedHabit) {
    return (
      <HabitDetail
        habit={selectedHabit}
        onBack={() => {
          setSelectedHabitId(null);
          setActiveScreen("main");
        }}
      />
    );
  }

  const completedCount = habits.filter(h => h.completedDates.includes(today)).length;

  return (
    <div className="pb-28 md:pb-8 px-4 md:px-6 pt-[env(safe-area-inset-top,12px)]">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="flex items-center justify-between py-4"
      >
        <h1 className="text-foreground">Habits</h1>
        <div className="bg-surface rounded-full py-1.5 px-3 flex items-center gap-1.5">
          <Flame className="w-4 h-4 text-primary" />
          <span className="text-sm text-foreground">{completedCount}/{habits.length}</span>
        </div>
      </motion.div>

      {/* Progress */}
      <motion.div
        initial={{ y: 10, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ delay: 0.05, duration: 0.3 }}
        className="mb-5"
      >
        <div className="w-full h-2 bg-surface rounded-full overflow-hidden">
          <motion.div
            className="h-full bg-primary rounded-full"
            initial={{ width: 0 }}
            animate={{ width: `${(completedCount / habits.length) * 100}%` }}
            transition={{ duration: 0.6, ease: "easeOut" }}
          />
        </div>
      </motion.div>

      {/* Habits Grid - 2 columns on tablet */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {habits.map((habit, i) => (
          <motion.div
            key={habit.id}
            initial={{ y: 15, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.05 + i * 0.04, duration: 0.25, ease: "easeOut" }}
          >
            <HabitCard
              habit={habit}
              isCompleted={habit.completedDates.includes(today)}
              onToggle={() => toggleHabit(habit.id, today)}
              onSelect={() => {
                setSelectedHabitId(habit.id);
                setActiveScreen("habit-detail");
              }}
            />
          </motion.div>
        ))}
      </div>
    </div>
  );
}