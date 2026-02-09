import { useState, useEffect } from "react";
import { motion } from "motion/react";
import { BarChart3, TrendingUp, CalendarCheck, Clock, ChevronDown, Target, ChevronRight, Flame } from "lucide-react";
import { useApp } from "../AppContext";
import {
  LineChart, Line, BarChart, Bar, XAxis, YAxis, Tooltip,
  ResponsiveContainer, CartesianGrid, Area, AreaChart
} from "recharts";

function AnimatedNumber({ value, suffix = "" }: { value: number; suffix?: string }) {
  const [display, setDisplay] = useState(0);
  useEffect(() => {
    const duration = 600;
    const startTime = Date.now();
    const tick = () => {
      const elapsed = Date.now() - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      setDisplay(Math.round(value * eased));
      if (progress < 1) requestAnimationFrame(tick);
    };
    requestAnimationFrame(tick);
  }, [value]);
  return <span>{display}{suffix}</span>;
}

const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload?.length) {
    return (
      <div className="bg-surface-elevated border border-border rounded-xl px-3 py-2 shadow-lg">
        <p className="text-xs text-muted-foreground">{label}</p>
        <p className="text-sm text-primary">{payload[0].value}%</p>
      </div>
    );
  }
  return null;
};

export function InsightsScreen() {
  const { habits, tasks, goals, theme } = useApp();
  const [timeRange, setTimeRange] = useState<"week" | "month">("week");

  // Theme-aware chart colors (monochromatic)
  const chartPrimary = theme === "dark" ? "#D4D4D8" : "#27272A";
  const chartSecondary = theme === "dark" ? "#8A8A95" : "#52525B";
  const chartGrid = theme === "dark" ? "rgba(255,255,255,0.04)" : "rgba(0,0,0,0.06)";
  const chartTickFill = theme === "dark" ? "#71717A" : "#A1A1AA";

  const today = new Date().toISOString().split("T")[0];
  const totalHabits = habits.length;
  const completedHabits = habits.filter(h => h.completedDates.includes(today)).length;
  const consistencyScore = totalHabits > 0 ? Math.round((completedHabits / totalHabits) * 100) : 0;

  const completedTasks = tasks.filter(t => t.completed).length;
  const totalTasks = tasks.length;
  const planningAccuracy = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;

  // Mock weekly data
  const weeklyHabitData = [
    { day: "Mon", value: 67 },
    { day: "Tue", value: 83 },
    { day: "Wed", value: 50 },
    { day: "Thu", value: 100 },
    { day: "Fri", value: 67 },
    { day: "Sat", value: 83 },
    { day: "Sun", value: consistencyScore },
  ];

  const monthlyHabitData = [
    { week: "W1", value: 72 },
    { week: "W2", value: 68 },
    { week: "W3", value: 85 },
    { week: "W4", value: 78 },
  ];

  const taskRolloverData = [
    { day: "Mon", completed: 5, rolled: 1 },
    { day: "Tue", completed: 4, rolled: 2 },
    { day: "Wed", completed: 6, rolled: 0 },
    { day: "Thu", completed: 3, rolled: 3 },
    { day: "Fri", completed: 5, rolled: 1 },
    { day: "Sat", completed: 2, rolled: 1 },
    { day: "Sun", completed: 4, rolled: 0 },
  ];

  const productivityData = [
    { hour: "6am", value: 20 },
    { hour: "8am", value: 65 },
    { hour: "10am", value: 90 },
    { hour: "12pm", value: 70 },
    { hour: "2pm", value: 55 },
    { hour: "4pm", value: 75 },
    { hour: "6pm", value: 45 },
    { hour: "8pm", value: 30 },
  ];

  const chartData = timeRange === "week" ? weeklyHabitData : monthlyHabitData;
  const xKey = timeRange === "week" ? "day" : "week";

  return (
    <div className="pb-28 md:pb-8 px-4 md:px-6 pt-[env(safe-area-inset-top,12px)]">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="flex items-center justify-between py-4"
      >
        <h1 className="text-foreground">Insights</h1>
        <motion.button
          whileTap={{ scale: 0.95 }}
          className="flex items-center gap-1 bg-surface rounded-xl px-4 py-2.5 text-sm text-muted-foreground active:bg-surface-elevated"
          onClick={() => setTimeRange(timeRange === "week" ? "month" : "week")}
        >
          {timeRange === "week" ? "This Week" : "This Month"}
          <ChevronDown className="w-4 h-4" />
        </motion.button>
      </motion.div>

      {/* Overview Cards */}
      <motion.div
        initial={{ y: 15, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ delay: 0.05, duration: 0.3 }}
        className="grid grid-cols-3 gap-3 mb-5"
      >
        <div className="bg-card border border-border rounded-2xl p-3 md:p-4 text-center">
          <TrendingUp className="w-5 h-5 text-primary mx-auto mb-2" />
          <div className="text-foreground text-xl">
            <AnimatedNumber value={consistencyScore} suffix="%" />
          </div>
          <p className="text-muted-foreground text-[10px]">Consistency</p>
        </div>
        <div className="bg-card border border-border rounded-2xl p-3 md:p-4 text-center">
          <CalendarCheck className="w-5 h-5 text-primary mx-auto mb-2" />
          <div className="text-foreground text-xl">
            <AnimatedNumber value={Math.round((completedHabits / Math.max(totalHabits, 1)) * 100)} suffix="%" />
          </div>
          <p className="text-muted-foreground text-[10px]">Adherence</p>
        </div>
        <div className="bg-card border border-border rounded-2xl p-3 md:p-4 text-center">
          <BarChart3 className="w-5 h-5 text-primary mx-auto mb-2" />
          <div className="text-foreground text-xl">
            <AnimatedNumber value={planningAccuracy} suffix="%" />
          </div>
          <p className="text-muted-foreground text-[10px]">Accuracy</p>
        </div>
      </motion.div>

      {/* Charts grid - 2 col on tablet */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
        {/* Habit Trends Chart */}
        <motion.div
          initial={{ y: 15, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.1, duration: 0.3 }}
          className="bg-card border border-border rounded-2xl p-5"
        >
          <h3 className="text-foreground mb-4">Habit Trends</h3>
          <div className="h-44">
            <ResponsiveContainer width="100%" height="100%" minWidth={0} minHeight={0}>
              <AreaChart data={chartData}>
                <defs>
                  <linearGradient id="habitGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor={chartPrimary} stopOpacity={0.2} />
                    <stop offset="100%" stopColor={chartPrimary} stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid stroke={chartGrid} strokeDasharray="3 3" />
                <XAxis
                  dataKey={xKey}
                  axisLine={false}
                  tickLine={false}
                  tick={{ fill: chartTickFill, fontSize: 11 }}
                />
                <YAxis
                  axisLine={false}
                  tickLine={false}
                  tick={{ fill: chartTickFill, fontSize: 11 }}
                  domain={[0, 100]}
                />
                <Tooltip content={<CustomTooltip />} />
                <Area
                  type="monotone"
                  dataKey="value"
                  stroke={chartPrimary}
                  strokeWidth={2}
                  fill="url(#habitGradient)"
                  animationDuration={800}
                  animationEasing="ease-out"
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </motion.div>

        {/* Task Rollover */}
        <motion.div
          initial={{ y: 15, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.15, duration: 0.3 }}
          className="bg-card border border-border rounded-2xl p-5"
        >
          <h3 className="text-foreground mb-4">Task Completion</h3>
          <div className="h-36 md:h-44">
            <ResponsiveContainer width="100%" height="100%" minWidth={0} minHeight={0}>
              <BarChart data={taskRolloverData} barGap={2}>
                <CartesianGrid stroke={chartGrid} strokeDasharray="3 3" />
                <XAxis
                  dataKey="day"
                  axisLine={false}
                  tickLine={false}
                  tick={{ fill: chartTickFill, fontSize: 11 }}
                />
                <YAxis
                  axisLine={false}
                  tickLine={false}
                  tick={{ fill: chartTickFill, fontSize: 11 }}
                />
                <Tooltip content={<CustomTooltip />} />
                <Bar
                  dataKey="completed"
                  fill={chartPrimary}
                  radius={[4, 4, 0, 0]}
                  animationDuration={800}
                  animationEasing="ease-out"
                />
                <Bar
                  dataKey="rolled"
                  fill={chartSecondary}
                  opacity={0.3}
                  radius={[4, 4, 0, 0]}
                  animationDuration={800}
                  animationEasing="ease-out"
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
          <div className="flex gap-4 mt-3 text-xs">
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded-sm bg-primary" />
              <span className="text-muted-foreground">Completed</span>
            </div>
            <div className="flex items-center gap-1.5">
              <div className="w-2.5 h-2.5 rounded-sm bg-primary/20" />
              <span className="text-muted-foreground">Rolled over</span>
            </div>
          </div>
        </motion.div>
      </div>

      {/* Productivity Times */}
      <motion.div
        initial={{ y: 15, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ delay: 0.2, duration: 0.3 }}
        className="bg-card border border-border rounded-2xl p-5 mb-4"
      >
        <h3 className="text-foreground mb-1">Peak Productivity</h3>
        <p className="text-muted-foreground text-xs mb-4">When you get the most done</p>
        <div className="h-36 md:h-44">
          <ResponsiveContainer width="100%" height="100%" minWidth={0} minHeight={0}>
            <AreaChart data={productivityData}>
              <defs>
                <linearGradient id="prodGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor={chartSecondary} stopOpacity={0.25} />
                  <stop offset="100%" stopColor={chartSecondary} stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid stroke={chartGrid} strokeDasharray="3 3" />
              <XAxis
                dataKey="hour"
                axisLine={false}
                tickLine={false}
                tick={{ fill: chartTickFill, fontSize: 10 }}
              />
              <YAxis
                axisLine={false}
                tickLine={false}
                tick={{ fill: chartTickFill, fontSize: 11 }}
                domain={[0, 100]}
              />
              <Tooltip content={<CustomTooltip />} />
              <Area
                type="monotone"
                dataKey="value"
                stroke={chartSecondary}
                strokeWidth={2}
                fill="url(#prodGradient)"
                animationDuration={800}
                animationEasing="ease-out"
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </motion.div>

      {/* Goals Section */}
      <motion.div
        initial={{ y: 15, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ delay: 0.25, duration: 0.3 }}
        className="mb-4"
      >
        <h3 className="text-foreground mb-3">Goals</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {goals.map((goal, i) => (
            <motion.div
              key={goal.id}
              initial={{ y: 10, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              transition={{ delay: 0.25 + i * 0.04, duration: 0.25 }}
              className="bg-card border border-border rounded-2xl p-4"
            >
              <div className="flex items-center gap-3 mb-3">
                <div className="w-9 h-9 rounded-xl bg-primary/10 flex items-center justify-center">
                  <Target className="w-4.5 h-4.5 text-primary" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-foreground text-sm">{goal.title}</p>
                  <p className="text-muted-foreground text-xs">{goal.progress}% complete</p>
                </div>
              </div>
              <div className="w-full h-1.5 bg-surface rounded-full overflow-hidden">
                <motion.div
                  className="h-full bg-primary rounded-full"
                  initial={{ width: 0 }}
                  animate={{ width: `${goal.progress}%` }}
                  transition={{ duration: 0.6, ease: "easeOut", delay: 0.3 }}
                />
              </div>
            </motion.div>
          ))}
        </div>
      </motion.div>
    </div>
  );
}