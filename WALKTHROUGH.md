# Implementation walkthrough

Virtual Game Controller is a standalone Kotlin/Compose Android Gradle project. See [README.md](README.md) for setup and compatibility.

## Bluetooth input

[BluetoothHidManager](app/src/main/java/com/virtualcontroller/bt/BluetoothHidManager.kt) registers with Android's Bluetooth HID Device API, tracks the connected host and answers report requests. [BluetoothHidService](app/src/main/java/com/virtualcontroller/bt/BluetoothHidService.kt) owns the manager.

[GamepadReport](app/src/main/java/com/virtualcontroller/bt/GamepadReport.kt) serializes seven payload bytes: four unsigned stick axes followed by three bytes of digital buttons/padding. The report ID is separate. [HidReportDescriptor](app/src/main/java/com/virtualcontroller/bt/HidReportDescriptor.kt) declares the matching fields.

Logical button IDs remain stable in saved layouts; serialization translates them to host slots:

| Zero-based host slot | Control |
| --- | --- |
| 0–3 | Cross/A, Circle/B, Square/X, Triangle/Y |
| 4–5 | L1, R1 |
| 6–7 | L2, R2 |
| 8–9 | Create/Select, Options/Start |
| 10–11 | L3, R3 |
| 12–15 | D-pad Up, Down, Left, Right |
| 16–18 | System, Touchpad click, Share |

Triggers and D-pad directions each have one digital representation. Removing the previous extra trigger/hat axes resolved false Up and L2-to-Left tester inputs. Stick HID usages occupy Windows Chromium raw axis slots 0–3.

[ControllerInputState](app/src/main/java/com/virtualcontroller/bt/ControllerInputState.kt) synchronizes updates from both displays so an outer press does not overwrite an inner stick or button.

## Foldable display sessions

[DualScreenManager](app/src/main/java/com/virtualcontroller/foldable/DualScreenManager.kt) queries WindowManager's `OPERATION_PRESENT_ON_AREA` capability and requests a rear-facing session when the user enables outer controls. Activation and consent belong to the system.

[FoldStateTracker](app/src/main/java/com/virtualcontroller/foldable/FoldStateTracker.kt) observes posture. [OuterScreenPresentation](app/src/main/java/com/virtualcontroller/foldable/OuterScreenPresentation.kt) supports eligible presentation displays and supplies Compose lifecycle ownership. An arbitrary display is not assumed to be the cover panel.

Normal unfolded Fold 6 operation left the cover disabled in the original implementation. The supported concurrent-display session activates both panels; overlay/wake flags alone were insufficient. See [device findings](docs/FOLD6_TESTING.md).

## Geometry and touch controls

[OuterControllerScreen](app/src/main/java/com/virtualcontroller/ui/controller/OuterControllerScreen.kt) uses its measured viewport, safe drawing insets and normalized positions. Size uses the shorter edge and a 64–144 dp range, further bounded by the viewport. Regular buttons send immediate press/release callbacks.

[LayoutEditorScreen](app/src/main/java/com/virtualcontroller/ui/editor/LayoutEditorScreen.kt) provides separate inner/outer canvases. The outer preview scales the same sizing rule to its bounds. Drag calculations use the latest element position.

[VirtualDPad](app/src/main/java/com/virtualcontroller/ui/components/VirtualDPad.kt) responds on pointer-down and releases on pointer-up/cancellation. [VirtualJoystick](app/src/main/java/com/virtualcontroller/ui/components/VirtualJoystick.kt) maps current touch position within a clamped circle and recenters on release. [ControllerTouchMath](app/src/main/java/com/virtualcontroller/ui/components/ControllerTouchMath.kt) provides testable coordinate calculations.

## Touchpad and persistence

[VirtualTouchpad](app/src/main/java/com/virtualcontroller/ui/components/VirtualTouchpad.kt) sends single-contact absolute coordinates through [TouchpadReport](app/src/main/java/com/virtualcontroller/bt/TouchpadReport.kt), independently of gamepad input. Its Click strip uses host index 17. Movement uses a generic digitizer report, not a Sony protocol or mouse report; host/game support requires separate verification.

[ProfileRepository](app/src/main/java/com/virtualcontroller/data/ProfileRepository.kt) persists profiles and active selection using DataStore/Gson. [ProfileOverrides](app/src/main/java/com/virtualcontroller/data/ProfileOverrides.kt) merges by ID with saved versions winning, fixing preset edits being hidden after saving/restarting.

## Validation

CI runs 16 regression tests, lint and APK assembly. Live Fold 6/Windows checks confirmed the cover session, right-stick movement, D-pad release, isolated L2 press/release, touchpad click and saved-layout persistence. See [Fold 6 testing](docs/FOLD6_TESTING.md) for procedures and limits.
