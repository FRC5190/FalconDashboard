package org.ghrobotics.falcondashboard

import org.ghrobotics.lib.mathematics.units.derived.degrees
import org.ghrobotics.lib.mathematics.units.meters

/**
 * Stores general properties for robots and vision targets.
 */
object Properties {
    // Robot Sizes
    val kRobotLength = 0.78.meters
    val kRobotWidth = 0.71.meters

    // Turret offset
    val kTurretOffsetX = 0.3.meters

    // Turret Size
    val kTurretSize = 0.2.meters

    // Camera Specs
    val kCameraFOV = 60.degrees
    val kCameraSight = 3.meters

    // Target Sizes
    val kTargetWidth = 0.36.meters
    val kTargetThickness = kTargetWidth / 2
}