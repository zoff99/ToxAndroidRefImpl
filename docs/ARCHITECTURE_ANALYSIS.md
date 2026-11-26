# TRIfA Android — architecture analysis and conversation-UI redesign

Branch analysed: `experimental_own_risk`
Commit analysed: `7af7dee7251c221eed04808c6a0da354397e9df4`
Scope: repository-wide architecture review, followed by a targeted redesign of the friend, conference, and group message-list screens.

## 1. What this repository is

TRIfA is an Android Tox client built around a native `c-toxcore` JNI layer, a Java Android application, an encrypted SQLCipher/Orma-style persistence layer, and an encrypted virtual filesystem for attachments. The Android project lives in `android-refimpl-app/`; the native bridge lives in `jni-c-toxcore/`; the database schema/generator templates live in `sorma2/`.

The application is not a thin UI over a remote chat API. Most protocol state, message sending, file transfer, group/conference state, and audio/video handling are driven by native code and long-lived Android components. The UI reads from local database state and reacts to callbacks/updates from the Tox service.

## 2. Startup and process model

1. `MainApplication` installs process-wide crash handling and global initialization.
2. `StartMainActivityWrapper` / `MainActivity` load preferences, native libraries, database state, and encrypted storage.
3. `MainActivity` starts or binds to `TrifaToxService`.
4. `TrifaToxService` owns the Tox iteration loop, bootstrap/connectivity logic, callbacks, battery-saving wake/sleep behavior, and notification updates.
5. Chat activities and fragments observe local database state and service-driven updates rather than owning the protocol connection themselves.

Important consequence for UI work: message-list screens must remain tolerant of asynchronous updates, list reloads, paging placeholders, and activity recreation. Replacing the adapters with a new data source would risk breaking message delivery state, file-transfer state, and selection cleanup. The redesign therefore keeps the existing adapters and holders as the source of truth.

## 3. Native/JNI layer

`jni-c-toxcore/jni-c-toxcore.c` exposes a large JNI surface to `MainActivity`, including:

- Tox initialization, iteration, bootstrap, and savedata handling.
- Friend management, friend messages, typing flags, read receipts, and message v2/v3 helpers.
- Conference and group operations.
- File transfer control and chunk handling.
- Audio/video call and NGC group media helpers.
- Utility functions for capabilities, connection state, and protocol metadata.

The Java side declares these as `native` methods on `MainActivity`. The native layer is responsible for protocol correctness; the message-list UI should not invent new delivery semantics.

## 4. Persistence and encrypted storage

The app uses an Orma-generated database wrapper (`com.zoffcc.applications.sorm.OrmaDatabase`) over SQLCipher. Core tables include:

- `Message`: one-to-one friend messages and file-transfer message rows.
- `ConferenceMessage`: legacy Tox conference messages.
- `GroupMessage`: NGC group messages and file-transfer message rows.
- `FriendList`, `ConferenceDB`, `GroupDB`, peer/cache tables, file-transfer tables, relay/bootstrap tables, and options.

Attachments and avatars are commonly stored through IOCipher (`info.guardianproject.iocipher`) rather than ordinary plaintext files. UI code must not assume that a file path is directly readable by Glide or Android media APIs without the existing VFS helpers.

Message rows carry more than text: direction, read state, sent/received timestamps, message version, sync state, file-transfer state, relay/push state, and type. The redesigned presentation layer preserves these fields and maps them to conservative visual states.

## 5. Message-list architecture before the redesign

There are three parallel chat stacks:

| Chat type | Activity | Fragment | Adapter |
|---|---|---|---|
| Friend | `MessageListActivity` | `MessageListFragment` | `MessagelistAdapter` |
| Conference | `ConferenceMessageListActivity` | `ConferenceMessageListFragment` | `ConferenceMessagelistAdapter` |
| Group | `GroupMessageListActivity` | `GroupMessageListFragment` | `GroupMessagelistAdapter` |

Each stack has its own holder classes for text, file transfers, paging, and errors. The adapters choose view types from `Message_model` constants and bind holder-specific behavior. The fragments own RecyclerView setup, paging, search/filter state, scroll-to-bottom behavior, date headers, and swipe-to-quote attachment.

Shared or duplicated behavior includes:

- Long-press selection and contextual action mode.
- Copy/delete/export/info/fullscreen actions through `ToolbarActionMode`.
- Swipe-to-quote through `MessageListSwipeCallback`.
- Date headers and fast-scroll date bubbles.
- File-transfer progress, pause/resume/cancel, preview, export/share, and audio playback controls.
- Emoji popup, attachment picking, audio recording, and typing indicators.

The old UI used bright orange/blue bubble backgrounds, large avatars on many rows, inconsistent metadata placement, and separate header rows below the toolbar. It also had compact/non-compact layout variants that duplicated much of the same structure.

## 6. Selection, action mode, and swipe handling

Selection state is stored in static sets on `MainActivity`:

- `selected_messages`, `selected_messages_text_only`, `selected_messages_incoming_file`
- `selected_conference_messages`
- `selected_group_messages`, `selected_group_messages_text_only`, `selected_group_messages_incoming_file`

`ToolbarActionMode` inflates the contextual menu and performs copy/delete/save/info/fullscreen operations. Cleanup is delicate because some asynchronous operations consume the selection after the action mode closes. The branch already contains fixes to avoid stale highlights after info/fullscreen/group-save paths.

`MessageListSwipeCallback` implements swipe-to-quote with an identity guard: it records the message id at swipe start and only quotes if the same id is still bound at commit time. It disables swipe during selection and for non-text rows. The redesign preserves this callback and its threshold/animation behavior.

## 7. Redesign strategy

The redesign is intentionally presentation-first:

1. Keep the existing activities, fragments, adapters, holders, database queries, paging, selection sets, and swipe callback.
2. Replace the visual shell: toolbar/header, search bar, composer, wallpaper, bubbles, metadata, and action icons.
3. Add a small shared helper, `ChatUI`, that decorates rows after legacy binding and centralizes palette/date/status/composer behavior.
4. Collapse the six friend text-row layout variants into two shared visual structures: incoming and outgoing. The old IDs remain so existing holders continue to bind and handle clicks.
5. Keep file-transfer layouts structurally intact, but restyle their bubbles, alignment, metadata, and icons through the same decoration pass.
6. Add a reply action to the contextual menu for single selected text messages, reusing the existing quote-insertion functions.

This avoids a risky rewrite of protocol-adjacent code while still producing a consistent conversation experience.

## 8. What changed visually and interactively

### Header

- Compact conversation header with avatar, title, subtitle, and overflow actions.
- Friend chats show online/away/busy/offline state derived from the existing status drawables.
- Conference/group chats show active/offline participant counts in the subtitle.
- Search moved into a dismissible bar instead of permanently occupying the toolbar.
- Friend header opens friend info; conference/group header opens participant drawer.
- Existing call/video/group controls are preserved where supported.

### Message list

- Original low-contrast geometric wallpaper, light and dark variants.
- Incoming bubbles left-aligned, outgoing bubbles right-aligned.
- Rounded asymmetric bubbles, compact padding, and reduced row avatars.
- Inline metadata: short local time plus status icon.
- Date separators use Today/Yesterday/localized dates.
- Group/conference sender names are colored deterministically and shown only when needed by the existing holder logic.
- Selection uses a translucent row highlight instead of opaque gray bubble backgrounds.

### Delivery/status semantics

The redesign does not invent additional delivery guarantees:

- Friend text: pending, relay/push-assisted, or read receipt received.
- Conference/group text: sent marker only, because Tox conferences/groups do not provide the same per-recipient read receipts.
- NGC sync confirmation badges are preserved rather than replaced by fake read receipts.
- File transfers keep their existing progress/action controls; outgoing files show a conservative sent marker.

### Composer

- Rounded input surface with emoji, multiline text, attach/send, and audio-record controls.
- Friend/group composer morphs between attach+record and send states.
- Conference composer remains text-only, matching existing capabilities.
- Existing emoji popup, incognito keyboard flags, attachment picker, and audio recording flows are preserved.

### Contextual actions

- Long-press selection remains.
- Copy/delete/export/info/fullscreen remain.
- Single selected text messages now expose Reply, which inserts the existing quote format into the composer.
- Selection count is localized through a string resource.

## 9. Licensing and asset approach

The upstream project is distributed under GPLv2 or GPLv3, with translations under GPLv2-or-later. The redesign adds only original project resources and code:

- Original vector icons for search, overflow, emoji, attach, send, microphone, phone, video, group, person, pending, check, checks, and close.
- Original geometric wallpaper.
- Original palette and layout code.
- No third-party logos, proprietary artwork, copied screenshots, or fonts.

New Java code is marked `GPL-2.0-or-later`, compatible with the upstream licensing. The redesign uses original project assets and code.

## 10. Verification performed

Environment: Debian sandbox, OpenJDK 17, Android SDK 34, Gradle 8.7 wrapper.

Commands and results:

- `:app:mergeDebugResources` — passed.
- `:app:processDebugResources` — passed after fixing resource references and adding a legacy coordinator-style compatibility override.
- `:app:compileDebugJavaWithJavac` — attempted repeatedly under the sandbox's 1 GiB memory limit. Kotlin compilation and Jetifier-enabled builds were killed by the environment. A reduced-memory Java-only compile with Jetifier disabled reached `javac` and reported only four pre-existing AndroidX/Support Toolbar conversion errors in the original drawer code (`MainActivity`, `GroupMessageListActivity`, `ConferenceMessageListActivity`, `ConferenceAudioActivity`); those errors disappear when Jetifier is enabled, but Jetifier-enabled builds exceed the sandbox memory limit. No syntax or symbol errors remain in the redesigned chat classes in the final Java-only log.
- XML resources were parsed locally for well-formedness.
- `git diff --check` was used to catch whitespace errors.

Because the full Android build could not complete in this sandbox, the patch should still be built and smoke-tested on a normal development machine or CI runner with more memory.

## 11. Recommended smoke tests

1. Friend chat: send/receive text, read receipt, offline relay/push state, typing indicator, emoji, attachment, audio record, call/video buttons.
2. Conference chat: send/receive text, participant drawer, search, quote/reply, selection actions.
3. Group chat: send/receive text, sender names/colors, participant drawer, invite, media gallery, NGC sync badges, video panel, audio record.
4. File transfers: incoming/outgoing, pause/resume/cancel, preview, export/share, audio playback.
5. Paging: older/newer/load-all placeholders, fast scroll, scroll-to-bottom button.
6. Selection: long press, multi-select, reply/copy/delete/info/fullscreen, cancel, activity recreation.
7. Swipe-to-quote: incoming/outgoing text, disabled during selection, identity guard during list reload.
8. Light/dark mode, large font scale, RTL locale, small screen, keyboard/emoji popup.
9. Search: open/close, back button, query debounce, files-only filter.
10. Regression: message delivery, notifications, encrypted attachment loading, and database migrations.

## 12. Known limitations and follow-up work

- The full APK build was not completed in this sandbox due memory limits.
- New UI strings are English-only; they should be sent through the project's Weblate workflow.
- The redesign preserves existing file-transfer layouts rather than rewriting every media row from scratch.
- The old compact preference now maps to the same redesigned text-row structure; if users expect a density toggle, a future pass can add a compact metadata variant.
- The visual result is an original open-source chat UI.
