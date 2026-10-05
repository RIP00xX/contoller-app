# Virtual Controller

Bluetooth HID controller with separate inner and outer layouts.

## Dual-screen debugging

The inner screen contains sticks and face buttons; the preset outer layouts
contain L1/R1 and L2/R2. Unfolding alone does not imply dual-screen availability.
All four outer controls are regular hold/release buttons. L2/R2 send digital
trigger buttons in host slots 6/7 (zero-based); no pressure slider or trigger
axis is sent. D-pad taps respond immediately and send direction buttons in
slots 12..15, with two buttons for diagonals and none for release. There is no
extra hat axis, avoiding conflicting host/browser fallback interpretations.
Stick clicks use slots 10/11; the only gamepad axes are the four stick axes in
the first four Windows Chromium raw axis slots. This matches common browser
input ordering but does not change the controller into an XInput device or
guarantee every game's automatic mapping.
Their size uses the actual cover view's shorter edge, capped at 144 dp, with
safe drawing insets. Profile positions remain relative to that outer view in
both portrait and landscape.
The editor switches between inner and outer canvases; the outer preview uses
the dimensions measured from the active cover view (Fold 6 portrait dimensions
are used until the first dual-screen session).

The PlayStation and Xbox presets include a central touchpad on the inner screen.
Drag its surface to send single-contact absolute HID touchpad coordinates; hold
the separate **Click** strip to press gamepad button 18. Touch and click reports
are independent of sticks and shoulder inputs. No companion app is required
for the gamepad. This is a generic Bluetooth HID controller, not a DualShock or
DualSense protocol emulator: ordinary games may ignore touchpad coordinates.
Native touchpad handling depends on the paired host. No mouse report or automatic
mapping to a joystick is sent.

Because the Bluetooth descriptor and button order have changed, a host that
caches the previous descriptor may need to forget the old pairing and pair again
after installing this version.
The app checks Jetpack WindowManager's rear-facing `OPERATION_PRESENT_ON_AREA`
capability. Tap **Enable outer controls** when the status reports availability,
then accept the device's system dialog if one appears.

If no dual-screen capability is reported, a running `Presentation` display can
be used for testing (for example, an emulator's simulated secondary display).
An arbitrary secondary display ID is not treated as the cover screen. Overlay
permission and window flags cannot independently activate a firmware-disabled
cover panel. Samsung app-continuity settings address screen switching, not
simultaneous inner/outer operation.

Official API reference and sample:
https://developer.android.com/develop/adaptive-apps/guides/foldables/support-foldable-display-modes

### Verify on a Fold 6

1. Build with JDK 17 and Android SDK 34:
   `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` (Windows: `gradlew.bat`).
2. Install `app/build/outputs/apk/debug/app-debug.apk`, open the app unfolded,
   select the DualSense or Xbox preset, and record the status shown at the bottom.
3. If enabled, tap **Enable outer controls**. Verify L1/L2/R1/R2 are on the cover
   screen. Hold a stick on the inner screen while pressing both outer triggers;
   the paired host should receive all inputs together. Release L1 while holding
   a face button and verify that only L1 is released.
4. Fold/unfold, change profiles, enter/exit the editor, and background/return to
   the app. Re-enable outer controls after returning. Confirm there are no stuck
   controls or duplicate presentation windows.
5. Verify the outer controls fit in portrait and landscape, and outer L2/R2
   activate immediately on press without requiring a drag. Check the central
   Click strip as button 18 in the host's game-controller test panel, including
   while holding a stick and an outer trigger. Check touch movement separately
   on a host that natively supports HID touchpad input; gamepad recognition does
   not establish touchpad support.
6. Move or resize an element in either editor canvas and save. Change profiles,
   switch back, and restart the app; the edited preset must retain its changes.

With USB debugging enabled, capture read-only device diagnostics:

```powershell
adb devices -l
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.oneui
adb shell dumpsys display > fold6-display.txt
adb shell dumpsys device_state > fold6-device-state.txt
adb logcat -d -s DualScreenManager AndroidRuntime > fold6-controller-log.txt
```

Capture the dumps while unfolded with this app open. Also capture them while
Asphalt is actually displaying its controls on both panels, if available on the
same phone. This helps determine whether the game uses an OEM-specific display
path. The log includes device/firmware, visible display IDs, display states and
flags, rear-area capability, and any session failure.

The patch fixes missing posture collection, Compose lifecycle ownership,
shared input reports, and display selection. Panel activation from the patched
app remains unverified until its APK is built and tested on the phone.

### Findings from the connected Fold 6

On 2026-10-05, the connected SM-F956U1 reported Android 16, One UI property
`80000`, and firmware `F956U1UES3CZC1`.

- In normal unfolded state 3 (`OPENED`), the inner panel is ON. Logical display
  1 is the cover panel (968 x 2376), marked `FLAG_PRESENTATION` but disabled and
  OFF. The installed original app logged `Total displays: 1, Presentation
  displays: 0`, followed by `No secondary display available for dual-screen mode`.
- The firmware exposes state 4 (`CONCURRENT_INNER_DEFAULT`) and maps
  `config_deviceStateConcurrentRearDisplay` to 4.
- A temporary ADB request for state 4 made both logical displays enabled and ON;
  the cover display also acquired `FLAG_REAR_DISPLAY`. The request was reset
  immediately, and the phone returned to state 3. This demonstrates panel
  activation through the system mode; it does not verify the patched app's
  session, touch input, or HID output.
- The OEM WindowManager extension contains `WindowAreaComponentImpl` and rear
  display presentation session classes/methods. Actual capability and consent
  behavior must still be checked from the patched APK.

The original implementation did not request a dual-screen device session, so
its display enumeration could not find the disabled cover panel. The supported
WindowAreaController session is the intended replacement; no permanent ADB
override is part of the app.
