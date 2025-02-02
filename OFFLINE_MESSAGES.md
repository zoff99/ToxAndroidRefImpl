Offline Messages
=
To get offline messages for your TRIfA App, install ToxProxy on a Linux Box (or Windows system) at home and leave it running 0:00-24:00.<br>
### Installation instructions:

* install ToxProxy for Linux: [appimage binary](https://github.com/zoff99/ToxProxy/releases/download/nightly/ToxProxy_x86_64.AppImage)
* or install ToxProxy on Windows: [windows binary](https://github.com/zoff99/ToxProxy/releases/download/nightly/ToxProxy_win_nightly.exe)
* now run ToxProxy (**it will only write data to the current directory and below**):
```
dummy@dummy:/home/dummy$ ./ToxProxy_x86_64.AppImage
ToxProxy version: 0.99.xx
Connection Status changed to:Online via UDP
#############################################################
#############################################################

ToxID:827707DBFF41BEA803C9CF7D81C1CFC2007FA774E6DE24FF1B661259CB8891668EF63E91C06E

#############################################################
#############################################################
```
* open TRIfA on your phone and add this ToxID as Friend and set it as Relay:

<img height="300" src="https://raw.githubusercontent.com/zoff99/ToxAndroidRefImpl/zoff99/dev003/images/add_toxproxy.gif"></img><br>

* ToxProxy will show that your phone is set as master:
```
added master:71BC3623887FEFC1F76811F8C3291806873E1B66159D955DB129BAACFE33BE2D
```

* now install the Tox Notify Companion App: [APK App](https://github.com/zoff99/tox_push_msg_app/releases/latest/download/play.pushmsg.apk)

* sync Token to TRIfA, approve it in TRIfA and restart TRIfA:

<img height="300" src="https://raw.githubusercontent.com/zoff99/ToxAndroidRefImpl/zoff99/dev003/images/add_fcm.gif"></img><br>

* ToxProxy will show it has received the Token:
```
received token:XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx
saved token:XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx
```

* now in TRIfA goto ```settings``` and activate ```Battery Savings Mode```
* and set ```Offline Time in Batterysavings mode``` ```to 120 minutes```

<br>
