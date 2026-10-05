# Virtual Game Controller App — Complete Implementation Walkthrough

A high-performance Android application built with **Kotlin** and **Jetpack Compose** that transforms a smartphone—and specifically foldable devices like the **Galaxy Z Fold 6**—into a customizable, zero-driver Bluetooth HID Gamepad for Windows PCs, laptops, and consoles.

---

## 🌟 Key Architecture & Flagship Features

### 1. Galaxy Z Fold 6 Dual-Screen & Cover Display Activation (`com.virtualcontroller.foldable`)
- **Cover Display Backlight & Wakeup**: Fixed Samsung One UI 6+ behavior where the outer display turns off/suspends when unfolded by incorporating:
  - System Alert Window permission (`SYSTEM_ALERT_WINDOW`).
  - Window type `TYPE_APPLICATION_OVERLAY`.
  - Backlight & Lockscreen flags: `FLAG_KEEP_SCREEN_ON | FLAG_TURN_SCREEN_ON | FLAG_SHOW_WHEN_LOCKED | FLAG_ALLOW_LOCK_WHILE_SCREEN_ON`.
  - Context display binding: `context.createDisplayContext(outerDisplay)`.
- **Concurrent Touch Window**: Allows active touch input on rear L1/R1 bumpers and L2/R2 analog triggers on the cover screen while holding the unfolded device like a physical gamepad.
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
- **Low-Latency Packet Serializer**: Converts touch inputs in real time into standard 9-byte HID reports containing 4 analog joystick axes, 2 analog trigger sliders, 8-directional D-Pad hat switch, and 16 digital buttons.
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
- **Latest Commit**: `88f14d6`
- **Actions Workflow Run**: [Run #37338831582](https://github.com/RIP00xX/contoller-app/actions/runs/37338831582)
- **Generated Artifact**: `VirtualController-Debug-APK` (~7.9 MB)

---

## 📲 How to Install & Test

1. Download **`VirtualController-Debug-APK`** from [GitHub Actions Run #37338831582](https://github.com/RIP00xX/contoller-app/actions/runs/37338831582).
2. Extract the `.zip` archive and install `app-debug.apk` on your Galaxy Z Fold 6.
3. Launch the app and grant **Display over other apps** (`SYSTEM_ALERT_WINDOW`) when prompted.
4. Unfold your Z Fold 6. The main controller interface will appear on the inner main screen, while the rear triggers (L1/R1/L2/R2) light up and operate on the outer cover screen!
