# Comprehensive Design & Technical Specification: Reflexiō

Reflexiō is an atmospheric journaling app that prioritizes emotional safety, self-reflection, and a therapeutic ritual over rapid data logging. It serves as a strong reference for building a "Premium Atmosphere" within a utility app.

---

## 1. Core Brand Identity & Visual Language

### Aesthetic: "Dreamy Surrealism"
Unlike most productivity or journaling apps that use flat, minimal, or industrial design, Reflexiō uses 3D-style, stylized vector illustrations of mountains, oceans, and giant symbolic letters. This creates a vast, calm, and "dream-like" state, framing the app as a safe space for reflection.

### Color Systems
- **Primary Gradients**:
  - **Action Pink**: `#EC407A` → `#7E57C2` (Used for primary CTA buttons, active timeline pills, and selection highlights).
  - **Golden Premium**: Red/Orange → Yellow/Gold (Used exclusively for the "Special Offer" CTA to make it stand out).
  - **Atmospheric Backgrounds**: Deep Midnight Blue (`#0D1117`) fading to Royal Blue or vibrant Magenta. The backgrounds often feature stylized suns or moons.
- **Surface Palette (Glassmorphism)**:
  - **Frosted Glass**: The primary content cards use `#FFFFFF` (10-15% alpha) with high Gaussian blur (30px+). This creates separation from the complex backgrounds without losing the atmospheric color bleed.
  - **Dark Mode Surfaces**: Deep Navy or Charcoal are used for list items in Settings to maintain readability.

### Typography
- **Headlines**: Wide-tracked, bold Sans-Serif (e.g., *Montserrat* or *Outfit*) in all-caps for titles like "KEEP YOUR NOTES PRIVATE" or "ANSWER THE QUESTION OF THE DAY."
- **Content**: Clean, highly legible Sans-Serif for body text.
- **The "Diary" Feel**: Playful, handwritten or cursive fonts (e.g., *Satisfy* or *Dancing Script*) are used specifically for diary entry previews to simulate a physical paper journal.

---

## 2. Feature-by-Feature Breakdown & UX Patterns

### A. The "Daily Ritual" (Onboarding & Home)
- **Question of the Day**: Instead of asking for a generic mood, Reflexiō provides a guided prompt that changes daily (e.g., "Are you a happy person?"). This solves "blank page syndrome".
- **Dashboard Illustration**: A static illustration at the bottom of the home screen (e.g., a person and a dog watching the sunset). This anchors the UI and makes the app feel "alive".
- **Floating Tags**: Interaction-heavy activity/emotion tags (e.g., "Gratitude," "Anger") that look like physical pills or stickers. They can be dynamically "popped" into the diary and closed with an 'X'.

### B. Timeline & Archives
- **The "Questions" Archive**: A vertical timeline connecting past, present, and future questions.
- **Anticipation Design**: Future entries (e.g., tomorrow's question) are visible but locked with a padlock icon and a desaturated, translucent UI. This creates a "streak" incentive and anticipation for the next day.

### C. Self-Reflection Tools & Statistics
- **Word Cloud**: A circular visualization of the user's most used words, parsed from diary entries. Words are color-coded (Blues, Teals, Purples) and sized by frequency.
- **Mood Flow Chart**: A continuous, smooth line graph (often using a rainbow gradient stroke) showing high/low emotional points over a week or month. Horizontal banding separates the mood levels (Rad, Good, Meh, etc.).
- **Monthly Averages**: Vertical bar charts showing the dominant mood per month, utilizing the semantic mood colors.

### D. Customization & Modularity
- **Widget Management**: A dedicated screen where users can customize their dashboard. Users can reorder (via drag-and-drop hamburger handles) or hide specific components (Questions, Mood Report, Word Cloud) using a red "minus" action.

### E. Privacy & Monetization
- **Circular Passcode Keypad**: A minimalist numeric keypad for app lock. The active pressed digit is highlighted with a solid blue circle, providing clean visual feedback.
- **Backup Integration**: A user-centric backup screen framing the user's Google profile picture inside a halo, making cloud sync feel personal rather than technical.
- **Premium Conversion**: Uses a Hero Image (a beautiful sunset ocean photograph) blended into a gradient, followed by a simple, scannable checklist with white checkmarks for premium features.

---

## 3. Technical Implementation Strategy

### Android (Jetpack Compose)
- **Glassmorphism Containers**: Use `Modifier.background(color = Color.White.copy(alpha = 0.1f))` combined with a `Modifier.blur(30.dp)` (Requires Android 12+ / API 31). Fallback to a solid dark gray for older versions.
- **Reorderable Widgets**: Utilize a `LazyColumn` combined with a library like `shreyashk7/reorderable` (or custom drag-and-drop state) to power the Widget Manager. Store the ordering array in Room or DataStore.
- **Timeline UI**: Implement using a custom `DrawModifier` or `Canvas` that draws the vertical line behind a `Column` of timeline entries.
- **Passcode Keypad**: Implement using a `LazyVerticalGrid` with a fixed 3x4 layout. Use `Box` with `clip(CircleShape)` and an interaction source to trigger the highlight on press.
- **Charts**: Use `Canvas` with `Path` and `CubicBezier` for the smooth Mood Flow line. Apply a `Brush.horizontalGradient` as the stroke.

### Frontend (React Native / Expo)
- **Glassmorphism**: Use `@react-native-community/blur` for robust cross-platform frosted glass effects over the gradient backgrounds.
- **Modular Dashboard**: Implement the home screen as a `FlatList` where `data` is driven by the user's saved widget preferences (stored in `AsyncStorage`).
- **Timeline Feed**: Use a `FlatList` where the `ItemSeparatorComponent` contains the vertical connecting line.
- **Word Cloud**: Can be implemented using a text layout algorithm that maps frequency to `fontSize`, rendered inside a `View` with `flexWrap: 'wrap'` and `justifyContent: 'center'`.
- **Onboarding Carousel**: Utilize `react-native-reanimated-carousel` for the smooth horizontal transitions and interactive features shown in the tour.

---

## 4. Synergy with HabitMind "Neural Crystal"

The Reflexiō analysis provides key elements we should adapt for HabitMind's "Neural Crystal" aesthetic and functionality:

1. **Modular Home Screen**: We MUST allow users to "Widgetize" the HabitMind home screen. Users should decide if they want the "Habit Tracker" at the top or the "Journal Prompt" at the top.
2. **Timeline Ritual**: Use the vertical timeline aesthetic for the daily habit log and journal entries. Seeing tomorrow's habits "locked" or greyed out creates anticipation.
3. **The "Atmosphere" Layer**: Instead of plain black backgrounds, we should introduce subtle, very dark gradients (e.g., Black to Midnight Navy) or low-opacity abstract shapes in the background of our Platinum-themed app to give it a premium, spatial feel.
4. **Question of the Day**: Integrate a daily reflective prompt into the journaling module to reduce friction and encourage daily engagement.
