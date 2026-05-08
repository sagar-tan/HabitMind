#ProjectMap

# Journal-First Habit System

This document describes the core interaction model for the upgraded HabitMind experience, focusing on structured reflection and interactive habit confirmation.

## 🖋️ Daily Reflection Ritual
The app transitions from a simple "habit tracker" to a "ritual engine." The day begins and ends with the **Daily Journal**.
- **Sectioned Reflection**: Instead of a blank page, the journal is divided into cognitive slices (Behavioral Diagnostics, Emotional Truths, etc.).
- **Automated Triggers**: Certain habit completions can trigger specific journal prompts.

## 🔘 Premium Habit Confirmation
Habits are no longer simple checkboxes. They are confirmed via the `PremiumHabitToggle`.

### Habit Types
1. **Positive (Achievement)**:
   - **Goal**: Complete the action.
   - **Confirmation**: Slider to "YES" (Success).
2. **Negative (Avoidance)**:
   - **Goal**: Avoid the action (e.g., "No Doomscrolling").
   - **Confirmation**: 
     - Slider to "NO" (Success/Avoided).
     - Slider to "YES" (Failure/Caved) -> Triggers an immediate "Symptom Check" journal prompt.

### Interactive Elements
- **Haptic Feedback**: Variable vibration patterns for success vs. failure.
- **Glassmorphic Slider**: Real-time color shifts (Green/Red) based on intent.

## 🔗 Related Documents
- [[Architecture]]
- [[Data_Schema]]
