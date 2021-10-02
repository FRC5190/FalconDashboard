package org.ghrobotics.falcondashboard

import org.ghrobotics.lib.mathematics.units.derived.degrees
import org.ghrobotics.lib.mathematics.units.derived.radians
import org.ghrobotics.lib.mathematics.units.meters
import java.lang.Math

/**
 * Stores general properties for robots and vision targets.
 */
object Properties {
    // Robot Sizes
    val kRobotLength = 0.78.meters
    val kRobotWidth = 0.71.meters

    // Lookahead
    val lookahead = 0.1

    // Turret offset
    val kTurretOffsetX = 0.3.meters

    // Turret Size
    val kTurretSize = 0.2.meters

    // Camera Specs
    val kCameraFOV = Math.toRadians(40.0).radians
    val kCameraSight = 6.meters

    // Target Sizes
    val kTargetWidth = 0.36.meters
    val kTargetThickness = kTargetWidth / 2
}