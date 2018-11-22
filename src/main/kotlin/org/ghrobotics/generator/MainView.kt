package org.ghrobotics.generator

import javafx.scene.Parent
import org.ghrobotics.generator.charts.PositionChart
import org.ghrobotics.generator.charts.VelocityChart
import org.ghrobotics.generator.tables.WaypointsTable
import org.ghrobotics.lib.mathematics.twodim.trajectory.DefaultTrajectoryGenerator
import org.ghrobotics.lib.mathematics.units.derivedunits.acceleration
import org.ghrobotics.lib.mathematics.units.derivedunits.velocity
import org.ghrobotics.lib.mathematics.units.feet
import org.ghrobotics.lib.mathematics.units.meter
import tornadofx.*

class MainView : View() {
    override val root: Parent = hbox { }

    init {
        Main.waypoints.onChange {
            val trajectory = DefaultTrajectoryGenerator.generateTrajectory(
                it.list,
                listOf(),
                0.0.meter.velocity,
                0.0.meter.velocity,
                10.feet.velocity,
                4.feet.acceleration,
                false
            )

            PositionChart.update(trajectory)
            VelocityChart.update(trajectory)
        }

        title = "FRC 5190 Trajectory Generator"
        with(root) {
            tabpane {
                tab("Position") {
                    add(PositionChart)
                    isClosable = false
                }
                tab("Velocity") {
                    add(VelocityChart)
                    isClosable = false
                }
            }
            vbox {
                paddingAll = 20.0
                maxWidth = 300.0
                add(WaypointsTable(Main.waypoints))

                button {
                    text = "Add Waypoint"
                    setOnMouseClicked {
                        println("XD")
                    }
                }
            }
        }
    }
}