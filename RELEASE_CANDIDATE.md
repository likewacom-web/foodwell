FoodWell v90 — Release Candidate
================================

This package is the Release Candidate source package for the FoodWell Android project.

Release checklist:
- Android UI polish: included from v89
- Offline/error recovery: included from preceding project versions
- Permission & privacy flow: included from preceding project versions
- Background sync: SOURCE STRUCTURE ONLY; requires Android build and device testing
- APK: NOT included in this source package

Required real-device validation:
1. Build the Android project with a supported Android toolchain.
2. Install on an Android device.
3. Test offline -> online recovery and duplicate prevention.
4. Test Android back/navigation, safe areas, and notifications.
5. Record and fix device-specific errors before production release.

Important:
This is a source Release Candidate, not a verified production APK.
