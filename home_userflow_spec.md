# HabitMind Home + User Flow Spec

## Direction

Keep the current HabitMind aesthetic:

- dark, near-black background
- glassy cards and soft borders
- monochrome palette with one accent
- calm premium motion, not flashy motion
- dense functionality hidden behind clear sections and strong CTAs

The product changes, but the visual language stays.

---

## Product Shape

HabitMind is now a structured self-improvement journaling app with analytics.

That means the app should feel centered on one daily ritual:

1. open app
2. see where today stands
3. continue today journal
4. log domains if needed
5. review patterns later

The home screen should become the command center for that ritual.

---

## Navigation Model

### Bottom tabs

Use 5 bottom tabs.

1. `Home`
2. `Journal`
3. `Domains`
4. `Analytics`
5. `Reviews`

### Why this tab set

- `Home` is the hub and starting point every day
- `Journal` is the main product surface
- `Domains` keeps structured logging separate from the long journal flow
- `Analytics` gives feedback loops without cluttering home
- `Reviews` owns weekly/monthly reflection instead of hiding it under settings or insights

### Remove from primary tabs

- `Plan` should no longer be a primary tab
- `Habits` should no longer be a standalone primary tab
- `Insights` evolves into `Analytics`

Those ideas still live in the product, but inside journal, domains, and analytics instead of acting like separate mini-apps.

### Secondary screens

These should not be bottom tabs:

- `Settings`
- `Journal History`
- `Daily Journal Detail`
- `Quick Thought`
- `Review Detail`
- `Onboarding`
- future `Integrations`

### Floating action button

Keep the floating glass FAB.

New default behavior:

- open `Quick Thought`

Longer-term optional behavior:

- context-aware by tab
- but for MVP, a universal quick capture is stronger

---

## Screen Map

### 1. Home

Purpose:

- show the state of today
- pull the user back into journaling
- surface the next best action

Primary content blocks:

- greeting + date
- today journal progress
- current streak
- continue journal CTA
- start minimum mode CTA
- quick thought CTA
- today snapshot preview
- pending review reminder
- micro insight preview

Main actions:

- continue today journal
- start minimum mode
- open quick thought
- open this week review
- jump to analytics or domains when relevant

### 2. Journal

Purpose:

- create and edit the main daily journal
- browse past entries

Structure:

- default view: today journal status + recent entries
- segmented control or top switch:
  - `Today`
  - `History`

Main actions:

- open today journal
- create missing entry for selected date
- browse old entries by date
- edit prior entry

### 3. Daily Journal Detail

Purpose:

- complete the structured daily reflection flow

Section order:

1. Daily Snapshot
2. State Tracking
3. Event Timeline
4. Behavioral Diagnostics
5. Emotional Truth
6. Brain Dump
7. Reflection
8. Action Fixes
9. Tomorrow Top 3
10. Daily Lesson

Behavior:

- auto-save after changes
- collapsible cards
- progress indicator at top
- minimum mode toggle available from header

### 4. Domains

Purpose:

- log structured domain-specific check-ins without bloating the journal entry

Top tabs inside screen:

- `Physical`
- `Focus`
- `Mindfulness`
- `Learning`
- `Work`
- `Social`
- `Identity`

Main actions:

- check quick flags
- answer one prompt per domain
- add small notes

### 5. Analytics

Purpose:

- show trends and simple correlations from local data

Top tabs inside screen:

- `Productivity`
- `Focus`
- `Wellness`
- `Patterns`

Main content:

- streak
- journal completion rate
- sleep trend
- deep work trend
- screen time trend
- workout consistency
- identity alignment frequency
- common focus killers
- common emotional triggers

### 6. Reviews

Purpose:

- create weekly and monthly reviews
- revisit older reviews

Sections:

- pending review card
- weekly review CTA
- monthly review CTA
- recent reviews list

Main actions:

- start weekly review
- start monthly review
- open past review

### 7. Quick Thought

Purpose:

- ultra-fast capture when the user does not want the full journal flow

Fields:

- short text
- optional tag or type later

Behavior:

- opens as sheet/dialog
- save in one tap
- can later be attached to today journal or remain standalone

### 8. Settings

Purpose:

- app configuration and data controls

Sections:

- data export/import
- backup/restore
- enabled domains
- theme
- privacy
- integrations later

---

## Home Screen Detailed Layout

This is the recommended order for the upgraded home screen.

### Block 1 - Header

- greeting with name
- weekday + date
- settings icon

Tone:

- calm and direct
- not motivational fluff

Example copy:

- `Good evening, Taha`
- `Friday, May 8`

### Block 2 - Today Status Hero

Main card on the screen.

Shows:

- entry status: `Not started`, `In progress`, or `Complete`
- completion percentage for today journal
- current streak
- one short status line

Primary CTA:

- `Continue Today's Journal`

Secondary CTA:

- `Minimum Mode`

Example status lines:

- `You have not checked in yet today.`
- `You are halfway through today's reflection.`
- `Today's journal is complete. Review your patterns later tonight.`

### Block 3 - Quick Capture Row

Three compact actions:

- `Quick Thought`
- `Log Domains`
- `Open History`

This replaces the current task-heavy quick actions.

### Block 4 - Snapshot Preview

Compact read-only preview of today's objective inputs:

- sleep
- deep work
- workout
- screen time
- social battery

Purpose:

- make the journal feel measurable, not vague

### Block 5 - State Pulse

A compact triplet for:

- morning
- afternoon
- evening

Each chip shows whether that time block has been filled and the last selected mood/energy label.

### Block 6 - Pending Review Card

Shows one of:

- weekly review due
- monthly review due
- all caught up

CTA:

- `Start Review`

### Block 7 - Insight Preview

One small local insight only.

Examples:

- `Low sleep days correlate with weaker focus.`
- `Workout days show better evening mood stability.`

This should tease Analytics, not replace it.

---

## Main User Flows

### First-time user flow

1. Splash
2. Onboarding
3. Choose enabled domains
4. Optional permissions later, not upfront unless essential
5. Land on Home
6. Hero CTA drives first journal entry

### Daily use flow

1. User opens app
2. Home shows today's state
3. User taps `Continue Today's Journal`
4. Completes full journal or minimum mode
5. Returns to Home
6. Home updates streak, completion, and preview blocks

### Bad day / low energy flow

1. User opens app
2. Home hero suggests `Minimum Mode`
3. User answers only:
   - what happened
   - what went badly
   - what did I avoid
   - tomorrow top 3
4. Entry saves as minimum completion
5. Home reflects partial completion, not failure

### Quick thought flow

1. User taps FAB
2. Quick Thought sheet opens
3. User types short note
4. Saves instantly
5. Note appears in journal-related history

### Weekly reflection flow

1. Home shows pending weekly review
2. User taps `Start Review`
3. Reviews screen opens weekly template
4. User completes reflection
5. Analytics and review history update

### Pattern-seeking flow

1. User notices weak focus trend on Home preview or Review
2. Opens Analytics
3. Sees trend lines and trigger frequency
4. Opens today's journal or domains
5. Logs an action fix

---

## Home Screen Rules

These rules matter during implementation.

- Home should not become a dumping ground for every feature
- Home should answer: `What is today's status, and what should I do next?`
- The hero card must dominate visually
- Only one primary CTA should feel strongest: continue journal
- Analytics on Home should stay tiny and teaser-like
- Reviews should feel important but secondary to today's journal
- Old habits/tasks language should be removed from the top-level home experience

---

## Recommended Copy Direction

Use calm, precise labels.

Prefer:

- `Continue Today's Journal`
- `Minimum Mode`
- `Log Domains`
- `Weekly Review`
- `Pattern Preview`

Avoid:

- gamified language
- loud productivity jargon
- generic wellness fluff

---

## Recommended MVP Navigation Implementation

For the first draft, map the existing app to this structure like this:

- current `Home` stays `Home`
- current `Journal` evolves into journal hub + history
- current `DailyTrackerScreen` becomes the new daily journal detail screen
- current `Insights` becomes `Analytics`
- current `WeeklyReviewScreen` becomes the first `Reviews` implementation
- current `Plan` tab should be replaced by `Domains`
- current `Habits` tab should be removed from primary navigation

This keeps the code migration manageable while aligning with the upgraded product identity.

---

## Build Order

### Step 1

Finalize navigation and home content model.

### Step 2

Refactor `HomeScreen` into the new dashboard shell with static/draft data.

### Step 3

Refactor `DailyTrackerScreen` into the new journal flow.

### Step 4

Replace `Plan` with `Domains` in navigation.

### Step 5

Convert `Insights` into `Analytics` and `WeeklyReview` into a `Reviews` hub.

---

## Recommendation

The best next implementation move is:

1. lock this tab structure
2. redesign `HomeScreen` first with the current aesthetic
3. wire its CTAs to the journal-first flow
4. then rebuild the daily journal detail screen behind it
