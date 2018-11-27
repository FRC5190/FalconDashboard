package org.ghrobotics.falcondashboard.generator.fragments

import javafx.beans.property.SimpleDoubleProperty
import org.ghrobotics.falcondashboard.createNumericalEntry
import org.ghrobotics.falcondashboard.generator.GeneratorView
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.units.degree
import org.ghrobotics.lib.mathematics.units.feet
import tornadofx.*

class WaypointFragment : Fragment() {
    override val root = vbox { }

    val x = SimpleDoubleProperty(0.0)
    val y = SimpleDoubleProperty(0.0)
    val a = SimpleDoubleProperty(0.0)

    init {
        with(root) {
            title = "Add Waypoint"

            paddingAll = 50

            createNumericalEntry("X", x)
            createNumericalEntry("Y", y)
            createNumericalEntry("Angle", a)

            button {
                text = "Add"
                prefWidth = 100.0
                action {
                    GeneratorView.waypoints.add(Pose2d(x.value.feet, y.value.feet, a.value.degree))
                    close()
                }
            }
        }
    }
}