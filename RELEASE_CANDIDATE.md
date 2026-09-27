FoodWell v90 — Release Candidate
================================

This package is the Release Candidate source package for the FoodWell Android project.

Release checklist:
- Android UI polish: included from v89
- Offline/error recovery: included from preceding project versions
- Permission & privacy flow: included from preceding project versions
- Health Connect integration: SOURCE STRUCTURE ONLY; requires Android build and device testing
- Background sync: SOURCE STRUCTURE ONLY; requires Android build and device testing
- Smart Watch integration: requires compatible device/app and Health Connect data
- APK: NOT included in this source package

Required real-device validation:
1. Build the Android project with a supported Android toolchain.
2. Install on an Android device.
3. Verify Health Connect availability and permissions.
4. Test Steps, Heart Rate, Exercise, and Sleep where supported/authorized.
5. Test offline -> online recovery and duplicate prevention.
6. Test Android back/navigation, safe areas, and notifications.
7. Record and fix device-specific errors before production release.

Important:
This is a source Release Candidate, not a verified production APK.
