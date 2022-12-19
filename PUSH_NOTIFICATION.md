<img src="https://raw.githubusercontent.com/zoff99/ToxAndroidRefImpl/zoff99/dev003/doc/mockup_004a.png" width="99%">

### Setup PUSH Notification for TRIfA [using Google FCM]

* #### 1) Make sure you have [TRIfA](https://play.google.com/store/apps/details?id=com.zoffcc.applications.trifa) installed and running
* #### 2) Make sure you have [Google Play Services](https://play.google.com/store/apps/details?id=com.google.android.gms) installed and working
* #### 3) Install the Tox Push Message App:

<a href="https://play.google.com/store/apps/details?id=com.zoffcc.applications.pushmsg"><img src="https://raw.githubusercontent.com/zoff99/ToxAndroidRefImpl/zoff99/dev003/images/playstore.png" width="200"></a>
<a href="https://github.com/zoff99/tox_push_msg_app/releases/latest/download/play.pushmsg.apk"><img src="https://raw.githubusercontent.com/zoff99/ToxAndroidRefImpl/zoff99/dev003/images/on_github.png" width="200"></a>

* #### 4) Activate *"Use Push Service"* in TRIfA Settings

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/zoff99/ToxAndroidRefImpl/zoff99/dev003/images/sett_push.png" height="200">

* #### 5) Activate *"Battery Savings Mode"* in TRIfA Settings
* #### 6) Set *"Offline Time in Batterysavings mode"* to "*120 Minutes*" in TRIfA Settings

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/zoff99/ToxAndroidRefImpl/zoff99/dev003/images/sett_batt.png" height="200">

* #### 7) Start the Tox Push Message App, and see the FCM Token

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/zoff99/ToxAndroidRefImpl/zoff99/dev003/images/seetoken.png" height="100">

* #### 8) Now start TRIfA and confirm the new Token

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="https://raw.githubusercontent.com/zoff99/ToxAndroidRefImpl/zoff99/dev003/images/acktoken.png" height="200">

<br>

### Push Messages work with:
* TRIfA (Android)<br>
* <a href="https://github.com/Zoxcore/Antidote">Antidote (iPhone)</a><br>
* qTox(*) (Linux, Windows, MacOS currently only with a <a href="https://github.com/Zoxcore/qTox_enhanced/releases">patched Version of qTox</a>. <b>Use at your own risk!!</b>)<br>
