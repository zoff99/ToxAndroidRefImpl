# Conversation UI redesign notes

This patch redesigns only the conversation/message-list experience for:

- friend chats,
- legacy Tox conferences,
- NGC groups.

It does not replace the Tox engine, database layer, file-transfer engine, or notification service.

## Design goals

- Clean mobile chat layout: compact header, aligned bubbles, inline time/status, rounded composer.
- Consistent behavior across all three chat types.
- Preserve existing selection, swipe-to-quote, paging, search, attachments, audio recording, and call/video flows.
- Use only original open-source assets and code.
- Avoid implying delivery/read guarantees that Tox does not provide.

## Main files

- `ChatUI.java`: shared presentation helper.
- `activity_message_list.xml`, `activity_conference_message_list.xml`, `activity_group_message_list.xml`: conversation shells.
- `chat_header.xml`, `chat_composer.xml`, `chat_group_video.xml`: shared header/composer/video pieces.
- `message_list_entry_read*.xml`, `message_list_self_entry*.xml`: redesigned text rows.
- `chat_*.xml` drawables/colors: original palette, icons, wallpaper, and shapes.
- `ToolbarActionMode.java`: adds Reply for single selected text messages.
- `MessagelistAdapter.java`, `ConferenceMessagelistAdapter.java`, `GroupMessagelistAdapter.java`: apply the shared decoration pass after legacy binding.

## Asset provenance

All new icons and wallpaper are original vector/shape resources created for this patch. No third-party logos, screenshots, proprietary fonts, or copied artwork are included.

## License

New code is `GPL-2.0-or-later`. The upstream project remains available under its existing GPLv2/GPLv3 terms. See `LICENSE-zzGPLv2`, `LICENSE-GPLv3`, and the upstream README.

## Application ID

The Gradle `applicationId` is changed to `com.zoffcc.applications.trifama` so the redesigned build can be installed side-by-side with the upstream package.

To avoid install/runtime collisions, the content-provider authorities are now derived from `applicationId`:

- `${applicationId}.std_fileprovider`
- `${applicationId}.ext2_provider`
- `${applicationId}.ext1_fileprovider`

Java code uses the matching `TRIFAGlobals` authority constants, and `MY_PACKAGE_NAME` follows `BuildConfig.APPLICATION_ID`.
