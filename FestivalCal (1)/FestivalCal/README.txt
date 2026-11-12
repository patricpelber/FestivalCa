Festival Cal - open this folder in Android Studio, let Gradle sync, then Build > Build APK(s).
Press WIDGET STYLE in the app to customise the widget (themes, colors, opacity, corners, size, alignment, formats).
Release build (R8-shrunk) is a few hundred KB. Long-press home screen > Widgets > Festival Cal.
Press CALIBRATE in the app to download festival dates (internet needed once; saved offline after).
Feed URL: Calibrator.kt (FEED). Built-in table in Festivals.kt is the fallback.

GitHub build: push this folder to a repo > Actions tab > "Build APK" > download the FestivalCal-apk artifact.

No Android Studio? Upload this folder's contents to a GitHub repo. The Actions tab builds the APK
automatically (.github/workflows/build.yml). Open the finished run > Artifacts > FestivalCal-apk.
