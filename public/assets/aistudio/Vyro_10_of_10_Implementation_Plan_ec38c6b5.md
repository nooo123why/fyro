# Vyro --- Production-Quality Implementation Plan (10/10)

## Objective

Fix the existing Vyro project without replacing working code. Prioritize
reliable cross-account video publishing and playback, correct Firebase
security, complete account deletion, and functional settings. Keep the
Viral Tap Reflex mini-game optional and implement it only after the core
video platform is verified.

This document is an implementation plan, not evidence that any code has
already been changed or tests have passed. All verification results must
be recorded only after tests are actually run.

------------------------------------------------------------------------

## 1. Guiding principles and non-negotiable requirements

1.  Inspect the repository and current app architecture before editing.
2.  Trace each bug to its root cause; do not patch symptoms with
    hard-coded demo data.
3.  Preserve the existing UI, navigation, working features, package
    versions, and project structure wherever practical.
4.  Use one source of truth for post metadata and clear ownership of
    media files.
5.  Never expose private content, credentials, admin privileges, or
    private user data.
6.  Every loading, success, empty, offline, permission-denied, and
    failure state must be represented honestly.
7.  Do not claim a service is connected, deployed, or verified unless
    there is evidence.
8.  Do not add unrelated features until critical data and playback flows
    work.
9.  Use server timestamps and server-authorized operations for sensitive
    counters and mutations.
10. Keep a record of files changed, tests run, actual outcomes, and
    remaining setup steps.

## 2. Phase 0 --- Repository and architecture audit

### Inspect first

-   `VyroRepository.kt`
-   `MediaAssets.kt`
-   `SettingsScreen.kt`
-   `MainActivity.kt`
-   `GameScreen.kt`
-   `NavState.kt`
-   `firestore.rules`
-   `storage.rules` or the project's actual Storage rules file
-   Firebase initialization and authentication/session code
-   Firestore indexes, Gradle configuration, manifests, and relevant
    tests
-   Existing video-player, upload, cache, and account-deletion
    implementations

### Establish the facts

-   Confirm the app is Android/Kotlin/Jetpack Compose and identify
    actual navigation and state-management patterns.
-   Confirm Firebase project configuration exists without printing
    secrets.
-   Identify whether posts are currently stored locally, in Firestore,
    or both.
-   Identify the actual schema for `posts`, `videos`, and `shorts`; do
    not create duplicate collections without a migration plan.
-   Determine how media is stored and whether the referenced MP4 is
    actually bundled in the project.
-   Confirm whether the local MP4 path is an Android asset, packaged
    resource, app file, or web-only public asset. Do not assume
    `/public/...` is available to Android ExoPlayer.
-   Check current dependency versions and the available build/test
    commands.
-   Record baseline build failures before changing code.

**Deliverable:** a short diagnosis mapping each observed issue to its
root cause, files involved, and intended fix.

## 3. Phase 1 --- Correct data model and cross-account synchronization

### Data model

Use the project's existing schema if sound. If the existing schema uses
`posts`, do not introduce a separate `videos` or `shorts` collection
merely for naming convenience. Represent media type explicitly, for
example:

-   `id`
-   `ownerId`
-   `title`
-   `description`
-   `mediaType` (`LONG_VIDEO` or `SHORT`)
-   `mediaPath` (canonical Storage object path where applicable)
-   `thumbnailPath`
-   `visibility` (`PUBLIC`, `UNLISTED`, `PRIVATE`)
-   `status` (`UPLOADING`, `PROCESSING`, `PUBLISHED`, `FAILED`,
    `DELETED`)
-   `createdAt` (server timestamp)
-   `updatedAt` (server timestamp)
-   `durationMs` when known
-   `aspectRatio` when known
-   counters only when maintained by trusted operations

Validate the actual field types and migrate existing documents safely if
needed. Do not store a local filesystem path as a cross-device media
URL.

### Create and observe posts

-   `createPost()` must write the authoritative metadata to Firestore
    and return an explicit success/failure result.
-   Set `ownerId` from the authenticated identity, not from
    user-supplied form data.
-   Use server timestamps and valid status/visibility values.
-   `observePosts()` must query published, discoverable posts with
    pagination and appropriate ordering.
-   Use snapshot listeners only for bounded, useful real-time views;
    unsubscribe when screens are disposed.
-   Merge pages without duplicate items and preserve stable ordering.
-   Distinguish permission errors, offline state, empty feeds, and
    failed queries.
-   Confirm Home and Shorts use the same authoritative data source while
    applying their respective media-type and visibility filters.
-   Do not use hard-coded posts as a substitute for Firestore. Any
    optional sample content must be clearly identified and isolated from
    production data.

### Acceptance tests

-   Account A publishes a public post; Account B sees it after refresh
    and, where applicable, through the active listener.
-   A private post does not appear to Account B.
-   An unlisted post is excluded from public
    discovery/search/recommendations but can be opened through its
    intended direct link subject to the chosen access model.
-   Repeated listener emissions do not duplicate cards.
-   Offline/reconnect behavior is understandable and does not silently
    lose a successful post.

## 4. Phase 2 --- Reliable media storage and actual playback

### Media source rules

-   Use Firebase Storage or an explicitly configured media provider for
    uploaded media.
-   Persist a canonical storage object path and retrieve a valid
    playback URL using the provider's supported API when needed.
-   Never store or depend on a developer's local path such as
    `/public/assets/...` for cross-device playback.
-   Never label a URL "verified" until it has actually been checked.
-   Do not rely on arbitrary third-party MP4 test links for production
    functionality.
-   If the supplied WhatsApp MP4 is present, confirm its real location,
    codec, duration, and whether it may legally be included. Bundle it
    only if that is intentional and appropriate; otherwise upload it
    through the normal creator workflow.
-   Use `MediaItem.fromUri(...)` only with a URI valid for the Android
    runtime and accessible to the signed-in viewer.

### Upload workflow

Implement explicit states:
`SELECTED → UPLOADING → PROCESSING (if applicable) → PUBLISHED`, with
`FAILED` and retry/cancel paths.

Requirements: - Validate supported MIME types and configured file-size
limits. - Use resumable upload support where available. - Show real
upload progress from the upload task, not simulated progress. - Avoid
publishing metadata as playable until upload success and required
processing complete. - Clean up partial uploads safely. - Handle
cancellation, timeout, network loss, permission denial, quota/billing
issues, and retry. - Prevent duplicate posts when retrying after
uncertain network responses. - Ensure upload ownership and destination
paths are enforced by Storage rules.

### Player behavior

-   Reuse the existing ExoPlayer/Media3 implementation if suitable.
-   Handle loading, ready, buffering, ended, and error states.
-   Verify long-form controls: play/pause, seek, duration, volume,
    fullscreen where implemented, and playback resume if supported.
-   Verify Shorts: vertical presentation, tap-to-pause, sound control,
    and feed navigation.
-   Ensure only the active short plays; release players/listeners when
    leaving a screen.
-   Pause playback when app lifecycle or navigation requires it.
-   Preload a small bounded number of nearby items; avoid loading the
    entire feed.
-   Support only formats/codecs the chosen player and target devices can
    actually decode.
-   If adaptive streaming is promised, implement and test the required
    renditions/manifest and playback support. Otherwise describe quality
    support accurately.

### Acceptance tests

-   Upload from Device A/account A, then play from Device B/account B.
-   Test both a long-form video and a vertical short.
-   Test expired/unavailable media URLs, deleted media, unsupported
    codecs, interrupted uploads, and slow network.
-   Confirm a thumbnail alone is never treated as successful playback.

## 5. Phase 3 --- Firestore and Storage security

Review the rules against the real schema and test them in the Firebase
Emulator Suite when available.

### Firestore

-   Publicly readable video metadata must not expose private profile
    fields or sensitive user data.
-   Restrict updates/deletes to the content owner and explicitly
    authorized trusted workflows.
-   Clients must not set arbitrary `ownerId`, administrator flags,
    protected counters, moderation status, or trusted processing status.
-   Validate field types, allowed values, required fields, and immutable
    ownership.
-   Make public/unlisted/private visibility rules explicit.
-   Protect comments, likes, subscriptions, playlists, settings, and
    reports according to ownership and visibility.
-   Prevent duplicate likes/subscriptions through deterministic document
    IDs or equivalent integrity controls.
-   Do not grant blanket read/write access to all authenticated users or
    guests.

### Storage

-   Validate authenticated ownership, object paths, allowed MIME types,
    and practical file-size limits.
-   Ensure private media is not made public by a guessable path alone.
-   Make thumbnail and profile image rules separate from large media
    rules where needed.
-   Ensure users cannot overwrite another user's media or delete other
    users' files.
-   If publicly readable media URLs are used, document the privacy
    implications and use the provider's supported access controls for
    private media.

### Verification

Create positive and negative tests for owner, other authenticated user,
signed-out user, and authorized administrator. Verify denied requests
are rejected by the backend, not merely hidden in the UI.

## 6. Phase 4 --- Safe account deletion and data cleanup

### Required flow

1.  Show a clear confirmation dialog describing what will be deleted.
2.  Require recent authentication or reauthentication when Firebase
    requires it.
3.  Start a trusted, retryable deletion workflow for the user's data and
    owned media.
4.  Delete associated posts, Storage objects, thumbnails, and other
    owned records according to retention policy.
5.  Handle comments, likes, subscriptions, playlists, and references
    consistently without deleting other users' content.
6.  Delete or anonymize content that must remain for platform integrity
    or legal reasons, if applicable, using a documented policy.
7.  Delete the Firebase Authentication user at the correct point in the
    workflow.
8.  Sign out and clear local user-specific state only after the flow has
    reached a well-defined result.
9.  Show a truthful success or failure message and provide a safe
    retry/recovery path.

Do not rely on an unbounded client-side loop to delete a large account.
Use a trusted backend/Cloud Function or other suitable server-side
workflow when needed. Make the operation idempotent so retrying does not
damage unrelated data. Do not claim complete deletion if part of the
process failed.

### Acceptance tests

-   Cancel confirmation: nothing is deleted.
-   Confirm deletion: owned data/media are removed or handled by
    documented policy, and session is cleared.
-   Reauthentication required: user receives the correct prompt.
-   Partial failure: the app reports it and the cleanup can resume
    safely.
-   Other users' posts, comments, and media remain intact.

## 7. Phase 5 --- Settings that genuinely work

### Layout and navigation

-   Hide the bottom navigation bar on Settings, or apply correct
    window/navigation insets consistently.
-   Add a clear back action returning to the expected screen.
-   Verify the Delete Account control is visible, scrollable to, and not
    covered by system or app navigation.

### Preferences

-   Theme: Dark, Light, System Default.
-   Autoplay: persist and enforce the setting.
-   Default audio muted: persist and enforce it in both video
    experiences.
-   Notifications: only show toggles that map to actual notification
    capabilities. A saved preference is not proof that push
    notifications are delivered.
-   Clear cache: clear only appropriate app caches, preserve user
    content, and report success based on the actual operation. Do not
    invent a reclaimed-space number.
-   Account/privacy settings: persist values and enforce them in
    relevant flows.

Use DataStore or the existing established preference layer. Handle
startup restoration and migration. Test persistence after app restart
and sign-in changes.

## 8. Phase 6 --- Real featured video integration

-   Treat the named creator/video as ordinary content with real
    ownership and consent, not a permanently hard-coded special case.
-   If the supplied video is intended to be featured, use the normal
    upload/metadata flow or a controlled, documented seed process.
-   Verify the source file exists, is playable, and is authorized for
    use.
-   Feature it through a data field or curated feed configuration, not
    fragile list-position assumptions.
-   If the media file is absent or invalid, show a graceful empty/error
    state instead of a broken player.
-   Do not claim that it plays on all devices until cross-device
    playback is tested.

## 9. Phase 7 --- Optional Viral Tap Reflex mini-game

Implement only after Phases 1--5 pass the core acceptance tests.

Features: - 30-second reflex round. - Randomized target positions and
spawn timing. - Combo multiplier and miss handling. - Score and high
score. - Accessible touch targets and reduced-motion support. - Optional
haptic feedback. - Share challenge action using the platform's real
share mechanism.

Persist the personal best using the project's existing local preference
architecture. Clearly scope the score to the local account/device unless
cloud synchronization is deliberately implemented. Test scoring and
timer logic independently. This feature must not delay fixes to
publishing, playback, or security.

## 10. Performance, reliability, and observability

-   Use paginated queries and bounded listeners.
-   Avoid downloading full media until playback requires it.
-   Avoid duplicate network calls and repeated player initialization.
-   Release ExoPlayer and listener resources correctly.
-   Provide useful logs without recording passwords, tokens, or private
    media URLs.
-   Add retry/backoff for transient failures where safe.
-   Distinguish expected offline behavior from bugs.
-   Check Firebase read/write/storage/bandwidth costs and document any
    provider plan or billing requirement.
-   Avoid introducing new services or dependencies without a clear need.

## 11. Test and verification plan

### Automated checks

Run the checks supported by the actual repository, such as: - Android
compilation/build task. - Unit tests for repository mapping, visibility
filtering, upload state transitions, deletion workflow orchestration,
and game scoring. - Firestore/Storage rules tests with the Firebase
Emulator Suite where available. - UI tests for navigation, Settings
layout, confirmation dialogs, and major loading/error states. - Static
analysis/lint if configured.

Do not assume a command such as `compile_applet` exists. Inspect the
environment and use the project's real build tools and task names.

### Manual end-to-end matrix

1.  Account A creates and publishes a public short.
2.  Account B sees and plays it.
3.  Account A uploads a long-form video.
4.  Account B opens and plays it.
5.  Verify private content remains inaccessible to Account B.
6.  Like, unlike, comment, and follow; verify state persists after
    relaunch.
7.  Interrupt an upload and verify recovery/retry.
8.  Navigate to Settings on small and large screens and verify controls
    are unobstructed.
9.  Change theme/autoplay/audio settings and relaunch the app.
10. Run account deletion and verify the actual data cleanup outcome.
11. Test slow/offline network and missing/deleted media.
12. Confirm no duplicate playback, duplicate feed entries, or
    unauthorized mutations.

### Reporting format

For each check, record `PASS`, `FAIL`, `BLOCKED`, or `NOT RUN`, with
evidence or the exact error. Never use `PASS` for a test that was not
executed. List any required Firebase Console configuration, OAuth setup,
billing enablement, or deployment separately.

## 12. Suggested implementation order and exit gates

  -----------------------------------------------------------------------
  Phase                   Work                    Exit gate
  ----------------------- ----------------------- -----------------------
  0                       Audit project and       Root causes and actual
                          reproduce bugs          schema documented

  1                       Firestore post sync and Cross-account tests
                          visibility              pass

  2                       Upload, media URLs, and Long video and Short
                          playback                play across
                                                  accounts/devices

  3                       Security rules          Authorized flows pass
                                                  and unauthorized flows
                                                  are denied

  4                       Account deletion        Cleanup is safe,
                                                  retryable, and
                                                  truthfully reported

  5                       Settings                Preferences persist and
                                                  controls are accessible

  6                       Featured video          Real media source
                                                  verified or graceful
                                                  fallback confirmed

  7                       Mini-game               Core platform remains
                                                  stable; game tests pass
  -----------------------------------------------------------------------

Do not move past a critical exit gate merely to add more features. If a
gate is blocked by credentials, Firebase Console access, missing media,
billing, or unavailable emulator tooling, document the blocker and
complete all work possible without pretending it has been resolved.

## 13. Final instruction to the coding agent

Start by inspecting the existing repository and reproducing the reported
problems. Then implement the phases in priority order, making small,
verifiable changes. After each phase, build and test what changed before
proceeding.

Do not stop after writing this plan. Continue into implementation where
the environment allows. Do not rewrite the app from scratch, introduce
fake data to mask failures, weaken security rules, hard-code a local
video path, or report unrun tests as successful.

At completion, report: - Root causes found. - Files changed and why. -
Features implemented. - Firebase services/rules actually configured and
verified. - Tests actually run and their exact outcomes. - Any blocked
items and the reason. - Exact manual steps still required from the
project owner.

**Definition of done:** public content syncs safely across accounts;
real long-form videos and Shorts upload and play across devices; private
content stays private; account deletion and settings behave honestly;
and the build/test results are reported accurately.
