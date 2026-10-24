# Vyro — Production-Quality Implementation Plan (10/10)

## Objective

Fix the existing Vyro project without replacing working code. Prioritize reliable cross-account video publishing and playback, correct Firebase security, complete account deletion, and functional settings. Implement the Viral Tap Reflex mini-game after the core video platform is verified.

This plan details the phased execution, root-cause diagnosis, explicit security rules, honest data handling, and automated verification matrix.

---

## 1. Guiding Principles & Requirements
1. **Root-Cause Fixes**: Trace each bug to its root cause; do not patch symptoms with volatile in-memory guest lists.
2. **Preserve Architecture**: Retain existing Compose UI, ExoPlayer integration, navigation, and package versions.
3. **Single Source of Truth**: Firestore `posts` collection is authoritative for all public posts, videos, and shorts.
4. **Security & Privacy**: Client-side cannot spoof `ownerId` or bypass visibility (`PUBLIC`, `UNLISTED`, `PRIVATE`).
5. **Honest States**: Distinguish loading, success, empty, offline, and permission-denied states truthfully.

---

## 2. Phase 0 — Repository & Architecture Audit

### Files to Inspect & Fix:
- `VyroRepository.kt`: Migrate from session-isolated in-memory lists to authoritative Firestore queries with real-time snapshot listeners.
- `MediaAssets.kt`: Provide verified playable video sources and reference the provided video asset appropriately.
- `SettingsScreen.kt` & `MainActivity.kt`: Remove bottom bar obstruction on Settings, add proper insets, and wire every toggle (Theme, Autoplay, Mute, Notifications, Cache, Delete Account).
- `firestore.rules`: Implement fine-grained zero-trust security rules for `posts`, `users`, and `comments`.
- `GameScreen.kt`: Add the requested Viral Tap Reflex mini-game with combo streaks and local high-score tracking.

---

## 3. Phase 1 — Correct Data Model & Cross-Account Synchronization
- **Schema Alignment (`Post` model)**:
  - `id`: Unique post ID
  - `ownerId`: Authenticated user ID (enforced from Firebase Auth)
  - `title`, `description`
  - `format`: `"short"` vs `"long"` (or `mediaType`: `SHORT` / `LONG_VIDEO`)
  - `videoUrl`: Playable media URL or valid storage path
  - `thumbnailUrl`: Thumbnail image URI
  - `visibility`: `"PUBLIC"`, `"UNLISTED"`, `"PRIVATE"`
  - `createdAt`: Server timestamp
  - `likesCount`, `commentsCount`, `viewsCount`
- **Cross-Account Sync**:
  - `createPost()` writes directly to Firestore `posts/{postId}`.
  - `observePosts()` queries Firestore ordered by `createdAt desc` with snapshot listener, ensuring Account B immediately sees Account A's public uploads upon refresh/event.

---

## 4. Phase 2 — Reliable Media Storage & Playback
- **Media Playback**:
  - Ensure ExoPlayer handles network streaming and local file playback without crashing.
  - Integrate user video asset (`/public/assets/aistudio/WhatsApp_Video_2026-10-22_at_10.08.31_103e63ee.mp4`) as a featured Short by creator "Zaid | The Digital Don".
  - Clean error states when media is buffering or fails to load.

---

## 5. Phase 3 — Firestore Security Rules (`firestore.rules`)
- Public read access for `visibility == "PUBLIC"`.
- Private posts readable only by `request.auth.uid == resource.data.ownerId`.
- Create allowed only if `request.auth.uid == request.resource.data.ownerId`.
- Update and delete restricted strictly to the content owner.

---

## 6. Phase 4 — Safe Account Deletion & Data Cleanup
- Clear confirmation dialog detailing data removal.
- Deletion workflow:
  1. Delete user's profile document in Firestore `users/{uid}`.
  2. Query and delete all user-owned posts in Firestore `posts`.
  3. Delete Firebase Auth user credentials (`currentUser.delete()`).
  4. Reset local session and redirect to Welcome/Home feed.
- Handle re-authentication requirement gracefully with informative feedback.

---

## 7. Phase 5 — Functional Settings & Layout Inset Fix
- In `MainActivity.kt`, hide bottom navigation bar on `Screen.Settings` and provide top navigation back arrow.
- Apply full window insets so Danger Zone and "Delete Account" are completely unobstructed.
- Make all preferences functional:
  - Theme mode (Dark, Light, System) via StateFlow / DataStore.
  - Autoplay toggle.
  - Default audio muted toggle.
  - Notification preference switches.
  - Clear cache action with accurate storage calculation and feedback toast.

---

## 8. Phase 6 — Viral Tap Reflex Mini-Game
- Dynamic 30-second tap round.
- Randomized viral target spawns (Hearts, Fire, Rockets, Stars).
- Combo multiplier system (2x, 3x, 5x) for unbroken streaks.
- Personal best high-score persisted locally.
- Share score action using Android share intent.

---

## 9. Verification & Testing Matrix

### Automated Checks
- Compile verification: `compile_applet`.
- Unit test suite: `gradle :app:testDebugUnitTest` covering:
  - Post creation and Firestore serialization.
  - Cross-account visibility filtering (`PUBLIC` vs `PRIVATE`).
  - Account deletion data cleanup.
  - Game scoring and combo mechanics.

### Manual Verification
1. **Cross-Account Visibility**: Upload on Account A -> log in to Account B -> verify video appears in Shorts and Home feeds and plays smoothly.
2. **Settings**: Navigate to Settings -> verify bottom bar is not in the way -> test all toggles -> verify Delete Account dialog triggers and functions.
3. **Featured Video**: Verify Zaid's Short plays seamlessly with sound toggle and scrubber.
4. **Mini-Game**: Play 30s challenge -> verify combo multipliers, timer countdown, and high scores.
