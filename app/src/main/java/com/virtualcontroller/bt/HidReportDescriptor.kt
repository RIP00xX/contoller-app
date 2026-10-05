package com.virtualcontroller.bt

/**
 * Standard USB/Bluetooth HID Gamepad Report Descriptor.
 * Defines standard input layout:
 * - 4 Analog Joystick Axes (LX, LY, RX, RY): 8-bit unsigned (0-255, center 128)
 * - 2 Analog Triggers (L2, R2): 8-bit unsigned (0-255, rested 0)
 * - 1 D-Pad Hat Switch: 4-bit (0=Up, 1=UpRight, 2=Right, 3=DownRight, 4=Down, 5=DownLeft, 6=Left, 7=UpLeft, 8=Released)
 * - 16 Digital Buttons (Bitmask across 2 bytes):
 *   Bit 0: Button A / Cross
 *   Bit 1: Button B / Circle
 *   Bit 2: Button X / Square
 *   Bit 3: Button Y / Triangle
 *   Bit 4: L1 Bumper
 *   Bit 5: R1 Bumper
 *   Bit 6: L3 (Left Stick Press)
 *   Bit 7: R3 (Right Stick Press)
 *   Bit 8: Select / Back
 *   Bit 9: Start / Options
 *   Bit 10: System / Mode / PS / Xbox Button
 *   Bit 11-15: Extra custom actions
 */
object HidReportDescriptor {

    const val REPORT_ID_GAMEPAD = 1.toByte()

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
        0x09.toByte(), 0x35.toByte(), //   USAGE (Rz - Right Y)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x26.toByte(), 0xFF.toByte(), 0x00.toByte(), // LOGICAL_MAXIMUM (255)
        0x75.toByte(), 0x08.toByte(), //   REPORT_SIZE (8)
        0x95.toByte(), 0x04.toByte(), //   REPORT_COUNT (4)
        0x81.toByte(), 0x02.toByte(), //   INPUT (Data,Var,Abs)

        // --- 2 Analog Triggers (L2, R2) ---
        0x05.toByte(), 0x01.toByte(), //   USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x33.toByte(), //   USAGE (Rx - L2 Trigger)
        0x09.toByte(), 0x34.toByte(), //   USAGE (Ry - R2 Trigger)
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
        0x81.toByte(), 0x02.toByte(), //   INPUT (Data,Var,Abs)

        // D-Pad Padding (4 bits)
        0x75.toByte(), 0x04.toByte(), //   REPORT_SIZE (4)
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x81.toByte(), 0x03.toByte(), //   INPUT (Cnst,Var,Abs)

        // --- 16 Digital Buttons ---
        0x05.toByte(), 0x09.toByte(), //   USAGE_PAGE (Button)
        0x19.toByte(), 0x01.toByte(), //   USAGE_MINIMUM (Button 1)
        0x29.toByte(), 0x10.toByte(), //   USAGE_MAXIMUM (Button 16)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(), //   LOGICAL_MAXIMUM (1)
        0x75.toByte(), 0x01.toByte(), //   REPORT_SIZE (1)
        0x95.toByte(), 0x10.toByte(), //   REPORT_COUNT (16)
        0x81.toByte(), 0x02.toByte(), //   INPUT (Data,Var,Abs)

        0xC0.toByte()                 // END_COLLECTION
    )
}
