import { useState } from "react";
import { motion } from "motion/react";
import {
  ArrowLeft, Bell, Download, Upload, Palette, RotateCcw,
  ChevronRight, Shield, Info, Sun, Moon
} from "lucide-react";
import { useApp } from "../AppContext";

export function SettingsScreen() {
  const { setActiveScreen, setOnboarded, theme, toggleTheme } = useApp();
  const [notifications, setNotifications] = useState(true);

  const settingGroups = [
    {
      title: "Preferences",
      items: [
        {
          icon: Bell,
          label: "Notifications",
          description: "Reminders and weekly reviews",
          toggle: true,
          value: notifications,
          onChange: () => setNotifications(!notifications),
        },
        {
          icon: theme === "dark" ? Moon : Sun,
          label: "Theme",
          description: theme === "dark" ? "Dark mode" : "Light mode",
          toggle: true,
          value: theme === "dark",
          onChange: toggleTheme,
        },
      ],
    },
    {
      title: "Data",
      items: [
        {
          icon: Download,
          label: "Export Data",
          description: "Download all your data as JSON",
          action: true,
          onClick: () => {
            const data = localStorage.getItem("vitality-app-state");
            if (data) {
              const blob = new Blob([data], { type: "application/json" });
              const url = URL.createObjectURL(blob);
              const a = document.createElement("a");
              a.href = url;
              a.download = "vitality-backup.json";
              a.click();
              URL.revokeObjectURL(url);
            }
          },
        },
        {
          icon: Upload,
          label: "Import Data",
          description: "Restore from a backup file",
          action: true,
        },
      ],
    },
    {
      title: "About",
      items: [
        {
          icon: Shield,
          label: "Privacy",
          description: "All data stored locally on device",
          action: true,
        },
        {
          icon: Info,
          label: "About Vitality",
          description: "Version 1.0.0",
          action: true,
        },
      ],
    },
    {
      title: "Danger Zone",
      items: [
        {
          icon: RotateCcw,
          label: "Reset App",
          description: "Delete all data and start fresh",
          destructive: true,
          onClick: () => {
            if (confirm("This will delete all your data. Are you sure?")) {
              localStorage.removeItem("vitality-app-state");
              setOnboarded(false);
              setActiveScreen("main");
              window.location.reload();
            }
          },
        },
      ],
    },
  ];

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
        <h2 className="text-foreground">Settings</h2>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {settingGroups.map((group, gi) => (
          <motion.div
            key={group.title}
            initial={{ y: 15, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.05 * gi, duration: 0.3 }}
          >
            <p className="text-muted-foreground text-xs uppercase tracking-wider mb-2 px-1">{group.title}</p>
            <div className="bg-card border border-border rounded-2xl overflow-hidden">
              {group.items.map((item, i) => (
                <motion.button
                  key={item.label}
                  whileTap={{ scale: 0.99 }}
                  className={`w-full flex items-center gap-3 p-4 text-left ${
                    i < group.items.length - 1 ? "border-b border-border" : ""
                  }`}
                  onClick={item.onClick}
                >
                  <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${
                    (item as any).destructive ? "bg-destructive/10" : "bg-surface"
                  }`}>
                    <item.icon className={`w-4.5 h-4.5 ${
                      (item as any).destructive ? "text-destructive-foreground" : "text-muted-foreground"
                    }`} />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className={`text-sm ${
                      (item as any).destructive ? "text-destructive-foreground" : "text-foreground"
                    }`}>{item.label}</p>
                    {item.description && (
                      <p className="text-xs text-muted-foreground">{item.description}</p>
                    )}
                  </div>
                  {(item as any).toggle ? (
                    <div
                      className={`w-11 h-6 rounded-full relative transition-colors duration-200 ${
                        (item as any).value ? "bg-primary" : "bg-switch-background"
                      }`}
                      onClick={(e) => {
                        e.stopPropagation();
                        (item as any).onChange?.();
                      }}
                    >
                      <motion.div
                        className="w-5 h-5 rounded-full bg-white absolute top-0.5 shadow-sm"
                        animate={{ left: (item as any).value ? 22 : 2 }}
                        transition={{ duration: 0.2, ease: "easeOut" }}
                      />
                    </div>
                  ) : (
                    <ChevronRight className="w-4 h-4 text-muted-foreground/50" />
                  )}
                </motion.button>
              ))}
            </div>
          </motion.div>
        ))}
      </div>
    </div>
  );
}
