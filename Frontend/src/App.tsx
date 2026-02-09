import { motion, AnimatePresence } from "motion/react";
import { Home, CalendarCheck, Repeat, BookOpen, BarChart3 } from "lucide-react";
import { AppProvider, useApp, TabName } from "./components/AppContext";
import { BottomNav } from "./components/BottomNav";
import { OnboardingFlow } from "./components/onboarding/OnboardingFlow";
import { HomeScreen } from "./components/screens/HomeScreen";
import { PlanScreen } from "./components/screens/PlanScreen";
import { HabitsScreen } from "./components/screens/HabitsScreen";
import { JournalScreen } from "./components/screens/JournalScreen";
import { InsightsScreen } from "./components/screens/InsightsScreen";
import { SettingsScreen } from "./components/screens/SettingsScreen";
import { WeeklyReviewScreen } from "./components/screens/WeeklyReviewScreen";

const sidebarTabs: { name: TabName; icon: React.ElementType; label: string }[] = [
  { name: "home", icon: Home, label: "Home" },
  { name: "plan", icon: CalendarCheck, label: "Plan" },
  { name: "habits", icon: Repeat, label: "Habits" },
  { name: "journal", icon: BookOpen, label: "Journal" },
  { name: "insights", icon: BarChart3, label: "Insights" },
];

function SidebarNav() {
  const { activeTab, setActiveTab, setActiveScreen } = useApp();

  return (
    <aside className="hidden md:flex flex-col w-20 lg:w-56 bg-sidebar border-r border-sidebar-border shrink-0 py-6">
      {/* Logo */}
      <div className="flex items-center justify-center lg:justify-start lg:px-6 mb-8">
        <div className="w-9 h-9 rounded-xl bg-primary/10 flex items-center justify-center">
          <div className="w-4 h-4 rounded-md bg-primary" />
        </div>
        <span className="hidden lg:block ml-3 text-foreground text-sm">Vitality</span>
      </div>

      {/* Nav items */}
      <nav className="flex-1 flex flex-col gap-1 px-3 lg:px-4">
        {sidebarTabs.map((tab) => {
          const isActive = activeTab === tab.name;
          const Icon = tab.icon;
          return (
            <motion.button
              key={tab.name}
              whileTap={{ scale: 0.96 }}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 ${
                isActive
                  ? "bg-primary/10 text-primary"
                  : "text-muted-foreground hover:text-foreground hover:bg-surface active:text-foreground active:bg-surface"
              }`}
              onClick={() => {
                setActiveTab(tab.name);
                setActiveScreen("main");
              }}
            >
              <Icon className="w-5 h-5 shrink-0 mx-auto lg:mx-0" />
              <span className="hidden lg:block text-sm">{tab.label}</span>
            </motion.button>
          );
        })}
      </nav>
    </aside>
  );
}

function AppContent() {
  const { onboarded, activeTab, activeScreen } = useApp();

  if (!onboarded) {
    return <OnboardingFlow />;
  }

  // Full-screen overlays
  if (activeScreen === "settings") {
    return (
      <div className="h-full flex">
        <SidebarNav />
        <div className="flex-1 h-full overflow-y-auto bg-background">
          <div className="max-w-2xl mx-auto">
            <SettingsScreen />
          </div>
        </div>
      </div>
    );
  }

  if (activeScreen === "weekly-review") {
    return (
      <div className="h-full flex">
        <SidebarNav />
        <div className="flex-1 h-full overflow-y-auto bg-background">
          <div className="max-w-2xl mx-auto">
            <WeeklyReviewScreen />
          </div>
        </div>
      </div>
    );
  }

  const screenMap = {
    home: <HomeScreen />,
    plan: <PlanScreen />,
    habits: <HabitsScreen />,
    journal: <JournalScreen />,
    insights: <InsightsScreen />,
  };

  return (
    <div className="h-full flex">
      <SidebarNav />
      <div className="flex-1 h-full bg-background relative overflow-hidden">
        <div className="h-full overflow-y-auto overflow-x-hidden">
          <AnimatePresence mode="wait">
            <motion.div
              key={activeTab + activeScreen}
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.25, ease: "easeOut" }}
              className="min-h-full max-w-2xl mx-auto"
            >
              {screenMap[activeTab]}
            </motion.div>
          </AnimatePresence>
        </div>
        {/* Bottom nav - mobile only */}
        <div className="md:hidden">
          <BottomNav />
        </div>
      </div>
    </div>
  );
}

export default function App() {
  return (
    <AppProvider>
      <div className="h-screen w-full bg-background transition-colors duration-300">
        <AppContent />
      </div>
    </AppProvider>
  );
}