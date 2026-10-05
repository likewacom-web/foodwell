# FoodWell v91 — APK Build Setup

This package prepares the FoodWell Release Candidate for a real Android APK build.

## Important
This is a source/build-setup package, not an APK. A real APK must be built with an Android build environment and then installed on an Android device.

## Build checklist
1. Open the Android project in Android Studio.
2. Verify the Android SDK and Gradle/Android Gradle Plugin versions required by the project.
3. Confirm the application ID and version code/name.
4. Sync Gradle dependencies.
5. Build a debug APK first.
6. Install the debug APK on a physical Android device.
7. Test the WebView/local app shell.
8. Test Native Bridge calls.
9. Only after debug testing passes, create a signed release APK.

## Real-device acceptance tests
- App launches without crashing.
- Android Back works.
- Local data survives app restart.
- Offline mode works.
- Sync retry works.
- Permission screens do not request access automatically.
- Background sync does not duplicate records.

## Release rule
Do not label the result production-ready until the APK has been installed and the above tests have been performed on a real Android device.
