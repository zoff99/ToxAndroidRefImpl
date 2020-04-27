
Import a Tox Savefile from another Client into TRIfA
=

1) export the tox savefile from your other client (without password!)
2) put the file on your Android device exactly in this directory with this name (case sensitive!):
  ```
  /sdcard/Android/data/com.zoffcc.applications.trifa/files/vfs_export/I_WANT_TO_IMPORT_savedata.tox
  ```
3) start TRIfA and select:
  ```
  Settings -> Maintenance -> IM-port TOX SAVEDATA *UNSECURE*
  ```
4) this will overwrite your current TRIfA profile without any confirmation and close TRIfA
5) restart TRIfA, now your imported profile should be active
6) delete the unencrypted file from your device:
  ```
  /sdcard/Android/data/com.zoffcc.applications.trifa/files/vfs_export/I_WANT_TO_IMPORT_savedata.tox
  ```

Import a Tox Savefile from another Client into TRIfA with root on your Android Device
=
1) export the tox savefile from your other client (without password!)
2) kill TRIfA
3) put the save file directly into the data directory of TRIfA with root access:
    ```
    /data/user/0/com.zoffcc.applications.trifa/files/savedata.tox
    ```
4) change the uid and gid accordingly and also read/write access
5) restart TRIfA, now your imported profile should be active
