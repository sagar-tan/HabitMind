#ProjectMap

# System Architecture

HabitMind is built as a local-first, offline-only Android application using modern declarative UI and reactive data patterns.

## 🏗️ Technical Stack
- **Language**: Kotlin 2.1+
- **UI Framework**: Jetpack Compose (Material 3)
- **Database**: Room Persistence Library (Version 6)
- **Asynchronous Flow**: Kotlin Coroutines & Flow
- **Navigation**: Jetpack Compose Navigation

## 🧱 Design Philosophy
- **Journal-First**: The application is structured around a daily reflection ritual. Habit tracking and tasks are secondary actions that feed into the journal.
- **Glassmorphism**: A premium UI style using translucent surfaces, vertical gradients, and subtle shadows to create depth and focus.
- **ADHD Friendly**: High-contrast labels, minimal decorative distractions, and one primary action per screen.

## 🎨 Visual Identity
- **App Icon**: The "Neural Crystal"—A simplified multi-faceted crystal with a glowing internal neural network, protected by two minimalist arcs.
- **Color Palette**: 
  - Background: Deep Charcoal (#121212)
  - Accent: Electric Indigo (#6366F1)
  - Success: Spring Green (#4ADE80)
  - Warning/Negative: Coral Red (#F87171)

## 🗺️ Layers
1. **Data Layer**: Room DAOs and Entities (`com.habitmind.data.database`)
2. **Repository Layer**: Domain logic and data orchestration (`com.habitmind.data.repository`)
3. **UI Layer**: ViewModels and Composable Screens (`com.habitmind.ui`)
4. **Navigation Layer**: Route definitions and Graph management (`com.habitmind.navigation`)

## 🔗 Related Documents
- [[Data_Schema]]
- [[Journal_System]]
