package com.virtualcontroller.bt

/**
 * Standard USB/Bluetooth HID Gamepad Report Descriptor.
 * Defines standard input layout:
 * - 4 Analog Joystick Axes (LX, LY, RX, RY): 8-bit unsigned (0-255, center 128)
 * - 2 Analog Triggers (L2, R2): 8-bit unsigned (0-255, rested 0)
 * - 1 D-Pad Hat Switch: 4-bit (0=Up, 1=UpRight, 2=Right, 3=DownRight, 4=Down, 5=DownLeft, 6=Left, 7=UpLeft, 8=Released)
 * - 19 Digital Buttons (plus padding across 3 bytes):
 *   Bit 0: Button A / Cross
 *   Bit 1: Button B / Circle
 *   Bit 2: Button X / Square
 *   Bit 3: Button Y / Triangle
 *   Bit 4: L1 Bumper
 *   Bit 5: R1 Bumper
 *   Bit 6: L2
 *   Bit 7: R2
 *   Bit 8: Select / Back
 *   Bit 9: Start / Options
 *   Bit 10-11: L3/R3
 *   Bit 12-15: D-pad Up/Down/Left/Right
 *   Bit 16-18: System, Touchpad Click, Share
 */
object HidReportDescriptor {

    const val REPORT_ID_GAMEPAD = 1.toByte()
    const val REPORT_ID_TOUCHPAD = 2.toByte()
    const val REPORT_ID_TOUCHPAD_CAPABILITIES = 3.toByte()

    val GAMEPAD_DESCRIPTOR = byteArrayOf(
        0x05.toByte(), 0x01.toByte(), // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x05.toByte(), // USAGE (Gamepad)
        0xA1.toByte(), 0x01.toByte(), // COLLECTION (Application)
        0x85.toByte(), REPORT_ID_GAMEPAD, //   REPORT_ID (1)

        // --- 4 Joystick Axes (LX, LY, RX, RY) ---
        0x05.toByte(), 0x01.toByte(), //   USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x30.toByte(), //   USAGE (X)
        0x09.toByte(), 0x31.toByte(), //   USAGE (Y)
        0x09.toByte(), 0x32.toByte(), //   USAGE (Z - Right X)
        0x09.toByte(), 0x33.toByte(), //   USAGE (Rx - Right Y)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x26.toByte(), 0xFF.toByte(), 0x00.toByte(), // LOGICAL_MAXIMUM (255)
        0x75.toByte(), 0x08.toByte(), //   REPORT_SIZE (8)
        0x95.toByte(), 0x04.toByte(), //   REPORT_COUNT (4)
        0x81.toByte(), 0x02.toByte(), //   INPUT (Data,Var,Abs)

        // --- 2 Analog Triggers (L2, R2) ---
        0x05.toByte(), 0x01.toByte(), //   USAGE_PAGE (Generic Desktop)
        // Windows Chromium indexes raw axes by usage minus X (0x30).
        // Keep the four stick axes in slots 0..3; triggers occupy slots 4..5.
        0x09.toByte(), 0x34.toByte(), //   USAGE (Ry - L2 Trigger)
        0x09.toByte(), 0x35.toByte(), //   USAGE (Rz - R2 Trigger)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x26.toByte(), 0xFF.toByte(), 0x00.toByte(), // LOGICAL_MAXIMUM (255)
        0x75.toByte(), 0x08.toByte(), //   REPORT_SIZE (8)
        0x95.toByte(), 0x02.toByte(), //   REPORT_COUNT (2)
        0x81.toByte(), 0x02.toByte(), //   INPUT (Data,Var,Abs)

        // --- D-Pad Hat Switch ---
        0x05.toByte(), 0x01.toByte(), //   USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x39.toByte(), //   USAGE (Hat switch)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x07.toByte(), //   LOGICAL_MAXIMUM (7)
        0x35.toByte(), 0x00.toByte(), //   PHYSICAL_MINIMUM (0)
        0x46.toByte(), 0x3B.toByte(), 0x01.toByte(), // PHYSICAL_MAXIMUM (315)
        0x65.toByte(), 0x14.toByte(), //   UNIT (English Rotation: Angular Degrees)
        0x75.toByte(), 0x04.toByte(), //   REPORT_SIZE (4)
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x81.toByte(), 0x42.toByte(), //   INPUT (Data,Var,Abs,Null State): 8 means released

        // D-Pad Padding (4 bits)
        0x75.toByte(), 0x04.toByte(), //   REPORT_SIZE (4)
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x81.toByte(), 0x03.toByte(), //   INPUT (Cnst,Var,Abs)

        // --- 19 Digital Buttons ---
        0x65.toByte(), 0x00.toByte(), //   UNIT (None)
        0x35.toByte(), 0x00.toByte(), //   PHYSICAL_MINIMUM (0)
        0x45.toByte(), 0x00.toByte(), //   PHYSICAL_MAXIMUM (0)
        0x05.toByte(), 0x09.toByte(), //   USAGE_PAGE (Button)
        0x19.toByte(), 0x01.toByte(), //   USAGE_MINIMUM (Button 1)
        0x29.toByte(), 0x13.toByte(), //   USAGE_MAXIMUM (Button 19)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(), //   LOGICAL_MAXIMUM (1)
        0x75.toByte(), 0x01.toByte(), //   REPORT_SIZE (1)
        0x95.toByte(), 0x13.toByte(), //   REPORT_COUNT (19)
        0x81.toByte(), 0x02.toByte(), //   INPUT (Data,Var,Abs)

        0x75.toByte(), 0x05.toByte(), //   REPORT_SIZE (5)
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x81.toByte(), 0x03.toByte(), //   INPUT (Constant padding)

        0xC0.toByte()                 // END_COLLECTION
    )

    private val TOUCHPAD_DESCRIPTOR = byteArrayOf(
        0x05, 0x0D, 0x09, 0x05, 0xA1.toByte(), 0x01, // Digitizer / Touch Pad application
        0x85.toByte(), REPORT_ID_TOUCHPAD,
        0x09, 0x22, 0xA1.toByte(), 0x02, // Finger logical collection
        0x65, 0x00, 0x35, 0x00, 0x45, 0x00, // Clear inherited units/physical range
        0x09, 0x42, 0x09, 0x32, 0x09, 0x47, // Tip switch, in range, confidence
        0x15, 0x00, 0x25, 0x01,
        0x75, 0x01, 0x95.toByte(), 0x03, 0x81.toByte(), 0x02,
        0x75, 0x05, 0x95.toByte(), 0x01, 0x81.toByte(), 0x03, // Padding
        0x09, 0x51, 0x15, 0x00, 0x25, 0x7F, // Contact identifier
        0x75, 0x08, 0x95.toByte(), 0x01, 0x81.toByte(), 0x02,
        0x05, 0x01, 0x09, 0x30, // Absolute X, 0..1919
        0x15, 0x00, 0x26, 0x7F, 0x07,
        0x75, 0x10, 0x95.toByte(), 0x01, 0x81.toByte(), 0x02,
        0x09, 0x31, 0x26, 0x37, 0x04, // Absolute Y, 0..1079
        0x81.toByte(), 0x02, 0xC0.toByte(),
        0x05, 0x0D, 0x09, 0x54, 0x15, 0x00, 0x25, 0x01, // Contact count
        0x75, 0x08, 0x95.toByte(), 0x01, 0x81.toByte(), 0x02,
        0x85.toByte(), REPORT_ID_TOUCHPAD_CAPABILITIES,
        0x09, 0x55, 0xB1.toByte(), 0x02, // Maximum contact count feature: 1
        0xC0.toByte()
    )

    val CONTROLLER_DESCRIPTOR = GAMEPAD_DESCRIPTOR + TOUCHPAD_DESCRIPTOR
}
