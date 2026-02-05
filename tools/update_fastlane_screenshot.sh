#! /bin/sh
url_two_friend_list='https://github.com/zoff99/ToxAndroidRefImpl/releases/download/nightly/screen_shot_android_29_02.png'
url_one_on_one_chat_with_image='https://github.com/zoff99/ToxAndroidRefImpl/releases/download/nightly/screen_shot_android_29_03.png'
url_video_call_test='https://github.com/zoff99/ToxAndroidRefImpl/releases/download/nightly/screen_shot_android_29_11.png'
url_start_screen='https://github.com/zoff99/ToxAndroidRefImpl/releases/download/nightly/android_screen01_33.png'
url_friend_and_group_list='https://github.com/zoff99/ToxAndroidRefImpl/releases/download/nightly/screen_shot_android_29_04.png'
url_info_screen='https://github.com/zoff99/ToxAndroidRefImpl/releases/download/nightly/screen_shot_info_29_03.png'

url_promo_friend_list='https://github.com/zoff99/ToxAndroidRefImpl/releases/download/nightly/promo_29_02.png'
url_promo_chat_with_image='https://github.com/zoff99/ToxAndroidRefImpl/releases/download/nightly/promo_29_03.png'

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"

cd "$basedir"
wget "$url_promo_friend_list" -O ./fastlane/metadata/android/en-US/images/phoneScreenshots/010.png
wget "$url_promo_chat_with_image" -O ./fastlane/metadata/android/en-US/images/phoneScreenshots/011.png
# 101.png is handmade. leave it
wget "$url_start_screen" -O ./fastlane/metadata/android/en-US/images/phoneScreenshots/102.png
wget "$url_friend_and_group_list" -O ./fastlane/metadata/android/en-US/images/phoneScreenshots/103.png
# lets leave 104.png for now
wget "$url_two_friend_list" -O ./fastlane/metadata/android/en-US/images/phoneScreenshots/105.png
wget "$url_one_on_one_chat_with_image" -O ./fastlane/metadata/android/en-US/images/phoneScreenshots/106.png
wget "$url_video_call_test" -O ./fastlane/metadata/android/en-US/images/phoneScreenshots/107.png

wget "$url_info_screen" -O ./fastlane/metadata/android/en-US/images/phoneScreenshots/121.png



