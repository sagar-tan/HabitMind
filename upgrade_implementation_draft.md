# HabitMind Upgrade Implementation Draft

## Goal

Reposition HabitMind from a broad habits/plans tracker into a structured self-improvement journaling app.

The new product goal is:

- daily self-reflection with low-friction structured inputs
- behavior and focus diagnostics
- physical and mindfulness awareness
- weekly/monthly reviews
- local analytics from personal patterns
- strict local-first ownership with no AI and no cloud

This draft turns `upgrade.md` into an implementation target that fits the current Android codebase.

---

## Current App Baseline

The current Android app already has useful foundations:

- Room database with repositories and ViewModels
- `DailyTracker` for one-entry-per-day logging
- `JournalEntry` timeline for free-form text, voice, and image notes
- `Goal` and `GoalUpdate` for long-term progress
- `WeeklyReviewScreen`, `InsightsScreen`, and onboarding flow
- local export/import foundation in `DataExportManager`

The current gap is that the data model and UI are still centered on habits/tasks plus a discipline tracker, not the new journal-first product definition.

---

## Product Translation

### What stays

- offline-first architecture
- Room + DataStore + local media storage
- Compose navigation and current onboarding shell
- history, reviews, analytics, and settings as product pillars

### What changes

- `DailyTracker` becomes the main daily journal record instead of a simple discipline checklist
- daily entry structure shifts from generic sliders to richer reflection sections
- home screen becomes a CTA-heavy journal dashboard
- life domains become first-class structured inputs
- reviews and analytics derive from journal data rather than mostly habits/tasks

---

## Recommended MVP Cut

Implement the upgrade in 3 phases instead of rewriting everything at once.

### Phase 1 - Journal-first core

- onboarding update for name, enabled domains, and journaling intent
- dashboard update with continue journal / minimum mode / streak / quick stats
- new daily journal schema
- daily journal screen with structured sections
- journal history by date
- local persistence only

### Phase 2 - Insight layers

- life domains screen
- weekly review
- monthly review
- analytics based on journal data

### Phase 3 - Device integrations

- usage stats
- export/import polish
- backup/restore
- optional Health Connect inputs

---

## First Draft Architecture

Keep the existing MVVM + repository structure, but split the current `DailyTracker` responsibilities into clearer models.

### New primary entities

#### 1. `DailyJournal`

One row per date. Holds the overall daily entry shell.

Suggested fields:

- `id: Long`
- `date: LocalDate`
- `createdAt: LocalDateTime`
- `updatedAt: LocalDateTime`
- `mode: JournalMode` (`FULL`, `MINIMUM`)
- `sleepHours: Float`
- `sleepQuality: SleepQuality`
- `workoutCompleted: Boolean`
- `workoutType: String`
- `workoutDurationMin: Int`
- `deepWorkHours: Float`
- `screenTimeHours: Float`
- `socialBattery: SocialBattery`
- `bestPart: String`
- `worstPart: String`
- `mentallyOccupiedBy: String`
- `brainDump: String`
- `whatWentWell: String`
- `whatWentBadly: String`
- `whyItHappened: String`
- `patternNoticed: String`
- `whatIAvoided: String`
- `dailyLesson: String`
- `identityAlignment: IdentityAlignment`
- `isComplete: Boolean`

#### 2. `JournalStateSlice`

Tracks morning / afternoon / evening state without stuffing repeated columns into `DailyJournal`.

Suggested fields:

- `journalId: Long`
- `period: DayPeriod` (`MORNING`, `AFTERNOON`, `EVENING`)
- `energy: EnergyLevel`
- `mood: MoodState`
- `stress: StressLevel`

Primary key: `journalId + period`

#### 3. `JournalEvent`

Timeline bullets for the day.

Suggested fields:

- `id: Long`
- `journalId: Long`
- `position: Int`
- `text: String`

#### 4. `BehaviorDiagnostic`

Either store as booleans on `DailyJournal` or normalize as tags. For the first draft, use a separate table so analytics stay simple.

Suggested fields:

- `id: Long`
- `journalId: Long`
- `category: DiagnosticCategory`
- `key: String`
- `isChecked: Boolean`

Examples of keys:

- `procrastinated`
- `avoided_important_work`
- `mental_fog`
- `energy_crash`

#### 5. `ActionFix`

Stores problem -> fix pairs.

Suggested fields:

- `id: Long`
- `journalId: Long`
- `problem: String`
- `fix: String`
- `position: Int`

#### 6. `TomorrowPriority`

Exact top 3 tasks for tomorrow.

Suggested fields:

- `id: Long`
- `journalId: Long`
- `slot: Int` (`1..3`)
- `text: String`

#### 7. `DomainCheckin`

Reusable domain logging table for Physical, Focus, Mindfulness, Learning, Work, Social, and Identity.

Suggested fields:

- `id: Long`
- `date: LocalDate`
- `domain: LifeDomain`
- `promptAnswer: String`
- `summaryValue: String`
- `notes: String`

#### 8. `DomainFlag`

Checkbox/multi-select items attached to a domain check-in.

Suggested fields:

- `id: Long`
- `checkinId: Long`
- `key: String`
- `isChecked: Boolean`

---

## Enum Draft

Create Kotlin enums plus Room converters for:

- `JournalMode`
- `SleepQuality`
- `SocialBattery`
- `DayPeriod`
- `EnergyLevel`
- `MoodState`
- `StressLevel`
- `LifeDomain`
- `IdentityAlignment`
- `DiagnosticCategory`

This matches the new requirements better than the current 1-10 generic mood/focus sliders.

---

## UI Draft

### Home Dashboard

Refactor `HomeScreen` into a journal hub.

Primary blocks:

- today journal status
- streak and completion rate
- continue journal CTA
- minimum mode CTA
- quick thought CTA
- pending weekly/monthly review CTA
- small insight preview

### Daily Journal Screen

Refactor `DailyTrackerScreen` into a sectioned journal flow.

Recommended section order:

1. daily snapshot
2. morning / afternoon / evening state
3. event timeline
4. diagnostics
5. emotional truth
6. brain dump
7. reflection
8. action fixes
9. tomorrow top 3
10. daily lesson

Use collapsible cards so the screen stays manageable.

### Life Domains Screen

Add one screen with tabs for:

- Physical
- Focus
- Mindfulness
- Learning
- Work
- Social
- Identity

### Reviews Screen

Merge weekly and monthly review creation into one review hub.

### Journal History Screen

Start with a simple date list backed by `DailyJournal`, then add search later.

---

## ViewModel / Repository Draft

### New repositories

- `DailyJournalRepository`
- `DomainCheckinRepository`
- `ReviewRepository`

### New ViewModels

- `DailyJournalViewModel`
- `JournalHistoryViewModel`
- `LifeDomainsViewModel`
- `ReviewsViewModel`

### Migration path from current code

- keep `JournalViewModel` for free-form entries and quick thought capture
- replace `DailyTrackerViewModel` with `DailyJournalViewModel`
- keep `GoalRepository` for longer-term progress, but align dashboard and reviews with journal outcomes first

---

## Analytics Draft

Phase 1 analytics should stay intentionally small.

Start with:

- journal streak
- journal completion percentage
- sleep trend
- deep work trend
- screen time trend
- workout consistency
- identity alignment frequency
- most common focus killer
- most common emotional trigger

Only add correlations after the new journal schema is stable.

---

## Database Notes

- the current Room DB uses `fallbackToDestructiveMigration()` in `app/src/main/java/com/habitmind/data/database/HabitMindDatabase.kt`, so schema changes are easy for early drafts but unsafe for preserving real user data
- before shipping the upgraded journal model, add real migrations
- extend `Converters.kt` for all new enums and any list-backed serialization that remains

---

## First Implementation Slice

This is the recommended first coding slice for the upgrade.

### Slice A - schema + shell

- add new journal entities and enums
- register them in `HabitMindDatabase`
- add DAOs and repositories
- add stub ViewModels

### Slice B - dashboard + journal UI

- refactor `HomeScreen` copy and CTAs around journaling
- replace `DailyTrackerScreen` sections with the new journal structure
- keep auto-save behavior from the current tracker screen

### Slice C - history

- list daily journals by date
- open journal for any selected date

This gives a real end-to-end journaling loop without taking on domains, reviews, analytics, and integrations at the same time.

---

## Concrete Code Targets

The first Android implementation should touch these areas first:

- `app/src/main/java/com/habitmind/data/database/entity/`
- `app/src/main/java/com/habitmind/data/database/dao/`
- `app/src/main/java/com/habitmind/data/repository/`
- `app/src/main/java/com/habitmind/ui/viewmodel/`
- `app/src/main/java/com/habitmind/ui/screens/home/HomeScreen.kt`
- `app/src/main/java/com/habitmind/ui/screens/journal/DailyTrackerScreen.kt`
- `app/src/main/java/com/habitmind/navigation/HabitMindNavHost.kt`

---

## Recommendation

Treat `upgrade.md` as the product definition and this file as the engineering draft.

If you want the cleanest first implementation, the next step should be:

1. introduce the new Room entities and enums
2. replace the current `DailyTracker` screen with a journal-first draft
3. update the dashboard CTAs to drive daily journaling instead of generic tracking
