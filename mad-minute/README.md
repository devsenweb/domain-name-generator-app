# Mad Minute

A times-table drill for Android: as many multiplication facts as you can in
30 seconds, 1 minute, or 2 minutes. Pick which tables to practice, see your
misses at the end, and replay just the ones you got wrong. Best scores and
recent games are saved on the phone.

It's a small Java app with a WebView. The whole UI is
`app/src/main/assets/index.html`.

## Download the APK

Every push that touches `mad-minute/` runs the **Mad Minute APK** GitHub
Actions workflow. It builds a signed APK and attaches it to the
`mad-minute-latest` release:

https://github.com/devsenweb/domain-name-generator-app/releases/download/mad-minute-latest/MadMinute.apk

## Install on an Android phone

1. Open the link above on the phone (Chrome is fine) and download `MadMinute.apk`.
2. Tap the downloaded file. If Android warns about installing unknown apps,
   tap **Settings** and turn on **Allow from this source** for Chrome (or
   your Files app), then go back and tap **Install**.
3. Open **Mad Minute** from the app drawer.

Newer APKs install over the old one and keep the saved scores, because every
build is signed with the same key (`app/madminute.keystore`).

## Updating without reinstalling

On every launch the app downloads the latest `index.html` from
`BuildConfig.UPDATE_URL`:

https://raw.githubusercontent.com/devsenweb/domain-name-generator-app/main/mad-minute/app/src/main/assets/index.html

So to change the app, edit `app/src/main/assets/index.html` and get it onto
`main`. The next time the app is opened it fetches the new page and shows a
"new version is ready" banner; tapping it switches over. If the phone is
offline, the app keeps using the last page it downloaded (or the one built
into the APK).

You only need a new APK when the Java code, icon, or app permissions change.

## Build locally

Needs JDK 17 and the Android SDK (platform 34):

    ./gradlew assembleRelease
    # -> app/build/outputs/apk/release/app-release.apk

## A note on the signing key

The keystore and its password (`madminute`) are committed so every build
installs over the previous one. This repo is public, so anyone could sign an
APK with this key. That only matters if someone gets you to install their
APK, so only install builds from the release link above.
