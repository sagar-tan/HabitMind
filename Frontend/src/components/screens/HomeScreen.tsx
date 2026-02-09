import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import {
  Smile, Meh, Frown, Laugh, Annoyed,
  PenLine, ListPlus, StickyNote, Settings, ChevronRight,
  Clock, CheckCircle2
} from "lucide-react";
import { useApp } from "../AppContext";

const moodIcons = [Frown, Annoyed, Meh, Smile, Laugh];
const moodLabels = ["Rough", "Low", "Okay", "Good", "Great"];

function formatDate(date: Date) {
  const days = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
  const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
  return {
    day: days[date.getDay()],
    date: `${months[date.getMonth()]} ${date.getDate()}`,
    full: `${days[date.getDay()]}, ${months[date.getMonth()]} ${date.getDate()}`
  };
}

function AnimatedNumber({ value, suffix = "" }: { value: number; suffix?: string }) {
  const [display, setDisplay] = useState(0);
  useEffect(() => {
    let start = 0;
    const end = value;
    const duration = 600;
    const startTime = Date.now();
    const tick = () => {
      const elapsed = Date.now() - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      setDisplay(Math.round(start + (end - start) * eased));
      if (progress < 1) requestAnimationFrame(tick);
    };
    requestAnimationFrame(tick);
  }, [value]);
  return <span>{display}{suffix}</span>;
}

export function HomeScreen() {
  const { tasks, habits, mood, setMood, setActiveTab, setActiveScreen } = useApp();
  const now = new Date();
  const dateInfo = formatDate(now);
  const today = now.toISOString().split("T")[0];

  const todayTasks = tasks.filter(t => t.date === today);
  const completedTasks = todayTasks.filter(t => t.completed).length;
  const taskProgress = todayTasks.length > 0
    ? Math.round(todayTasks.reduce((sum, t) => sum + t.progress, 0) / todayTasks.length)
    : 0;
  const habitsCompleted = habits.filter(h => h.completedDates.includes(today)).length;

  const currentHour = now.getHours();

  // Simulated schedule blocks
  const scheduleBlocks = [
    { time: "7:00", label: "Morning routine", done: currentHour > 8 },
    { time: "8:00", label: "Deep work", done: currentHour > 10 },
    { time: "10:30", label: "Break & stretch", done: currentHour > 11 },
    { time: "11:00", label: "Tasks & reviews", done: currentHour > 12 },
    { time: "13:00", label: "Afternoon block", done: false },
    { time: "15:00", label: "Exercise", done: false },
    { time: "17:00", label: "Wind down", done: false },
  ];

  const currentBlockIdx = scheduleBlocks.findIndex(b => !b.done);

  return (
    <div className="pb-28 md:pb-8 px-4 md:px-6 pt-[env(safe-area-inset-top,12px)]">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.3, ease: "easeOut" }}
        className="flex items-center justify-between py-4"
      >
        <div>
          <p className="text-muted-foreground text-sm">{dateInfo.day}</p>
          <h1 className="text-foreground">{dateInfo.date}</h1>
        </div>
        <div className="flex items-center gap-3">
          {/* Mood indicator */}
          <div className="flex items-center gap-1.5 bg-surface rounded-full py-1.5 px-3">
            {(() => {
              const MoodIcon = moodIcons[mood - 1];
              return <MoodIcon className="w-4 h-4 text-primary" />;
            })()}
            <span className="text-xs text-muted-foreground">{moodLabels[mood - 1]}</span>
          </div>
          <motion.button
            whileTap={{ scale: 0.94 }}
            className="w-10 h-10 rounded-xl bg-surface flex items-center justify-center"
            onClick={() => setActiveScreen("settings")}
          >
            <Settings className="w-5 h-5 text-muted-foreground" />
          </motion.button>
        </div>
      </motion.div>

      {/* Two-column layout on tablet */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Left column */}
        <div className="space-y-4">
          {/* Mood Selector */}
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.05, duration: 0.3, ease: "easeOut" }}
            className="bg-card rounded-2xl p-4 border border-border"
          >
            <p className="text-muted-foreground text-sm mb-3">How are you feeling?</p>
            <div className="flex justify-between">
              {moodIcons.map((Icon, i) => (
                <motion.button
                  key={i}
                  whileTap={{ scale: 0.9 }}
                  onClick={() => setMood(i + 1)}
                  className={`w-12 h-12 rounded-xl flex items-center justify-center transition-all duration-200 ${
                    mood === i + 1
                      ? "bg-primary/15 text-primary"
                      : "bg-surface text-muted-foreground active:text-foreground active:bg-surface-elevated"
                  }`}
                >
                  <Icon className="w-6 h-6" />
                </motion.button>
              ))}
            </div>
          </motion.div>

          {/* Today Summary Card */}
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.1, duration: 0.3, ease: "easeOut" }}
            className="bg-card rounded-2xl p-5 border border-border"
          >
            <h3 className="text-foreground mb-4">Today's Progress</h3>
            <div className="grid grid-cols-3 gap-3">
              {/* Habits */}
              <div className="bg-surface rounded-xl p-3 text-center">
                <div className="text-primary text-2xl mb-1">
                  <AnimatedNumber value={habitsCompleted} />
                  <span className="text-muted-foreground text-sm">/{habits.length}</span>
                </div>
                <p className="text-muted-foreground text-xs">Habits</p>
              </div>
              {/* Tasks */}
              <div className="bg-surface rounded-xl p-3 text-center">
                <div className="text-primary text-2xl mb-1">
                  <AnimatedNumber value={taskProgress} suffix="%" />
                </div>
                <p className="text-muted-foreground text-xs">Tasks</p>
              </div>
              {/* Schedule */}
              <div className="bg-surface rounded-xl p-3 text-center">
                <div className="text-primary text-2xl mb-1">
                  <AnimatedNumber value={completedTasks} />
                  <span className="text-muted-foreground text-sm">/{todayTasks.length}</span>
                </div>
                <p className="text-muted-foreground text-xs">Done</p>
              </div>
            </div>

            {/* Overall progress bar */}
            <div className="mt-4">
              <div className="flex justify-between text-xs text-muted-foreground mb-1.5">
                <span>Overall</span>
                <span>{taskProgress}%</span>
              </div>
              <div className="w-full h-2 bg-surface rounded-full overflow-hidden">
                <motion.div
                  className="h-full bg-primary rounded-full"
                  initial={{ width: 0 }}
                  animate={{ width: `${taskProgress}%` }}
                  transition={{ duration: 0.8, ease: "easeOut", delay: 0.3 }}
                />
              </div>
            </div>
          </motion.div>

          {/* Quick Actions */}
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.2, duration: 0.3, ease: "easeOut" }}
            className="grid grid-cols-3 gap-3"
          >
            {[
              { icon: PenLine, label: "Journal", action: () => { setActiveTab("journal"); setActiveScreen("journal-entry"); } },
              { icon: ListPlus, label: "Add Task", action: () => setActiveTab("plan") },
              { icon: StickyNote, label: "Quick Note", action: () => setActiveTab("journal") },
            ].map((item, i) => (
              <motion.button
                key={i}
                whileTap={{ scale: 0.94 }}
                className="bg-card border border-border rounded-2xl p-4 flex flex-col items-center gap-2"
                onClick={item.action}
              >
                <item.icon className="w-5 h-5 text-primary" />
                <span className="text-xs text-muted-foreground">{item.label}</span>
              </motion.button>
            ))}
          </motion.div>
        </div>

        {/* Right column */}
        <div className="space-y-4">
          {/* Schedule Section */}
          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.15, duration: 0.3, ease: "easeOut" }}
            className="bg-card rounded-2xl p-5 border border-border"
          >
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-foreground">Schedule</h3>
              <span className="text-xs text-muted-foreground flex items-center gap-1">
                <Clock className="w-3.5 h-3.5" />
                {now.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
              </span>
            </div>
            <div className="space-y-1">
              {scheduleBlocks.map((block, i) => {
                const isCurrent = i === currentBlockIdx;
                return (
                  <motion.div
                    key={i}
                    initial={{ x: -10, opacity: 0 }}
                    animate={{ x: 0, opacity: 1 }}
                    transition={{ delay: 0.2 + i * 0.04, duration: 0.25, ease: "easeOut" }}
                    className={`flex items-center gap-3 py-2.5 px-3 rounded-xl transition-all ${
                      isCurrent
                        ? "bg-primary/8 border border-primary/20"
                        : block.done
                        ? "opacity-50"
                        : ""
                    }`}
                  >
                    <span className={`text-xs w-12 ${isCurrent ? "text-primary" : "text-muted-foreground"}`}>
                      {block.time}
                    </span>
                    <div className={`w-2 h-2 rounded-full ${
                      block.done ? "bg-primary/50" : isCurrent ? "bg-primary" : "bg-muted-foreground/30"
                    }`} />
                    <span className={`flex-1 text-sm ${
                      isCurrent ? "text-foreground" : block.done ? "text-muted-foreground line-through" : "text-foreground/70"
                    }`}>
                      {block.label}
                    </span>
                    {block.done && <CheckCircle2 className="w-4 h-4 text-primary/50" />}
                    {isCurrent && (
                      <motion.div
                        className="w-1.5 h-1.5 rounded-full bg-primary"
                        animate={{ opacity: [1, 0.3, 1] }}
                        transition={{ duration: 2, repeat: Infinity }}
                      />
                    )}
                  </motion.div>
                );
              })}
            </div>
          </motion.div>

          {/* Weekly Review CTA */}
          <motion.button
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.25, duration: 0.3, ease: "easeOut" }}
            whileTap={{ scale: 0.98 }}
            className="w-full bg-primary/8 border border-primary/15 rounded-2xl p-4 flex items-center justify-between"
            onClick={() => setActiveScreen("weekly-review")}
          >
            <div>
              <p className="text-foreground text-sm">Weekly Review</p>
              <p className="text-muted-foreground text-xs">Reflect and plan ahead</p>
            </div>
            <ChevronRight className="w-5 h-5 text-primary" />
          </motion.button>
        </div>
      </div>
    </div>
  );
}