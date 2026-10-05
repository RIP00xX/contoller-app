# Fold 6 testing and diagnostics

## Device evidence

Testing on October 5–6, 2026 used a Galaxy Z Fold 6 (SM-F956U1), Android 16, One UI property `80000`, firmware `F956U1UES3CZC1`, and a Windows PC.

The original app enumerated one active display while unfolded. Normal state 3 (`OPENED`) disabled the cover. A temporary ADB request for state 4 (`CONCURRENT_INNER_DEFAULT`) activated both panels and was immediately reset. The app now requests the supported WindowManager rear-presentation session; no permanent ADB override or overlay permission is used.

## Completed live checks

- Logs showed a connected HID host, active rear-presentation session and visible cover content.
- The original right-stick fault appeared as Y = −1.00 at rest. Corrected reports centered it; holding downward produced Y = +1.00.
- Trigger, stick-click and D-pad button slots were recognized.
- A saved preset edit survived reopening the editor. Backups/restores during debug APK replacement were verified with matching hashes.
- Extra trigger/hat axes caused ambiguous tester behavior despite a neutral phone D-pad report. Digital-only input cleared the idle Up indicator.
- Repeated L2 presses alternated Windows HID reports between L2-only and zero. Browser L2 indicators alternated pressed/released while every D-pad direction remained released.
- The touchpad Click strip registered. Movement support in games was not established.

The mapping build passed 16 tests, lint and assembly: [verified run](https://github.com/RIP00xX/contoller-app/actions/runs/37355455341). The subsequent size update raises the real/preview outer-button cap from 96 to 144 dp.

## Manual verification

1. Install a successful build, open unfolded and grant Bluetooth permissions. Re-pair if the descriptor changed.
2. Enable outer controls. Check fit and press/release for L1/L2/R1/R2.
3. Hold an inner stick/button while pressing outer controls. Releasing one control should release only that control.
4. Sweep both sticks and release to center. Tap D-pad directions/diagonals; releasing must clear every direction.
5. Repeatedly press L2 alone, then R2 alone. No D-pad direction should activate.
6. Test touchpad Click separately from movement, which requires compatible native handling.
7. Enlarge an outer button in the editor, save and confirm fit. Reopen, switch profiles and restart to check persistence.
8. Fold/unfold, background/return and rotate where supported. Re-enable the cover session as needed; check for stuck inputs or duplicate windows.

## Read-only ADB diagnostics

With USB debugging enabled:

```sh
adb devices -l
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.oneui
adb shell dumpsys display > fold6-display.txt
adb shell dumpsys device_state > fold6-device-state.txt
adb logcat -d -s DualScreenManager BluetoothHidManager AndroidRuntime > fold6-controller-log.txt
```

Include the app build, profile, orientation, posture, cover status and failing input. Review logs before sharing and remove unrelated device/pairing information. Other foldables and firmware need their own verification.
