<img src="https://raw.githubusercontent.com/zoff99/ToxAndroidRefImpl/zoff99/dev003/doc/mockup_004a.png" width="99%">

# Tox Reference Implementation for Android [TRIfA]

~~This is not a Reference Client, it's c-toxcore for Android.~~<br>
This is now also a Tox Client for Android.

<a href="https://f-droid.org/app/com.zoffcc.applications.trifa"><img src="https://raw.githubusercontent.com/zoff99/ToxAndroidRefImpl/zoff99/dev003/images/f-droid.png" width="200"></a>
<a href="https://play.google.com/store/apps/details?id=com.zoffcc.applications.trifa"><img src="https://raw.githubusercontent.com/zoff99/ToxAndroidRefImpl/zoff99/dev003/images/playstore.png" width="200"></a>
<a href="https://github.com/zoff99/ToxAndroidRefImpl/releases/latest/download/play.trifa.apk"><img src="https://raw.githubusercontent.com/zoff99/ToxAndroidRefImpl/zoff99/dev003/images/on_github.png" width="200"></a>

&nbsp;&nbsp;&nbsp;&nbsp;Looking for TRIfA Desktop? [follow me](https://github.com/zoff99/java_toxclient_example)

Status
=
[![CircleCI](https://circleci.com/gh/zoff99/ToxAndroidRefImpl/tree/zoff99%2Fdev003.png?style=badge)](https://circleci.com/gh/zoff99/ToxAndroidRefImpl/tree/zoff99%2Fdev003)
[![Android CI](https://github.com/zoff99/ToxAndroidRefImpl/workflows/Android%20CI/badge.svg)](https://github.com/zoff99/ToxAndroidRefImpl/actions?query=workflow%3A%22Android+CI%22)
[![Release](https://jitpack.io/v/zoff99/pkgs_ToxAndroidRefImpl.svg)](https://jitpack.io/#zoff99/pkgs_ToxAndroidRefImpl)
[![Last release](https://img.shields.io/github/v/release/zoff99/ToxAndroidRefImpl)](https://github.com/zoff99/ToxAndroidRefImpl/releases/latest)
[![Translations](https://hosted.weblate.org/widgets/trifa-a-tox-client-for-android/-/svg-badge.svg)](https://hosted.weblate.org/engage/trifa-a-tox-client-for-android/)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.en.html)
[![Liberapay](https://img.shields.io/liberapay/goal/zoff.svg?logo=liberapay)](https://liberapay.com/zoff/donate)

Help Translate the App in your Language
=
Use Weblate:
https://hosted.weblate.org/engage/trifa-a-tox-client-for-android/

Push Notification
=
See [PUSH_NOTIFICATION.md](./PUSH_NOTIFICATION.md)

Offline Messages
=
See [OFFLINE_MESSAGES.md](./OFFLINE_MESSAGES.md)

Does TRIfA connect to any third party servers?
=
TRIfA (exlcuding toxcore) uses the Google Firebase service and a third party server to deliver push notifications to other tox mobile users when they are offline. This makes it possible for Mobile devices to go into sleep mode and save battery and network bandwidth when there is no activity. Rest assured that the push notification does not contain any data, the request that comes from TRIfA includes only the FCM token of your contact(s). No ToxID, name or message data is transfered in the process.

Get in touch
=
* <a href="https://matrix.to/#/#trifa:matrix.org">Join discussion on Matrix</a><br>
* <a href="https://kiwiirc.com/nextclient/irc.libera.chat/toktok">Join Tox IRC Channel</a><br>

Import/Export
=
See [import-export.md](./import-export.md)

Compile in Android Studio
=
**Open an existing Project:**<br>
<img src="https://github.com/zoff99/ToxAndroidRefImpl/blob/zoff99/dev003/image.png" width="400">

**and select the "android-refimpl-app" subdir:**<br>
<img src="https://github.com/zoff99/ToxAndroidRefImpl/blob/zoff99/dev003/image1.png" width="400">

<br><br>

Development Snapshot Version (Android)
=
the latest Development Snapshot can be downloaded from CircleCI, [here](https://circleci.com/api/v1.1/project/github/zoff99/ToxAndroidRefImpl/latest/artifacts/0/artefacts/ToxAndroidRefImpl.apk?filter=successful&branch=zoff99%2Fdev003)

<b>Warning: CircleCI does not host the artifacts forever (anymore) so this link may or may not work</b>

<!--
<img src="https://circleci.com/api/v1/project/zoff99/ToxAndroidRefImpl/latest/artifacts/0/$CIRCLE_ARTIFACTS/capture_app_running_2.png?filter=successful&branch=zoff99%2Fdev003" width="148">
-->

## License (Project is dual licensed, you can choose the license)

TRIfA is distributed under the terms of the GNU General Public License (version 2) or the GNU General Public License (version 3).
You may use TRIfA according to either of these licenses as is most appropriate for your project on a case-by-case basis.

See LICENSE-zzGPLv2 and LICENSE-GPLv3 for details.

Translations are under the terms of GNU General Public License (version 2 or later).

See https://hosted.weblate.org/projects/trifa-a-tox-client-for-android/android-application/#information
and https://hosted.weblate.org/projects/trifa-a-tox-client-for-android/f-droid-texts/#information
