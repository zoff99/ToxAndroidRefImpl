#! /bin/bash

# -- to remove the previous files from the sdcard --
# adb shell rm /sdcard/audio_*.txt
# --------------------------------------------------

adb="$1"

"$adb" pull /sdcard/audio_rec_s.txt .
"$adb" pull /sdcard/audio_play_s.txt .
"$adb" pull /sdcard/audio_rec_d.txt .

"$adb" pull /sdcard/audio_rec_s_ts.txt .
"$adb" pull /sdcard/audio_play_s_ts.txt .


cat audio_play_s.txt|gnuplot -p -e "set title 'play_s' ; set datafile separator ','; set xtics rotate by 270 ;set format x '%.0f';plot '-' using 1:2 w l" &
cat audio_rec_s.txt|gnuplot -p -e "set title 'rec_s' ; set datafile separator ','; set xtics rotate by 270 ;set format x '%.0f';plot '-' using 1:2 w l" &
cat audio_rec_d.txt|gnuplot -p -e "set title 'rec_d' ; set datafile separator ','; set xtics rotate by 270 ;set format x '%.0f';plot '-' using 1:2 w l" &

awk '{print $0-s;s=$0}' audio_play_s_ts.txt | tail -10
awk '{print $0-s;s=$0}' audio_rec_s_ts.txt | tail -10
