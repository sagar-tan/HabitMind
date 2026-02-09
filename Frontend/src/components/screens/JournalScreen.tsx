import { useState } from "react";
import { motion, AnimatePresence } from "motion/react";
import {
  Plus, PenLine, Mic, Image, X, Tag, ArrowLeft,
  Clock, ChevronRight
} from "lucide-react";
import { useApp, JournalEntry } from "../AppContext";

function EntryCard({ entry }: { entry: JournalEntry }) {
  const time = new Date(entry.timestamp).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });
  const date = new Date(entry.timestamp);
  const isToday = entry.date === new Date().toISOString().split("T")[0];

  const typeIcon = {
    text: PenLine,
    voice: Mic,
    image: Image,
  }[entry.type];
  const TypeIcon = typeIcon;

  return (
    <motion.div
      initial={{ y: 15, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      exit={{ height: 0, opacity: 0, marginBottom: 0 }}
      transition={{ duration: 0.25, ease: "easeOut" }}
      className="bg-card border border-border rounded-2xl p-4"
    >
      <div className="flex items-start gap-3">
        <div className="w-9 h-9 rounded-xl bg-surface flex items-center justify-center shrink-0 mt-0.5">
          <TypeIcon className="w-4 h-4 text-primary" />
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1.5">
            <span className="text-xs text-muted-foreground flex items-center gap-1">
              <Clock className="w-3 h-3" />
              {isToday ? time : date.toLocaleDateString([], { month: "short", day: "numeric" })}
            </span>
          </div>
          <p className="text-foreground/90 text-sm mb-2 line-clamp-3">{entry.content}</p>
          {entry.tags.length > 0 && (
            <div className="flex flex-wrap gap-1.5">
              {entry.tags.map((tag) => (
                <span
                  key={tag}
                  className="text-[10px] text-primary bg-primary/10 px-2 py-0.5 rounded-md"
                >
                  {tag}
                </span>
              ))}
            </div>
          )}
        </div>
      </div>
    </motion.div>
  );
}

function JournalEntryCreation({ onClose, onSave }: {
  onClose: () => void;
  onSave: (entry: Omit<JournalEntry, "id">) => void;
}) {
  const [content, setContent] = useState("");
  const [tags, setTags] = useState<string[]>([]);
  const [tagInput, setTagInput] = useState("");
  const [entryType, setEntryType] = useState<"text" | "voice" | "image">("text");

  const addTag = () => {
    if (tagInput.trim() && !tags.includes(tagInput.trim())) {
      setTags([...tags, tagInput.trim()]);
      setTagInput("");
    }
  };

  return (
    <motion.div
      className="fixed inset-0 z-50 bg-background"
      initial={{ y: "100%" }}
      animate={{ y: 0 }}
      exit={{ y: "100%" }}
      transition={{ duration: 0.3, ease: "easeOut" }}
    >
      <div className="h-full flex flex-col max-w-2xl mx-auto">
        {/* Header */}
        <div className="flex items-center justify-between px-4 md:px-6 py-4 pt-[env(safe-area-inset-top,16px)]">
          <motion.button
            whileTap={{ scale: 0.9 }}
            onClick={onClose}
            className="w-10 h-10 rounded-xl bg-surface flex items-center justify-center"
          >
            <ArrowLeft className="w-5 h-5 text-foreground" />
          </motion.button>
          <h3 className="text-foreground">New Entry</h3>
          <motion.button
            whileTap={{ scale: 0.95 }}
            className={`px-4 py-2 rounded-xl text-sm ${
              content.trim()
                ? "bg-primary text-primary-foreground"
                : "bg-muted text-muted-foreground"
            }`}
            disabled={!content.trim()}
            onClick={() => {
              if (content.trim()) {
                onSave({
                  type: entryType,
                  content: content.trim(),
                  tags,
                  timestamp: new Date().toISOString(),
                  date: new Date().toISOString().split("T")[0],
                });
                onClose();
              }
            }}
          >
            Save
          </motion.button>
        </div>

        {/* Type selector */}
        <div className="flex gap-2 px-4 md:px-6 mb-4">
          {[
            { type: "text" as const, icon: PenLine, label: "Text" },
            { type: "voice" as const, icon: Mic, label: "Voice" },
            { type: "image" as const, icon: Image, label: "Photo" },
          ].map((item) => (
            <motion.button
              key={item.type}
              whileTap={{ scale: 0.95 }}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm transition-colors min-h-[44px] ${
                entryType === item.type
                  ? "bg-primary/15 text-primary border border-primary/20"
                  : "bg-surface text-muted-foreground active:bg-surface-elevated"
              }`}
              onClick={() => setEntryType(item.type)}
            >
              <item.icon className="w-4 h-4" />
              {item.label}
            </motion.button>
          ))}
        </div>

        {/* Content */}
        <div className="flex-1 px-4 md:px-6">
          {entryType === "text" && (
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="What's on your mind?"
              className="w-full h-48 py-3 px-4 rounded-2xl bg-surface border border-border text-foreground placeholder:text-muted-foreground/40 focus:border-primary/50 focus:outline-none resize-none"
              autoFocus
            />
          )}
          {entryType === "voice" && (
            <div className="flex flex-col items-center justify-center py-12">
              <motion.div
                className="w-20 h-20 rounded-full bg-primary/15 flex items-center justify-center mb-4"
                animate={{ scale: [1, 1.1, 1] }}
                transition={{ duration: 2, repeat: Infinity }}
              >
                <Mic className="w-8 h-8 text-primary" />
              </motion.div>
              <p className="text-muted-foreground text-sm mb-2">Tap to record</p>
              <input
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="Or type a note about the recording..."
                className="w-full py-3 px-4 rounded-xl bg-surface border border-border text-foreground placeholder:text-muted-foreground/40 focus:border-primary/50 focus:outline-none mt-4"
              />
            </div>
          )}
          {entryType === "image" && (
            <div className="flex flex-col items-center justify-center py-8">
              <div className="w-full aspect-video rounded-2xl bg-surface border-2 border-dashed border-border flex items-center justify-center mb-4">
                <div className="text-center">
                  <Image className="w-8 h-8 text-muted-foreground/40 mx-auto mb-2" />
                  <p className="text-muted-foreground text-sm">Tap to add photo</p>
                </div>
              </div>
              <input
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="Add a caption..."
                className="w-full py-3 px-4 rounded-xl bg-surface border border-border text-foreground placeholder:text-muted-foreground/40 focus:border-primary/50 focus:outline-none"
              />
            </div>
          )}
        </div>

        {/* Tags */}
        <div className="px-4 md:px-6 pb-[env(safe-area-inset-bottom,16px)]">
          <div className="flex items-center gap-2 mb-3">
            <Tag className="w-4 h-4 text-muted-foreground" />
            <input
              value={tagInput}
              onChange={(e) => setTagInput(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && addTag()}
              placeholder="Add tag..."
              className="flex-1 py-2 text-sm bg-transparent text-foreground placeholder:text-muted-foreground/40 focus:outline-none"
            />
          </div>
          {tags.length > 0 && (
            <div className="flex flex-wrap gap-1.5 mb-3">
              {tags.map((tag) => (
                <motion.button
                  key={tag}
                  initial={{ scale: 0.8, opacity: 0 }}
                  animate={{ scale: 1, opacity: 1 }}
                  className="flex items-center gap-1.5 text-xs text-primary bg-primary/10 px-3 py-2 rounded-lg min-h-[36px]"
                  onClick={() => setTags(tags.filter(t => t !== tag))}
                >
                  {tag}
                  <X className="w-3.5 h-3.5" />
                </motion.button>
              ))}
            </div>
          )}
        </div>
      </div>
    </motion.div>
  );
}

export function JournalScreen() {
  const { journal, addJournalEntry, activeScreen, setActiveScreen } = useApp();
  const [showCreate, setShowCreate] = useState(false);

  return (
    <div className="pb-28 md:pb-8 px-4 md:px-6 pt-[env(safe-area-inset-top,12px)]">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="flex items-center justify-between py-4"
      >
        <h1 className="text-foreground">Journal</h1>
        <span className="text-sm text-muted-foreground">{journal.length} entries</span>
      </motion.div>

      {/* Timeline */}
      {journal.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 text-center">
          <div className="w-16 h-16 rounded-2xl bg-surface flex items-center justify-center mb-4">
            <PenLine className="w-8 h-8 text-muted-foreground/50" />
          </div>
          <p className="text-muted-foreground mb-1">Your journal is empty</p>
          <p className="text-muted-foreground/60 text-sm mb-4">Start capturing your thoughts</p>
          <motion.button
            className="bg-primary text-primary-foreground px-6 py-3 rounded-xl"
            whileTap={{ scale: 0.96 }}
            onClick={() => setShowCreate(true)}
          >
            Write first entry
          </motion.button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {journal.map((entry, i) => (
            <motion.div
              key={entry.id}
              initial={{ y: 15, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              transition={{ delay: i * 0.04, duration: 0.25, ease: "easeOut" }}
            >
              <EntryCard entry={entry} />
            </motion.div>
          ))}
        </div>
      )}

      {/* FAB */}
      <motion.button
        className="fixed bottom-24 md:bottom-8 right-5 md:right-8 w-14 h-14 rounded-full bg-primary shadow-lg shadow-primary/20 flex items-center justify-center z-40"
        whileTap={{ scale: 0.9 }}
        onClick={() => setShowCreate(true)}
      >
        <Plus className="w-6 h-6 text-primary-foreground" />
      </motion.button>

      {/* Create Entry */}
      <AnimatePresence>
        {showCreate && (
          <JournalEntryCreation
            onClose={() => setShowCreate(false)}
            onSave={addJournalEntry}
          />
        )}
      </AnimatePresence>
    </div>
  );
}