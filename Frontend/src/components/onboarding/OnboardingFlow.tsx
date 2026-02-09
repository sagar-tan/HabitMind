import React, { useState } from "react";
import { motion, AnimatePresence } from "motion/react";
import { Shield, ArrowRight, ChevronRight, BarChart3, CalendarCheck, Repeat } from "lucide-react";
import { useApp } from "../AppContext";

const slideVariants = {
  enter: { x: 80, opacity: 0 },
  center: { x: 0, opacity: 1 },
  exit: { x: -80, opacity: 0 },
};

export function OnboardingFlow() {
  const { setOnboarded, setProfile } = useApp();
  const [step, setStep] = useState(0);
  const [privacyChecked, setPrivacyChecked] = useState(false);
  const [age, setAge] = useState("");
  const [height, setHeight] = useState("");
  const [weight, setWeight] = useState("");
  const [walkSlide, setWalkSlide] = useState(0);

  const next = () => setStep((s) => s + 1);

  const finish = () => {
    setProfile({ age, height, weight });
    setOnboarded(true);
  };

  const walkSlides = [
    { icon: Repeat, title: "Track habits and plans", desc: "Build streaks and stay consistent with daily tracking." },
    { icon: CalendarCheck, title: "Weekly reviews", desc: "Reflect on your week and plan the next one." },
    { icon: BarChart3, title: "Local analytics", desc: "All insights computed locally on your device." },
  ];

  return (
    <div className="min-h-screen bg-background flex flex-col items-center justify-center px-6">
      <AnimatePresence mode="wait">
        {/* Step 0: Welcome */}
        {step === 0 && (
          <motion.div
            key="welcome"
            variants={slideVariants}
            initial="enter"
            animate="center"
            exit="exit"
            transition={{ duration: 0.3, ease: "easeOut" }}
            className="flex flex-col items-center text-center w-full max-w-sm md:max-w-md"
          >
            <motion.div
              className="w-20 h-20 rounded-2xl bg-primary/10 flex items-center justify-center mb-8"
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ delay: 0.1, duration: 0.4, ease: "easeOut" }}
            >
              <div className="w-10 h-10 rounded-xl bg-primary/20 flex items-center justify-center">
                <div className="w-4 h-4 rounded-md bg-primary" />
              </div>
            </motion.div>
            <h1 className="text-foreground mb-3">Vitality</h1>
            <p className="text-muted-foreground mb-12 max-w-[280px]">
              Your personal space for habits, plans, and self-reflection. Fully private.
            </p>
            <motion.button
              className="w-full py-4 rounded-2xl bg-primary text-primary-foreground flex items-center justify-center gap-2"
              whileTap={{ scale: 0.96 }}
              transition={{ duration: 0.12 }}
              onClick={next}
            >
              Start Locally
              <ArrowRight className="w-5 h-5" />
            </motion.button>
          </motion.div>
        )}

        {/* Step 1: Privacy */}
        {step === 1 && (
          <motion.div
            key="privacy"
            variants={slideVariants}
            initial="enter"
            animate="center"
            exit="exit"
            transition={{ duration: 0.3, ease: "easeOut" }}
            className="flex flex-col items-center text-center w-full max-w-sm md:max-w-md"
          >
            <div className="w-16 h-16 rounded-2xl bg-primary/10 flex items-center justify-center mb-6">
              <Shield className="w-8 h-8 text-primary" />
            </div>
            <h2 className="text-foreground mb-3">Your data stays here</h2>
            <p className="text-muted-foreground mb-8 max-w-[300px]">
              Everything is stored locally on your device. No accounts, no servers, no tracking. Your data never leaves your phone.
            </p>
            <motion.button
              className="flex items-center gap-3 mb-8 py-3 px-4 rounded-xl bg-surface w-full"
              whileTap={{ scale: 0.98 }}
              onClick={() => setPrivacyChecked(!privacyChecked)}
            >
              <div className={`w-6 h-6 rounded-lg border-2 flex items-center justify-center transition-all duration-200 ${
                privacyChecked ? "bg-primary border-primary" : "border-muted-foreground/40"
              }`}>
                {privacyChecked && (
                  <motion.svg
                    initial={{ pathLength: 0 }}
                    animate={{ pathLength: 1 }}
                    transition={{ duration: 0.2 }}
                    className="w-4 h-4 text-primary-foreground"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth={3}
                  >
                    <motion.path
                      d="M5 13l4 4L19 7"
                      initial={{ pathLength: 0 }}
                      animate={{ pathLength: 1 }}
                      transition={{ duration: 0.2 }}
                    />
                  </motion.svg>
                )}
              </div>
              <span className="text-foreground text-left">I understand my data is stored locally</span>
            </motion.button>
            <motion.button
              className={`w-full py-4 rounded-2xl flex items-center justify-center gap-2 transition-all duration-200 ${
                privacyChecked
                  ? "bg-primary text-primary-foreground"
                  : "bg-muted text-muted-foreground cursor-not-allowed"
              }`}
              whileTap={privacyChecked ? { scale: 0.96 } : {}}
              disabled={!privacyChecked}
              onClick={next}
            >
              Continue
              <ChevronRight className="w-5 h-5" />
            </motion.button>
          </motion.div>
        )}

        {/* Step 2: Profile */}
        {step === 2 && (
          <motion.div
            key="profile"
            variants={slideVariants}
            initial="enter"
            animate="center"
            exit="exit"
            transition={{ duration: 0.3, ease: "easeOut" }}
            className="flex flex-col items-center w-full max-w-sm md:max-w-md"
          >
            <h2 className="text-foreground mb-2 text-center">About you</h2>
            <p className="text-muted-foreground mb-8 text-center">Optional. Helps personalize your experience.</p>

            <div className="w-full space-y-4 mb-8">
              <div className="flex flex-col gap-2">
                <label className="text-muted-foreground text-sm">Age</label>
                <input
                  type="number"
                  value={age}
                  onChange={(e) => setAge(e.target.value)}
                  placeholder="e.g. 28"
                  className="w-full py-3.5 px-4 rounded-xl bg-surface border border-border text-foreground placeholder:text-muted-foreground/40 focus:border-primary/50 focus:outline-none transition-colors"
                />
              </div>
              <div className="flex flex-col gap-2">
                <label className="text-muted-foreground text-sm">Height (cm)</label>
                <input
                  type="number"
                  value={height}
                  onChange={(e) => setHeight(e.target.value)}
                  placeholder="e.g. 175"
                  className="w-full py-3.5 px-4 rounded-xl bg-surface border border-border text-foreground placeholder:text-muted-foreground/40 focus:border-primary/50 focus:outline-none transition-colors"
                />
              </div>
              <div className="flex flex-col gap-2">
                <label className="text-muted-foreground text-sm">Weight (kg)</label>
                <input
                  type="number"
                  value={weight}
                  onChange={(e) => setWeight(e.target.value)}
                  placeholder="e.g. 70"
                  className="w-full py-3.5 px-4 rounded-xl bg-surface border border-border text-foreground placeholder:text-muted-foreground/40 focus:border-primary/50 focus:outline-none transition-colors"
                />
              </div>
            </div>

            <div className="w-full flex gap-3">
              <motion.button
                className="flex-1 py-4 rounded-2xl bg-surface text-muted-foreground"
                whileTap={{ scale: 0.96 }}
                onClick={next}
              >
                Skip
              </motion.button>
              <motion.button
                className="flex-1 py-4 rounded-2xl bg-primary text-primary-foreground"
                whileTap={{ scale: 0.96 }}
                onClick={next}
              >
                Continue
              </motion.button>
            </div>
          </motion.div>
        )}

        {/* Step 3: Walkthrough */}
        {step === 3 && (
          <motion.div
            key="walkthrough"
            variants={slideVariants}
            initial="enter"
            animate="center"
            exit="exit"
            transition={{ duration: 0.3, ease: "easeOut" }}
            className="flex flex-col items-center w-full max-w-sm md:max-w-md"
          >
            <AnimatePresence mode="wait">
              <motion.div
                key={walkSlide}
                initial={{ x: 60, opacity: 0 }}
                animate={{ x: 0, opacity: 1 }}
                exit={{ x: -60, opacity: 0 }}
                transition={{ duration: 0.25, ease: "easeOut" }}
                className="flex flex-col items-center text-center"
              >
                <div className="w-20 h-20 rounded-2xl bg-primary/10 flex items-center justify-center mb-8">
                  {React.createElement(walkSlides[walkSlide].icon, { className: "w-10 h-10 text-primary" })}
                </div>
                <h2 className="text-foreground mb-3">{walkSlides[walkSlide].title}</h2>
                <p className="text-muted-foreground mb-12 max-w-[280px]">{walkSlides[walkSlide].desc}</p>
              </motion.div>
            </AnimatePresence>

            {/* Dots */}
            <div className="flex gap-2 mb-8">
              {walkSlides.map((_, i) => (
                <motion.div
                  key={i}
                  className={`h-1.5 rounded-full transition-all duration-300 ${
                    i === walkSlide ? "w-6 bg-primary" : "w-1.5 bg-muted-foreground/30"
                  }`}
                />
              ))}
            </div>

            <div className="w-full flex gap-3">
              <motion.button
                className="flex-1 py-4 rounded-2xl bg-surface text-muted-foreground"
                whileTap={{ scale: 0.96 }}
                onClick={finish}
              >
                Skip
              </motion.button>
              <motion.button
                className="flex-1 py-4 rounded-2xl bg-primary text-primary-foreground"
                whileTap={{ scale: 0.96 }}
                onClick={() => {
                  if (walkSlide < walkSlides.length - 1) {
                    setWalkSlide((s) => s + 1);
                  } else {
                    finish();
                  }
                }}
              >
                {walkSlide < walkSlides.length - 1 ? "Next" : "Get Started"}
              </motion.button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
