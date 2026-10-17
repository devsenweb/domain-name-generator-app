# Mad Minute (Android)

Ivana's 30-fact multiplication drill. 1- or 2-minute timer, "tough ones" mode for 6s–9s,
missed facts come back more often, score history saved on the phone.

## Self-updating
On every launch the app downloads the latest page from:

https://raw.githubusercontent.com/devsenweb/domain-name-generator-app/main/mad-minute/app/src/main/assets/index.html

Edit `mad-minute/app/src/main/assets/index.html` and get it onto `main`. The phone picks it up
the next time the app opens (right away if Ivana is on the home screen). No reinstall.

## Get the APK
Every push that changes the app code runs the "Build Mad Minute APK" workflow, which attaches the
APK to the `mad-minute-latest` release:

https://github.com/devsenweb/domain-name-generator-app/releases/download/mad-minute-latest/MadMinute.apk

Open that on the Android phone, tap the download, and allow "Install unknown apps" when asked.

Pushing only `index.html` changes does not rebuild the APK (no need). Code changes rebuild it,
and the fixed signing key (`app/madminute.keystore`) lets the new APK install over the old one.
