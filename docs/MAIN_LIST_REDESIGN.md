# Main list redesign notes

This commit restyles the main combined list shown by `FriendListFragment`:

- friend chats,
- legacy Tox conferences,
- NGC groups.

It does not change the Tox engine, database layer, notification service, or the
conversation screens redesigned in the previous commit.

## Visual goals

- Flat original conversation-list rows with circular avatars, bold names, muted
  preview/status text, unread badges, and subtle dividers.
- Consistent row treatment for friends, conferences, and groups.
- Light/dark palettes that match the conversation UI palette.
- Preserve existing swipe-to-pin/unpin, notification toggles, long-press menus,
  filtering, fast scroll, and selection behavior.

## Main files

- `MainListUI.java`: shared row decoration applied after legacy holder binding.
- `FriendlistAdapter.java`: calls the decoration pass and uses the new separator
  palette.
- `friend_list_entry.xml`, `friend_list_entry_compact.xml`: redesigned friend rows.
- `friend_list_conf_entry.xml`, `friend_list_conf_entry_compact.xml`: redesigned
  conference/group rows.
- `friend_list_layout.xml`: list canvas, fast-scroll colors, loading surface.
- `activity_main.xml`: toolbar/status-strip/search-field palette.
- `main_list_ui.xml`, `values-night/main_list_ui.xml`: original palette.
- `main_list_unread_badge.xml`, `main_list_search_bg.xml`: original shapes.

## Compatibility

All legacy view IDs remain present so existing holders, click listeners, swipe
callbacks, and notification toggles continue to work. Technical IP chips are
hidden in the list row; detailed connection information remains available from
the existing profile/info screens.

## License

New code is `GPL-2.0-or-later`. The upstream project remains available under its
existing GPLv2/GPLv3 terms.
