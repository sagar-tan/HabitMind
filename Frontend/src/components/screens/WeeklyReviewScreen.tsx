import { useState } from "react";
import { motion } from "motion/react";
import { ArrowLeft, CheckCircle2, ArrowRight, Flame, Target, PenLine } from "lucide-react";
import { useApp } from "../AppContext";

export function WeeklyReviewScreen() {
  const { tasks, habits, goals, setActiveScreen, updateTask } = useApp();
  const [reflection, setReflection] = useState("");
  const [submitted, setSubmitted] = useState(false);

  const today = new Date().toISOString().split("T")[0];
  const completedTasks = tasks.filter(t => t.completed);
  const incompleteTasks = tasks.filter(t => !t.completed);
  const habitsCompleted = habits.filter(h => h.completedDates.includes(today)).length;

  const weekSummary = {
    tasksCompleted: completedTasks.length,
    tasksTotal: tasks.length,
    habitsRate: habits.length > 0 ? Math.round((habitsCompleted / habits.length) * 100) : 0,
    topStreak: Math.max(...habits.map(h => h.streak), 0),
  };

  const handleSubmit = () => {
    setSubmitted(true);
    // Carry forward incomplete tasks
    incompleteTasks.forEach(t => {
      const nextDay = new Date();
      nextDay.setDate(nextDay.getDate() + 1);
      updateTask(t.id, { date: nextDay.toISOString().split("T")[0] });
    });
  };

  if (submitted) {
    return (
      <div className="min-h-screen bg-background flex flex-col items-center justify-center px-6">
        <motion.div
          initial={{ scale: 0.8, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ duration: 0.4, ease: "easeOut" }}
          className="text-center"
        >
          <div className="w-20 h-20 rounded-2xl bg-primary/10 flex items-center justify-center mx-auto mb-6">
            <CheckCircle2 className="w-10 h-10 text-primary" />
          </div>
          <h2 className="text-foreground mb-2">Week reviewed</h2>
          <p className="text-muted-foreground mb-8">Incomplete tasks moved to tomorrow.</p>
          <motion.button
            className="bg-primary text-primary-foreground px-8 py-4 rounded-2xl"
            whileTap={{ scale: 0.96 }}
            onClick={() => setActiveScreen("main")}
          >
            Back to Home
          </motion.button>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="pb-28 md:pb-8 px-4 md:px-6 pt-[env(safe-area-inset-top,12px)]">
      {/* Header */}
      <div className="flex items-center gap-3 py-4">
        <motion.button
          whileTap={{ scale: 0.9 }}
          onClick={() => setActiveScreen("main")}
          className="w-10 h-10 rounded-xl bg-surface flex items-center justify-center"
        >
          <ArrowLeft className="w-5 h-5 text-foreground" />
        </motion.button>
        <h2 className="text-foreground">Weekly Review</h2>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Week Summary */}
        <motion.div
          initial={{ y: 15, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.05, duration: 0.3 }}
          className="bg-card border border-border rounded-2xl p-5"
        >
          <h3 className="text-foreground mb-4">Week Summary</h3>
          <div className="grid grid-cols-3 gap-3">
            <div className="bg-surface rounded-xl p-3 text-center">
              <Target className="w-5 h-5 text-primary mx-auto mb-1" />
              <p className="text-foreground text-lg">{weekSummary.tasksCompleted}/{weekSummary.tasksTotal}</p>
              <p className="text-muted-foreground text-[10px]">Tasks done</p>
            </div>
            <div className="bg-surface rounded-xl p-3 text-center">
              <CheckCircle2 className="w-5 h-5 text-primary mx-auto mb-1" />
              <p className="text-foreground text-lg">{weekSummary.habitsRate}%</p>
              <p className="text-muted-foreground text-[10px]">Habits</p>
            </div>
            <div className="bg-surface rounded-xl p-3 text-center">
              <Flame className="w-5 h-5 text-primary mx-auto mb-1" />
              <p className="text-foreground text-lg">{weekSummary.topStreak}</p>
              <p className="text-muted-foreground text-[10px]">Top streak</p>
            </div>
          </div>
        </motion.div>

        {/* Reflection */}
        <motion.div
          initial={{ y: 15, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.15, duration: 0.3 }}
          className="bg-card border border-border rounded-2xl p-5"
        >
          <div className="flex items-center gap-2 mb-3">
            <PenLine className="w-5 h-5 text-primary" />
            <h3 className="text-foreground">Reflection</h3>
          </div>
          <textarea
            value={reflection}
            onChange={(e) => setReflection(e.target.value)}
            placeholder="How did the week go? What would you change?"
            className="w-full h-28 py-3 px-4 rounded-xl bg-surface border border-border text-foreground placeholder:text-muted-foreground/40 focus:border-primary/50 focus:outline-none resize-none"
          />
        </motion.div>

        {/* Task Status */}
        <motion.div
          initial={{ y: 15, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.1, duration: 0.3 }}
          className="bg-card border border-border rounded-2xl p-5 md:col-span-2"
        >
          <h3 className="text-foreground mb-3">Tasks</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-x-4">
            {tasks.map(task => (
              <div key={task.id} className="flex items-center gap-3 py-2">
                <div className={`w-5 h-5 rounded-md border-2 flex items-center justify-center ${
                  task.completed ? "bg-primary border-primary" : "border-muted-foreground/30"
                }`}>
                  {task.completed && (
                    <svg className="w-3 h-3 text-primary-foreground" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={3}>
                      <path d="M5 13l4 4L19 7" />
                    </svg>
                  )}
                </div>
                <span className={`text-sm flex-1 ${
                  task.completed ? "text-muted-foreground line-through" : "text-foreground"
                }`}>{task.title}</span>
                {!task.completed && (
                  <span className="text-[10px] text-primary bg-primary/10 px-2 py-0.5 rounded-md">carry</span>
                )}
              </div>
            ))}
          </div>
        </motion.div>
      </div>

      {/* Submit */}
      <motion.button
        initial={{ y: 15, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ delay: 0.2, duration: 0.3 }}
        className="w-full mt-4 bg-primary text-primary-foreground py-4 rounded-2xl flex items-center justify-center gap-2"
        whileTap={{ scale: 0.96 }}
        onClick={handleSubmit}
      >
        Complete Review
        <ArrowRight className="w-5 h-5" />
      </motion.button>
    </div>
  );
}
