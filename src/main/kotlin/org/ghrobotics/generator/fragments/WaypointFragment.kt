package org.ghrobotics.generator.fragments

import tornadofx.Fragment
import tornadofx.textfield
import tornadofx.vbox

class WaypointFragment : Fragment() {
    override val root = vbox { }

    init {
        with(root) {
            textfield { text = "X Coordinate" }
            textfield { text = "Y Coordinate" }
            textfield { text = "Angle in Degrees" }
        }
    }

}