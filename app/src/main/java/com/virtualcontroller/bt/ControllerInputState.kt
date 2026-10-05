package com.virtualcontroller.bt

/** Both display callbacks update the same report so simultaneous inputs survive. */
class ControllerInputState {
    private var report = GamepadReport()

    @Synchronized
    fun update(updater: (GamepadReport) -> GamepadReport): GamepadReport {
        report = updater(report)
        return report
    }
}
