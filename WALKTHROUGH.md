# Virtual Game Controller App — Complete Implementation Walkthrough

A high-performance Android application built with **Kotlin** and **Jetpack Compose** that transforms a smartphone—and specifically foldable devices like the **Galaxy Z Fold 6**—into a customizable, zero-driver Bluetooth HID Gamepad for Windows PCs, laptops, and consoles.

---

## 🌟 Key Architecture & Flagship Features

### 1. Galaxy Z Fold 6 Dual-Screen & Cover Display Activation (`com.virtualcontroller.foldable`)
- **Cover Display Activation**: Queries Jetpack WindowManager's rear-facing `OPERATION_PRESENT_ON_AREA` capability and requests a dual-screen session when the user taps **Enable outer controls**. Overlay permission and wake-up flags alone did not activate the disabled cover panel in the original app.
- **Device Evidence**: On a Fold 6 running Android 16 / One UI 8, the normal unfolded state exposes only the active inner display to the original app. A temporary ADB request for Samsung's `CONCURRENT_INNER_DEFAULT` state powered on both panels; normal behavior was restored immediately. This verifies hardware activation, not the patched APK's session or touch behavior.
- **Concurrent Input**: Inner and outer callbacks update one shared HID report, preserving joystick, face-button, bumper, and trigger inputs together. Compose display content receives explicit lifecycle owners, and fold posture is collected while the activity is started.
- **Outer Layout and Buttons**: Layout uses the cover view's measured constraints and safe drawing insets. Controls are sized from the shorter screen edge and capped at 96 dp. L1/R1/L2/R2 are regular hold/release buttons; outer L2/R2 send 255 when pressed and 0 on release without a pressure slider.
- **Central Touchpad**: PlayStation and Xbox presets include an inner-screen touch surface and a separate Click strip mapped to gamepad button 18. Movement uses a separate single-contact absolute HID touchpad report. It does not emulate Sony's controller protocol, and ordinary games may ignore its coordinates. Host support must be tested separately; the gamepad remains Bluetooth-only. Re-pair if the host caches the old HID descriptor.
- **Validation Status**: The patched APK still requires a successful build and device testing. See [README.md](README.md) for diagnostic findings and the test procedure.
- [DualScreenManager.kt](file:///d:/conntroller%20app/app/src/main/java/com/virtualcontroller/foldable/DualScreenManager.kt)
- [OuterScreenPresentation.kt](file:///d:/conntroller%20app/app/src/main/java/com/virtualcontroller/foldable/OuterScreenPresentation.kt)
- [FoldStateTracker.kt](file:///d:/conntroller%20app/app/src/main/java/com/virtualcontroller/foldable/FoldStateTracker.kt)

### 2. High-DPI UI Sizing & Alignment Engine (`com.virtualcontroller.ui`)
- **Density-Independent Unit Scaling**: Fixed raw pixel to `Dp` conversion bugs on high-DPI foldables (e.g., Z Fold 6 3.5x pixel density).
- **Exact Screen Percentage Mapping**: Replaced raw `widthPx.dp` with `with(LocalDensity.current) { widthPx.toDp() }` across `MainControllerScreen`, `LayoutEditorScreen`, and `OuterControllerScreen` to ensure buttons render at their exact intended positions without overlapping or clustering into giant elements.
- [MainControllerScreen.kt](file:///d:/conntroller%20app/app/src/main/java/com/virtualcontroller/ui/controller/MainControllerScreen.kt)
- [OuterControllerScreen.kt](file:///d:/conntroller%20app/app/src/main/java/com/virtualcontroller/ui/controller/OuterControllerScreen.kt)
- [LayoutEditorScreen.kt](file:///d:/conntroller%20app/app/src/main/java/com/virtualcontroller/ui/editor/LayoutEditorScreen.kt)

### 3. Native Bluetooth HID Emulation (`com.virtualcontroller.bt`)
- **Zero-Driver Connectivity**: Utilizes Android's `BluetoothHidDevice` API to connect directly to PCs/consoles as a hardware Bluetooth controller without needing host software.
- **Low-Latency Packet Serializer**: Converts touch inputs in real time into standard 7-byte HID reports containing four analog stick axes and 19 digital buttons, including L2/R2 and D-pad directions. Trigger and D-pad input have a single digital representation to avoid conflicting host mappings.
- [GamepadReport.kt](file:///d:/conntroller%20app/app/src/main/java/com/virtualcontroller/bt/GamepadReport.kt)
- [BluetoothHidManager.kt](file:///d:/conntroller%20app/app/src/main/java/com/virtualcontroller/bt/BluetoothHidManager.kt)
- [BluetoothHidService.kt](file:///d:/conntroller%20app/app/src/main/java/com/virtualcontroller/bt/BluetoothHidService.kt)

### 4. Interactive Drag-and-Drop Layout Editor (`com.virtualcontroller.ui.editor`)
- **Drag-and-Drop Positioning**: Reposition controls anywhere on the screen canvas, adjust sizes, adjust opacity (10%-100%), and tune haptic vibration strength.
- **Cross-Screen Target Toggle**: Easily reassign buttons between `INNER_SCREEN` and `OUTER_SCREEN`.
- **Profiles & Local Persistence**: Pre-loaded layouts (DualSense PS5, Xbox Wireless, Racing Wheel) saved locally via DataStore.

---

## 📦 Build & Download Info

- **GitHub Repository**: [RIP00xX/contoller-app](https://github.com/RIP00xX/contoller-app)
- **Updated Builds**: Select the successful [GitHub Actions run](https://github.com/RIP00xX/contoller-app/actions) for the dual-screen session fix. The workflow runs unit tests before building the APK.
- **Previous Build**: [Run #37338831582](https://github.com/RIP00xX/contoller-app/actions/runs/37338831582) predates this fix.
- **Generated Artifact**: `VirtualController-Debug-APK`.

---

## 📲 How to Install & Test

1. Download **`VirtualController-Debug-APK`** from the successful Actions run containing the dual-screen session fix.
2. Extract the `.zip` archive and install `app-debug.apk` on your Galaxy Z Fold 6.
3. Launch the app and grant Bluetooth permissions. Overlay permission is no longer requested.
4. Unfold your Z Fold 6, select a DualSense or Xbox preset, and check the cover-screen status. If available, tap **Enable outer controls** and accept the system dialog if shown. Verify the outer L1/R1/L2/R2 controls while holding a stick on the inner display. If unavailable or failed, capture the logs described in README.md; do not treat unfolding alone as proof that dual-screen mode is active.
