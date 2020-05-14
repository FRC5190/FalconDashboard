package org.ghrobotics.falcondashboard.generator.fragments

import tornadofx.Fragment
import tornadofx.hbox
import tornadofx.paddingAll
import tornadofx.text

class InvalidTrajectoryFragment : Fragment() {
    override val root = hbox {}

    init {
        with(root) {
            title = "Invalid Trajectory Generated"
            paddingAll = 50

            text("You generated an invalid trajectory. Please ensure all your angles are correct.")
        }
    }
}