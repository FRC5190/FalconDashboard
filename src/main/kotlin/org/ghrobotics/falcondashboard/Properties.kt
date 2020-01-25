package org.ghrobotics.falcondashboard

import org.ghrobotics.lib.mathematics.units.feet
import org.ghrobotics.lib.mathematics.units.inMeters
import org.ghrobotics.lib.mathematics.units.inches

/**
 * Stores general properties for robots and vision targets.
 */
object Properties {
    // Robot Sizes
    val kRobotLength = 31.inches
    val kRobotWidth = 29.inches

    // Target Sizes
    val kTargetWidth = 14.5.inches
    val kTargetThickness = kTargetWidth / 2

    val kFieldWidth = 54.0.feet.inMeters()
    val kFieldHeight = 27.0.feet.inMeters()
}