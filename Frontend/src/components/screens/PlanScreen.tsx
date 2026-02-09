import { useState } from "react";
import { motion, AnimatePresence, useMotionValue, useTransform, PanInfo } from "motion/react";
import { Plus, GanttChart, List, Clock, X } from "lucide-react";
import { useApp, Task } from "../AppContext";

function TaskCard({ task, onUpdate, onDelete }: {
  task: Task;
  onUpdate: (id: string, updates: Partial<Task>) => void;
  onDelete: (id: string) => void;
}) {
  const x = useMotionValue(0);
  const opacity = useTransform(x, [-120, -60, 0], [0.5, 0.8, 1]);
  const bgOpacity = useTransform(x, [-120, -60, 0], [1, 0.5, 0]);
  const [isDragging, setIsDragging] = useState(false);

  const handleDragEnd = (_: any, info: PanInfo) => {
    setIsDragging(false);
    if (info.offset.x < -80) {
      onDelete(task.id);
    }
  };

  return (
    <div className="relative overflow-hidden rounded-2xl mb-2">
      {/* Delete background */}
      <motion.div
        className="absolute inset-0 bg-destructive/20 rounded-2xl flex items-center justify-end pr-5"
        style={{ opacity: bgOpacity }}
      >
        <span className="text-destructive-foreground text-sm">Defer</span>
      </motion.div>

      <motion.div
        className="relative bg-card border border-border rounded-2xl p-4"
        style={{ x, opacity }}
        drag="x"
        dragConstraints={{ left: -120, right: 0 }}
        dragElastic={0.1}
        onDragStart={() => setIsDragging(true)}
        onDragEnd={handleDragEnd}
      >
        <div className="flex items-start gap-3">
          {/* Checkbox */}
          <motion.button
            className={`w-6 h-6 rounded-lg border-2 flex items-center justify-center mt-0.5 shrink-0 transition-all duration-200 relative after:absolute after:content-[''] after:-inset-[10px] ${
              task.completed ? "bg-primary border-primary" : "border-muted-foreground/30"
            }`}
            whileTap={{ scale: 0.85 }}
            onClick={() => onUpdate(task.id, {
              completed: !task.completed,
              progress: task.completed ? task.progress : 100,
            })}
          >
            {task.completed && (
              <motion.svg
                initial={{ pathLength: 0 }}
                animate={{ pathLength: 1 }}
                transition={{ duration: 0.2 }}
                className="w-3.5 h-3.5 text-primary-foreground"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth={3}
              >
                <path d="M5 13l4 4L19 7" />
              </motion.svg>
            )}
          </motion.button>

          <div className="flex-1 min-w-0">
            <p className={`text-foreground text-sm mb-1 transition-all duration-200 ${
              task.completed ? "line-through text-muted-foreground" : ""
            }`}>
              {task.title}
            </p>
            <div className="flex items-center gap-2 text-xs text-muted-foreground">
              <Clock className="w-3 h-3" />
              <span>{task.timeEstimate}min</span>
              <span className="text-primary">{task.progress}%</span>
            </div>

            {/* Progress slider */}
            {!task.completed && (
              <div className="mt-2.5">
                <div className="relative w-full h-1.5 bg-surface rounded-full overflow-hidden">
                  <motion.div
                    className="absolute left-0 top-0 h-full bg-primary rounded-full"
                    animate={{ width: `${task.progress}%` }}
                    transition={{ duration: 0.15, ease: "easeOut" }}
                  />
                </div>
                <input
                  type="range"
                  min={0}
                  max={100}
                  step={5}
                  value={task.progress}
                  onChange={(e) => {
                    const val = Number(e.target.value);
                    onUpdate(task.id, {
                      progress: val,
                      completed: val === 100,
                    });
                  }}
                  className="w-full h-10 -mt-5 opacity-0 cursor-pointer relative z-10 touch-none"
                  style={{ WebkitAppearance: "none" }}
                />
              </div>
            )}
          </div>
        </div>
      </motion.div>
    </div>
  );
}

function GanttView({ tasks }: { tasks: Task[] }) {
  const totalMinutes = tasks.reduce((sum, t) => sum + t.timeEstimate, 0);
  return (
    <div className="space-y-2.5">
      <div className="flex items-center justify-between text-xs text-muted-foreground mb-3">
        <span>0h</span>
        <span>{Math.round(totalMinutes / 60 * 10) / 10}h total</span>
      </div>
      {tasks.map((task, i) => {
        const widthPercent = Math.max((task.timeEstimate / Math.max(totalMinutes, 1)) * 100, 15);
        return (
          <motion.div
            key={task.id}
            initial={{ x: -20, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            transition={{ delay: i * 0.05, duration: 0.3, ease: "easeOut" }}
            className="flex items-center gap-3"
          >
            <span className="text-xs text-muted-foreground w-20 md:w-28 truncate">{task.title}</span>
            <div className="flex-1 h-8 bg-surface rounded-lg overflow-hidden relative">
              <motion.div
                className="absolute left-0 top-0 h-full bg-primary/20 rounded-lg"
                initial={{ width: 0 }}
                animate={{ width: `${widthPercent}%` }}
                transition={{ duration: 0.5, ease: "easeOut", delay: i * 0.05 }}
              />
              <motion.div
                className="absolute left-0 top-0 h-full bg-primary/50 rounded-lg"
                initial={{ width: 0 }}
                animate={{ width: `${widthPercent * task.progress / 100}%` }}
                transition={{ duration: 0.6, ease: "easeOut", delay: 0.2 + i * 0.05 }}
              />
              <div className="absolute inset-0 flex items-center px-2">
                <span className="text-xs text-foreground/80">{task.timeEstimate}m</span>
              </div>
            </div>
          </motion.div>
        );
      })}
    </div>
  );
}

function AddTaskSheet({ onClose, onAdd }: {
  onClose: () => void;
  onAdd: (t: Omit<Task, "id">) => void;
}) {
  const [title, setTitle] = useState("");
  const [time, setTime] = useState("30");

  return (
    <motion.div
      className="fixed inset-0 z-50 flex items-end justify-center"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.2 }}
    >
      <div className="absolute inset-0 bg-black/50" onClick={onClose} />
      <motion.div
        className="relative w-full max-w-lg bg-card border-t border-border rounded-t-3xl p-6 pb-[env(safe-area-inset-bottom,24px)]"
        initial={{ y: "100%" }}
        animate={{ y: 0 }}
        exit={{ y: "100%" }}
        transition={{ duration: 0.3, ease: "easeOut" }}
        drag="y"
        dragConstraints={{ top: 0 }}
        dragElastic={0.2}
        onDragEnd={(_, info) => {
          if (info.offset.y > 100) onClose();
        }}
      >
        <div className="w-10 h-1 bg-muted-foreground/30 rounded-full mx-auto mb-5" />
        <div className="flex items-center justify-between mb-5">
          <h3 className="text-foreground">New Task</h3>
          <motion.button whileTap={{ scale: 0.9 }} onClick={onClose} className="w-10 h-10 rounded-xl flex items-center justify-center -mr-2">
            <X className="w-5 h-5 text-muted-foreground" />
          </motion.button>
        </div>

        <div className="space-y-4 mb-6">
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="What needs to be done?"
            className="w-full py-3.5 px-4 rounded-xl bg-surface border border-border text-foreground placeholder:text-muted-foreground/40 focus:border-primary/50 focus:outline-none"
            autoFocus
          />
          <div className="flex items-center gap-3">
            <Clock className="w-4 h-4 text-muted-foreground" />
            <input
              type="number"
              value={time}
              onChange={(e) => setTime(e.target.value)}
              className="w-20 py-2 px-3 rounded-lg bg-surface border border-border text-foreground text-center focus:border-primary/50 focus:outline-none"
            />
            <span className="text-muted-foreground text-sm">minutes</span>
          </div>
        </div>

        <motion.button
          className={`w-full py-4 rounded-2xl flex items-center justify-center ${
            title.trim() ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground"
          }`}
          whileTap={title.trim() ? { scale: 0.96 } : {}}
          disabled={!title.trim()}
          onClick={() => {
            if (title.trim()) {
              onAdd({
                title: title.trim(),
                timeEstimate: Number(time) || 30,
                progress: 0,
                date: new Date().toISOString().split("T")[0],
                completed: false,
                deferred: false,
              });
              onClose();
            }
          }}
        >
          Add Task
        </motion.button>
      </motion.div>
    </motion.div>
  );
}

export function PlanScreen() {
  const { tasks, addTask, updateTask, deleteTask } = useApp();
  const [view, setView] = useState<"list" | "gantt">("list");
  const [showAdd, setShowAdd] = useState(false);
  const today = new Date().toISOString().split("T")[0];
  const todayTasks = tasks.filter(t => t.date === today);

  return (
    <div className="pb-28 md:pb-8 px-4 md:px-6 pt-[env(safe-area-inset-top,12px)]">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.3 }}
        className="flex items-center justify-between py-4"
      >
        <h1 className="text-foreground">Plan</h1>
        <div className="flex items-center gap-2">
          <div className="flex bg-surface rounded-xl p-1">
            <motion.button
              className={`px-4 py-2 rounded-lg text-sm transition-colors ${
                view === "list" ? "bg-primary/15 text-primary" : "text-muted-foreground active:text-foreground"
              }`}
              whileTap={{ scale: 0.95 }}
              onClick={() => setView("list")}
            >
              <List className="w-5 h-5" />
            </motion.button>
            <motion.button
              className={`px-4 py-2 rounded-lg text-sm transition-colors ${
                view === "gantt" ? "bg-primary/15 text-primary" : "text-muted-foreground active:text-foreground"
              }`}
              whileTap={{ scale: 0.95 }}
              onClick={() => setView("gantt")}
            >
              <GanttChart className="w-5 h-5" />
            </motion.button>
          </div>
        </div>
      </motion.div>

      {/* Summary */}
      <motion.div
        initial={{ y: 10, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ delay: 0.05, duration: 0.3 }}
        className="flex items-center gap-4 mb-5 text-sm"
      >
        <span className="text-muted-foreground">
          {todayTasks.filter(t => t.completed).length}/{todayTasks.length} tasks
        </span>
        <div className="flex-1 h-1.5 bg-surface rounded-full overflow-hidden">
          <motion.div
            className="h-full bg-primary rounded-full"
            initial={{ width: 0 }}
            animate={{
              width: todayTasks.length > 0
                ? `${(todayTasks.filter(t => t.completed).length / todayTasks.length) * 100}%`
                : "0%"
            }}
            transition={{ duration: 0.6, ease: "easeOut" }}
          />
        </div>
      </motion.div>

      {/* Content */}
      <AnimatePresence mode="wait">
        {view === "list" ? (
          <motion.div
            key="list"
            initial={{ x: -30, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            exit={{ x: -30, opacity: 0 }}
            transition={{ duration: 0.25, ease: "easeOut" }}
            className="grid grid-cols-1 md:grid-cols-2 gap-x-4"
          >
            {todayTasks.length === 0 ? (
              <div className="md:col-span-2 flex flex-col items-center justify-center py-16 text-center">
                <div className="w-16 h-16 rounded-2xl bg-surface flex items-center justify-center mb-4">
                  <List className="w-8 h-8 text-muted-foreground/50" />
                </div>
                <p className="text-muted-foreground mb-4">No tasks for today yet</p>
                <motion.button
                  className="bg-primary text-primary-foreground px-6 py-3 rounded-xl"
                  whileTap={{ scale: 0.96 }}
                  onClick={() => setShowAdd(true)}
                >
                  Add your first task
                </motion.button>
              </div>
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
          </motion.div>
        ) : (
          <motion.div
            key="gantt"
            initial={{ x: 30, opacity: 0 }}
            animate={{ x: 0, opacity: 1 }}
            exit={{ x: 30, opacity: 0 }}
            transition={{ duration: 0.25, ease: "easeOut" }}
            className="bg-card rounded-2xl border border-border p-4"
          >
            <h3 className="text-foreground mb-4">Day Load</h3>
            {todayTasks.length > 0 ? (
              <GanttView tasks={todayTasks} />
            ) : (
              <p className="text-muted-foreground text-center py-8">No tasks to visualize</p>
            )}
          </motion.div>
        )}
      </AnimatePresence>

      {/* FAB */}
      <motion.button
        className="fixed bottom-24 md:bottom-8 right-5 md:right-8 w-14 h-14 rounded-full bg-primary shadow-lg shadow-primary/20 flex items-center justify-center z-40"
        whileTap={{ scale: 0.9 }}
        onClick={() => setShowAdd(true)}
      >
        <Plus className="w-6 h-6 text-primary-foreground" />
      </motion.button>

      {/* Add Sheet */}
      <AnimatePresence>
        {showAdd && (
          <AddTaskSheet onClose={() => setShowAdd(false)} onAdd={addTask} />
        )}
      </AnimatePresence>
    </div>
  );
}