import { motion } from "motion/react";
import { Home, CalendarCheck, Repeat, BookOpen, BarChart3 } from "lucide-react";
import { useApp, TabName } from "./AppContext";

const tabs: { name: TabName; icon: React.ElementType; label: string }[] = [
  { name: "home", icon: Home, label: "Home" },
  { name: "plan", icon: CalendarCheck, label: "Plan" },
  { name: "habits", icon: Repeat, label: "Habits" },
  { name: "journal", icon: BookOpen, label: "Journal" },
  { name: "insights", icon: BarChart3, label: "Insights" },
];

export function BottomNav() {
  const { activeTab, setActiveTab, setActiveScreen } = useApp();

  return (
    <div className="fixed bottom-0 left-0 right-0 z-50">
      <nav className="bg-nav-bg backdrop-blur-xl border-t border-nav-border px-2 pb-[env(safe-area-inset-bottom,8px)] pt-2">
        <div className="flex items-center justify-around">
          {tabs.map((tab) => {
            const isActive = activeTab === tab.name;
            const Icon = tab.icon;
            return (
              <motion.button
                key={tab.name}
                className="flex flex-col items-center gap-1 py-2 px-3 min-w-[56px] relative"
                whileTap={{ scale: 0.94 }}
                transition={{ duration: 0.12, ease: "easeOut" }}
                onClick={() => {
                  setActiveTab(tab.name);
                  setActiveScreen("main");
                }}
              >
                {isActive && (
                  <motion.div
                    layoutId="nav-indicator"
                    className="absolute -top-2 w-8 h-0.5 rounded-full bg-primary"
                    transition={{ type: "tween", duration: 0.25, ease: "easeOut" }}
                  />
                )}
                <Icon
                  className={`w-5 h-5 transition-colors duration-200 ${
                    isActive ? "text-primary" : "text-muted-foreground"
                  }`}
                />
                <span
                  className={`text-[10px] transition-colors duration-200 ${
                    isActive ? "text-primary" : "text-muted-foreground"
                  }`}
                >
                  {tab.label}
                </span>
              </motion.button>
            );
          })}
        </div>
      </nav>
    </div>
  );
}
