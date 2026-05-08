Let’s redefine the product cleanly.

# PRODUCT REDEFINITION

## Working Identity

A **structured self-improvement journaling app**.

Not just journaling.

Core pillars:

* self-reflection
* behavior tracking
* focus diagnostics
* physical progress awareness
* mindfulness tracking
* weekly/monthly reviews
* analytics
* private local data ownership

No AI.
No cloud.
Local-first Android app.

---

# CORE FUNCTIONALITIES

# 1. Daily Journal Engine (Main Feature)

This is the heart.

Each day = one journal entry.

---

## 1.1 Daily Snapshot

Quick objective inputs.

Fields:

* Date (auto)
* Sleep hours
* Sleep quality
* Workout completed (yes/no)
* Workout type
* Workout duration
* Deep work hours
* Screen time (manual or auto if possible)
* Social battery

Enums:

Sleep quality:

* Poor
* Okay
* Good
* Great

Social battery:

* Charged
* Neutral
* Drained

---

## 1.2 Time-Based State Tracking

Instead of fake mood score.

Track:

Morning
Afternoon
Evening

Each contains:

Energy:

* Drained
* Low
* Stable
* Good
* High

Mood:

* Calm
* Focused
* Motivated
* Meh
* Irritated
* Anxious
* Restless
* Happy

Stress:

* Light
* Moderate
* Heavy
* Overloaded

Purpose:
reduce recency bias.

---

## 1.3 Event Timeline

Quick bullet list.

Examples:

* attended class
* edited client work
* workout
* traded
* argument with friend

Feature:
dynamic add/remove event rows

---

## 1.4 Behavioral Diagnostics

Checkbox-based diagnostics.

### Productivity

* Procrastinated
* Avoided important work
* Distracted often
* Wasted time mindlessly

### Mental

* Overthought repeatedly
* Emotionally stuck
* Mental fog
* Irritable

### Physical

* Physically tired
* Energy crash
* Ate badly
* Poor movement

---

## 1.5 Emotional Truth Capture

3 prompts:

* Best part of day
* Worst part of day
* Most mentally occupied by

---

## 1.6 Brain Dump

Large free text box.

Purpose:
messy raw thoughts.

No structure.

---

## 1.7 Reflection

Prompts:

* What went well?
* What went badly?
* Why did that happen?
* What pattern did I notice?
* What did I avoid today?

---

## 1.8 Action Fixes

Structured entries.

Format:

Problem → Fix

Example:
Problem:
Instagram distracted me

Fix:
Keep phone in another room

Dynamic add/remove.

---

## 1.9 Tomorrow Top 3

Exactly 3 tasks.

---

## 1.10 Daily Lesson

One-line lesson.

---

# 2. Life Domain Tracking

Separate structured tracking.

---

## 2.1 Physical Domain

Track:

Workout:

* Completed
* Type
* Duration
* Performance note
* Skip reason

Recovery:

* Sleep quality
* Body soreness
* Energy crashes

Nutrition quick flags:

* Ate clean
* Junk food
* Protein okay
* Hydration okay
* Overate

Prompt:
What did I do to improve my body today?

---

## 2.2 Focus Domain

Track:

Focus improvement actions:

* Meditation
* Deep work
* Pomodoro
* Planned tasks
* No-phone session

Focus killers:

* Instagram
* YouTube
* Notifications
* Fatigue
* Unclear tasks
* Noise
* Hunger
* Emotional distraction

Prompt:
What stole my attention today?

---

## 2.3 Mindfulness Domain

Track:

Actions:

* Meditation
* Breathwork
* Silent walk
* Gratitude
* Reflection
* Journaling

Emotional triggers:

* Comparison
* Criticism
* Uncertainty
* Rejection
* Conflict

Prompt:
Did I react intentionally or impulsively?

---

## 2.4 Learning Domain

Prompts:

* What did I learn today?
* What skill did I improve?

---

## 2.5 Work Domain

Track:

* Deep work hours
* Main output
* Biggest blocker
* Goal progress

---

## 2.6 Social Domain (optional)

Track:

* Meaningful interaction
* Social energy impact
* Unresolved tension

---

## 2.7 Identity Alignment

Prompt:

Did my actions match the person I want to become?

Options:

* Yes
* Partly
* No

---

# 3. Review System

---

## 3.1 Minimum Mode

Fast fallback mode.

Fields:

* What happened
* What went badly
* What did I avoid
* Tomorrow top 3

For bad days.

---

## 3.2 Weekly Review

Prompts:

* Biggest wins
* Biggest mistakes
* Recurring patterns
* Emotional trends
* Focus leaks
* Wasted time sources
* Lessons
* Fixes for next week

---

## 3.3 Monthly Review

Prompts:

* Identity drift?
* Progress toward goals?
* Strongest habit?
* Weakest habit?
* Recurring blockers?

---

# 4. Analytics Dashboard

No AI.

Pure local insights.

Metrics:

Daily:

* streak
* journal completion %

Trend charts:

* deep work trend
* sleep trend
* screen time trend
* workout consistency
* identity alignment frequency
* focus killer frequency
* emotional trigger frequency

Correlations:

* low sleep vs poor focus
* high screen time vs distraction
* workout vs mood stability

---

# 5. Data Management

Must-have.

Features:

* export JSON
* import JSON
* backup local data
* restore backup
* clear data

Optional:
CSV export

---

# 6. Android Native Integrations

Possible useful integrations:

Usage Stats API:

* screen time
* app usage
* pickups approximation

Health Connect:

* steps
* workouts
* sleep

Notifications:
optional later

---

# SCREEN ARCHITECTURE

Don’t overbuild.

# MVP Screen Count: 8 to 10

---

## 1. Splash Screen

Purpose:
branding + init checks

Checks:

* first launch?
* onboarding needed?
* load local DB

---

## 2. Onboarding

If first launch.

Collect:

* name
* goals
* enabled domains
* notification preference

Flow:
3 to 5 pages max

---

## 3. Home Dashboard

Main hub.

Shows:

* today summary
* streak
* quick stats
* continue journal
* start minimum mode
* quick add thought
* weekly review reminder

CTA-heavy screen.

---

## 4. Daily Journal Screen

Biggest screen.

Use sections/collapsible cards.

Sections:

* daily snapshot
* state tracking
* events
* diagnostics
* emotional truth
* brain dump
* reflection
* action fixes
* tomorrow top 3
* lesson

This may need step-based UX.

---

## 5. Life Domains Screen

Dedicated structured domain logging.

Tabs:

* Physical
* Focus
* Mindfulness
* Learning
* Work
* Social
* Identity

Cleaner than stuffing everything into journal.

---

## 6. Analytics Screen

Charts.

Tabs:

* productivity
* focus
* wellness
* patterns

---

## 7. Reviews Screen

Weekly/monthly review creation.

Shows:

* pending review
* past reviews

---

## 8. Journal History Screen

Calendar/list view.

View:

* entries by date
* search
* edit old entries

---

## 9. Settings / Data Screen

Manage:

* export/import
* notifications
* integrations
* enabled domains
* theme
* privacy

---

## Optional 10. Quick Thought Capture

Super lightweight note capture.

Open fast.
Minimal friction.

---

# USER FLOW

# First Time User

Splash
↓
Onboarding
↓
Permissions (optional integrations)
↓
Home dashboard
↓
Create first journal

---

# Daily Use Flow

Open app
↓
Dashboard
↓
Continue today journal OR quick thought
↓
Complete journal
↓
Save
↓
Dashboard updates stats

---

# Weekly Flow

Dashboard reminder
↓
Weekly review
↓
Save
↓
Analytics update

---

# Monthly Flow

Dashboard reminder
↓
Monthly review
↓
Reflection history

---

# Power User Flow

Dashboard
↓
Analytics
↓
See focus problems
↓
Daily journal
↓
Log fixes
↓
Track improvement

---

# RECOMMENDED ANDROID ARCHITECTURE

Use:
**MVVM + Clean-ish architecture**

Structure:

```text
ui/
  home/
  journal/
  analytics/
  reviews/
  settings/

data/
  local/
  repository/
  models/

domain/
  usecases/

utils/
```

Tech:

* Kotlin
* Jetpack Compose
* Room
* DataStore
* MPAndroidChart / Compose charts
* Navigation Compose
* Hilt
* WorkManager

---

# REAL MVP CUT (important)

Don’t build all first.

Phase 1:
✅ onboarding
✅ dashboard
✅ daily journal
✅ local storage
✅ history

Phase 2:
✅ domains
✅ reviews
✅ analytics

Phase 3:
✅ usage stats integration
✅ export/import
✅ backup

Otherwise you’ll drown.

This is now a **real Android product scope.**
