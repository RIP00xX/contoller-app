# Virtual Game Controller

[![Android build](https://github.com/RIP00xX/contoller-app/actions/workflows/build-apk.yml/badge.svg)](https://github.com/RIP00xX/contoller-app/actions/workflows/build-apk.yml)

A Kotlin and Jetpack Compose Android app that turns a phone into a Bluetooth HID gamepad. On supported foldables, the inner screen provides sticks and face buttons while the cover screen provides L1, L2, R1 and R2.

The dual-screen implementation has been tested on a **Samsung Galaxy Z Fold 6** connected to a **Windows PC**. No companion PC app is required for hosts and games that accept generic Bluetooth HID controllers.

## Features

- Simultaneous inner/cover-screen controls through a system-approved rear-display session.
- Two analog sticks, stick clicks, face buttons, digital D-pad, shoulder buttons and system buttons.
- Ordinary hold/release L2/R2 buttons, with no pressure slider.
- PlayStation, Xbox and racing layout presets.
- Separate inner/outer layout editor with drag positioning, size, opacity and haptic settings.
- Saved preset overrides and active-profile selection stored locally with DataStore.
- Cover buttons can grow up to **144 dp**, within the viewport and the editor's 50% size scale limit. Preview and actual controls use the same sizing rule.
- Central touch surface and a separate touchpad Click strip in the PlayStation and Xbox presets.
- Shared input state so controls on both screens work together.

## Download the APK

1. Open [GitHub Actions](https://github.com/RIP00xX/contoller-app/actions/workflows/build-apk.yml).
2. Select the latest successful build for `main`.
3. Download **VirtualController-Debug-APK** from its artifacts. GitHub may require sign-in.
4. Extract the archive and install `app-debug.apk` on the phone.

These are development APKs. GitHub runners can generate different debug signing keys between builds, so Android may reject an in-place update. Reinstalling removes app data; preserve layouts before doing so. For consistent distributed updates, configure a persistent signing key outside this repository.

## Use the controller

1. Turn on Bluetooth, open the app and grant its Bluetooth/Nearby devices permissions.
2. Pair the phone with the host through Bluetooth settings while the app is running.
3. Choose a controller layout using the profile selector.
4. Unfold a supported foldable and tap **Enable outer controls** when the status reports availability. Accept the system display-session dialog if shown.
5. Verify inputs using the host's controller settings or a [browser gamepad tester](https://webcammictest.com/gamepad/), then configure the game's bindings if needed.

The app does not request display-overlay permission. Backgrounding it ends the cover session and releases inputs. Re-enable outer controls after returning if needed.

### Customize and save

Open the layout editor and select the **inner** or **outer** canvas. Select a control, drag it into position, and adjust **Size Scale**, opacity or haptic intensity. Save when finished.

To enlarge cover buttons, increase each outer button's **Size Scale**. Size uses the cover view's shorter edge rather than the inner display's width. Positions are clamped to the canvas. Saved edits replace their original preset on loading, so switching profiles and reopening the app retain the edit.

### Compatibility

- Requires Android 10 or later and working Bluetooth HID Device support.
- Dual-screen mode requires a supported rear-display presentation capability. Device and firmware support vary; unfolding alone does not activate both displays.
- This is a **generic HID gamepad**, not an XInput device or a Sony protocol emulator. Automatic bindings and game/console support vary. Console compatibility has not been established.
- L2/R2 and D-pad directions are digital buttons. Trigger pressure and a separate POV/hat axis are not advertised.
- Touchpad movement uses a separate absolute HID digitizer report. Ordinary games/testers may ignore those coordinates. The Click strip independently sends gamepad button 18 (zero-based index 17).
- Rumble output is not implemented.

## Build from source

Requirements: **JDK 17**, **Android SDK Platform 34**, and Android Studio or a terminal with the SDK configured using `ANDROID_HOME` or an untracked `local.properties` file.

```sh
git clone https://github.com/RIP00xX/contoller-app.git
cd contoller-app
```

Open the root folder in Android Studio, sync Gradle and run the `app` configuration on a connected device. The repository includes the Gradle wrapper, version catalog, Android module, resources and tests.

Windows PowerShell:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

macOS/Linux:

```sh
chmod +x gradlew
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

### Verification and CI

[The workflow](.github/workflows/build-apk.yml) runs tests, Android lint and APK assembly on pushes and pull requests to `main`/`master`, and supports manual runs. It uploads the APK, Gradle log and verification reports. Lint errors fail the build.

The current suite contains 16 tests covering report serialization, button separation, D-pad diagonals/release, Windows raw stick ordering, descriptor sizes, shared input state, touchpad payloads, saved preset overrides and touch-coordinate calculations. See [Fold 6 testing](docs/FOLD6_TESTING.md) for live checks and diagnostics.

## Project structure

| Path | Purpose |
| --- | --- |
| `app/src/main/java/com/virtualcontroller/bt/` | HID registration, descriptors, reports and shared input state |
| `app/src/main/java/com/virtualcontroller/foldable/` | Fold posture and cover-display sessions |
| `app/src/main/java/com/virtualcontroller/ui/components/` | Touch buttons, sticks, D-pad, touchpad and steering controls |
| `app/src/main/java/com/virtualcontroller/ui/controller/` | Inner/outer controller screens |
| `app/src/main/java/com/virtualcontroller/ui/editor/` | Layout editor and screen previews |
| `app/src/main/java/com/virtualcontroller/data/` | DataStore profiles and preset override merging |
| `app/src/main/java/com/virtualcontroller/model/` | Profiles, controls and screen targets |
| `app/src/test/` | Unit and regression tests |
| `gradle/` | Wrapper and dependency version catalog |
| `.github/workflows/` | Verification and APK builds |

See [WALKTHROUGH.md](WALKTHROUGH.md) for implementation details.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Cover does not activate | Open the app unfolded, check its status and enable outer controls. Firmware support is required. |
| Old input mapping after updating | Forget the host pairing and pair again to clear its cached HID descriptor. |
| Browser sees the controller but a game ignores it | Check generic HID/DirectInput support and the game's bindings. XInput-only support is insufficient. |
| Layout edits disappear | Save and return to the same profile. Current builds load saved edits ahead of the preset. |
| Cover button stops growing | Size is bounded by 144 dp, the cover viewport and the slider's maximum. |
| Touchpad click works but movement does not | Check native generic HID touchpad support; Sony-specific handling is not emulated. |

Bug reports should include the phone model, Android/One UI version, app commit/build, host OS, game/tester, profile and reproduction steps. Remove pairing addresses and unrelated device logs before sharing.
