# HabitMind — AGENTS.md

## Project Structure

- **Android app** — Kotlin + Jetpack Compose (Gradle-based)
- **Frontend (React Native)** — TypeScript + Expo SDK 54 (in `Frontend/src-rn/`)

---

## 1. Build / Lint / Test Commands

### Android (Kotlin)

```bash
# Build all variants
./gradlew assembleDebug

# Run all unit tests
./gradlew testDebugUnitTest

# Run a single unit test
./gradlew testDebugUnitTest --tests "com.example.myapplication.ExampleUnitTest"

# Run all instrumented tests
./gradlew connectedDebugAndroidTest

# Lint check
./gradlew lint

# Clean build
./gradlew clean
```

### Frontend (React Native / TypeScript / Expo)

```bash
# Install deps
cd Frontend/src-rn && npm install

# Start Expo dev server
cd Frontend/src-rn && npx expo start

# Start on Android
cd Frontend/src-rn && npx expo start --android

# Start on iOS
cd Frontend/src-rn && npx expo start --ios

# Start on web
cd Frontend/src-rn && npx expo start --web

# TypeScript type check
cd Frontend/src-rn && npx tsc --noEmit
```

> **No ESLint, Prettier, or lint scripts are configured for the frontend.** TypeScript `strict: true` is the only static analysis.

---

## 2. Code Style Guidelines

### Android (Kotlin)

#### Imports
- **Explicit imports only** — no wildcard `*` imports
- Group order: Android SDK → AndroidX → Jetpack Compose → Kotlin stdlib → project-local (`com.habitmind.*`)
- Blank lines separate groups

#### Formatting
- 4-space indentation (Gradle property: `kotlin.code.style=official`)
- Opening braces on same line (Kotlin standard)
- Trailing commas in multi-line function calls and constructor invocations
- `: Type` spacing — single space before colon
- Parameter lists break after opening paren when long

#### Types & Naming
- **Package:** `com.habitmind.<layer>.<feature>` (e.g., `com.habitmind.data.database.dao`)
- **Classes:** PascalCase — `Habit`, `HabitsViewModel`, `AddHabitDialog`
- **Entities:** Plain nouns, `data class` with `@Entity(tableName = "...")`, `@PrimaryKey(autoGenerate = true) val id: Long = 0`
- **DAOs:** Interface with `@Dao`, `suspend fun` for writes, `Flow` wrapped in `Flow<List<T>>` for reactive reads
- **Repositories:** Constructor injection of DAOs, expose `Flow` properties + `suspend fun` methods
- **ViewModels:** Extend `AndroidViewModel(application)`, access singletons via `(application as HabitMindApplication).repository`
- **UI State:** `data class *UiState` backed by `MutableStateFlow` exposed as `StateFlow.asStateFlow()`
- **Composables:** PascalCase, state-hoisting pattern with lambda defaults (`onConfirm: () -> Unit = {}`)
- **ViewModel injection:** Default param `viewModel: XViewModel = viewModel()`
- **Theme constants:** Objects (`object Spacing { ... }`), dark-mode-only via `darkColorScheme()`
- **Navigation:** `sealed class Screen(val route: String)` with `data object` for each destination
- **Dialogs:** Managed via a single `ActiveDialog` enum state

#### Error Handling
- **Database operations:** Wrapped in `try { ... } catch (e: Exception) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }`
- **Cleanup blocks:** Silent catch `catch (_: Exception) { }`
- **Results:** Sealed class patterns (`ExportResult.Success`, `ExportResult.Error`)
- **Workers:** Return `Result.retry()` on failure
- **Null safety:** `?.let { }`, `?:`, `?.map { } ?: emptyList()` throughout

#### Compose & Style
- `val uiState by viewModel.uiState.collectAsState()` pattern
- Animations: `Animatable`, `animateFloatAsState`, `spring`/`tween` specs, `FastOutSlowInEasing`
- Glassmorphism: `Brush.verticalGradient`, `copy(alpha = ...)`, `shadow`, `border` with translucent colors
- `@Composable` on all composables (including private helpers)
- `@OptIn` for experimental APIs (e.g., `@OptIn(ExperimentalMaterial3Api::class)`)

#### Room / Data Layer
- `@Database` with `@TypeConverters`, singleton pattern (double-checked locking with `@Volatile`)
- KSP for Room annotation processing
- DAO naming: `insert`, `update`, `delete`, `getById`, `getByIdFlow`, `getAllActive`, etc.

---

### Frontend (React Native / TypeScript / Expo)

#### Imports
- Group order: React → react-native → third-party libraries → project modules → theme/style constants
- Blank lines separate groups
- Relative imports only for project code (`../`, `./`); path alias `@/` (configured but unused)
- Barrel files: `index.ts` re-exports in `components/ui/`, `screens/`, `navigation/`, `theme/`

#### Formatting
- 4-space indentation
- Semicolons required
- No trailing commas
- Function declarations for components, arrow functions for callbacks/inline handlers
- JSX double quotes for string props

#### Types & Naming
- **Components:** PascalCase, named exports (not default — only `App.tsx` uses `export default function App()`)
- **Screens:** PascalCase suffixed with `Screen` — `HomeScreen`, `PlanScreen`
- **Props:** `interface` for objects/data models, `type` for unions/literals
- **Props naming:** Component name suffixed with `Props` — `ButtonProps`, `CardProps`
- **Shared types:** Exported `interface` from `AppContext.tsx`: `Task`, `Habit`, `JournalEntry`, `Goal`, `UserProfile`, `DailyLog`
- **Navigation:** Typed param lists (`RootStackParamList`, `TabParamList`) with exported types
- **Theme:** `ThemeMode` type as `'light' | 'dark'`

#### Component Patterns
- `StyleSheet.create()` at bottom of every component file
- No inline style objects except dynamic theme colors via array spread: `style={[styles.foo, { color: colors.primary }]}`
- `Pressable` used instead of `TouchableOpacity` / `TouchableHighlight`
- `gap` property in flex layouts (React Native 0.71+)
- Icons from `lucide-react-native` as components

#### State Management
- **React Context:** `AppContext` for all app data (tasks, habits, journal, goals, logs, mood, profile)
- **Custom hooks:** `useApp()` and `useTheme()` with guard clause pattern:
  ```ts
  export function useApp() {
      const ctx = useContext(AppContext);
      if (!ctx) throw new Error('useApp must be used within AppProvider');
      return ctx;
  }
  ```
- `useCallback` for mutation functions
- AsyncStorage persistence via `saveState`/`loadState`
- ID generation: `t${Date.now()}`, `j${Date.now()}`

#### Error Handling
- Guard pattern for context hooks (throws)
- Empty catch blocks for AsyncStorage: `try { ... } catch { }`
- `Alert.alert('Error', message)` for user-facing errors
- No try-catch around state-updating callbacks

#### Theme System
- `ThemeProvider` + `useTheme()` with `colors` resolved per theme
- Respects system color scheme via `useColorScheme()`
- Persists to AsyncStorage under key `habitmind-theme`
- Dark mode default

---

## 3. Git Workflow

- Commit messages: concise, focus on "why" not "what"
- Do not commit without explicit user request
- No force push to main/master
- No commit --amend unless explicitly requested and safe

---

## 4. Key Constraints

- **Android:** Room + DataStore only (no cloud), dark mode default, offline-first
- **Frontend:** Expo SDK 54, React 19.1, React Native 0.81.5, React Navigation v7
- **TypeScript:** `strict: true` mode, `"jsx": "react-jsx"`, `"moduleResolution": "bundler"`
- **No login, no cloud sync, no AI** — single user, fully offline
