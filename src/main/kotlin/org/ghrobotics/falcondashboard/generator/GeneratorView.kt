package org.ghrobotics.falcondashboard.generator

import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.trajectory.constraint.CentripetalAccelerationConstraint
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import kfoenix.jfxbutton
import kfoenix.jfxcheckbox
import kfoenix.jfxtabpane
import kfoenix.jfxtextfield
import org.ghrobotics.falcondashboard.Settings.autoPathFinding
import org.ghrobotics.falcondashboard.Settings.endVelocity
import org.ghrobotics.falcondashboard.Settings.maxAcceleration
import org.ghrobotics.falcondashboard.Settings.maxCentripetalAcceleration
import org.ghrobotics.falcondashboard.Settings.maxVelocity
import org.ghrobotics.falcondashboard.Settings.name
import org.ghrobotics.falcondashboard.Settings.optimize
import org.ghrobotics.falcondashboard.Settings.reversed
import org.ghrobotics.falcondashboard.Settings.startVelocity
import org.ghrobotics.falcondashboard.createNumericalEntry
import org.ghrobotics.falcondashboard.generator.charts.PositionChart
import org.ghrobotics.falcondashboard.generator.charts.VelocityChart
import org.ghrobotics.falcondashboard.generator.fragments.CodeFragment
import org.ghrobotics.falcondashboard.generator.fragments.WaypointFragment
import org.ghrobotics.falcondashboard.generator.tables.WaypointsTable
import org.ghrobotics.lib.mathematics.epsilonEquals
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.trajectory.FalconTrajectoryGenerator
import org.ghrobotics.lib.mathematics.twodim.trajectory.optimization.PathFinder
import org.ghrobotics.lib.mathematics.units.derived.acceleration
import org.ghrobotics.lib.mathematics.units.derived.velocity
import org.ghrobotics.lib.mathematics.units.feet
import tornadofx.*

class GeneratorView : View() {

    override val root = hbox {
        stylesheets += resources["/GeneratorStyle.css"]
        vbox {
            style {
                paddingAll = 20.0
                maxWidth = 300.px
                spacing = 5.px
            }

            hbox {
                paddingAll = 5
                jfxtextfield {
                    bind(name)
                    prefWidth = 290.0
                }
            }

            jfxcheckbox {
                paddingAll = 5
                text = "Reversed"
                bind(reversed)
            }
            jfxcheckbox {
                paddingAll = 5
                text = "Optimize Curvature"
                bind(optimize)
            }
            jfxcheckbox {
                paddingAll = 5
                text = "Auto Path Finding (Experimental)"
                bind(autoPathFinding)
            }

            createNumericalEntry("Start Velocity (f/s)", startVelocity)
            createNumericalEntry("End Velocity (f/s)", endVelocity)
            createNumericalEntry("Max Velocity (f/s)", maxVelocity)
            createNumericalEntry("Max Acceleration (f/s/s)", maxAcceleration)
            createNumericalEntry("Max Centripetal Acceleration (f/s/s)", maxCentripetalAcceleration)

            jfxbutton {
                text = "Add Velocity Limit Constraint in Region"
                maxWidth = 290.0
            }

            this += WaypointsTable

            vbox {
                spacing = 5.0
                jfxbutton {
                    prefWidth = 290.0
                    text = "Add Waypoint"
                    action {
                        find<WaypointFragment>().openModal(stageStyle = StageStyle.UTILITY)
                    }
                }
                jfxbutton {
                    prefWidth = 290.0
                    text = "Remove Waypoint"
                    action {
                        WaypointsTable.removeSelectedItemIfPossible()
                    }
                }
                jfxbutton {
                    prefWidth = 290.0
                    text = "Generate"
                    action {
                        find<CodeFragment>().openModal(stageStyle = StageStyle.UTILITY)
                    }
                }
            }
        }
        jfxtabpane {
            maxWidth = Double.MAX_VALUE
            hgrow = Priority.ALWAYS
            style {
                backgroundColor = multi(Color.LIGHTGRAY)
            }
            tab("Position") {
                hbox {
                    alignment = Pos.CENTER_LEFT
                    add(PositionChart)
                }
                isClosable = false
            }
            tab("Velocity") {
                add(VelocityChart)
                isClosable = false
            }
        }
    }

    companion object {
        val waypoints = observableList(
            Pose2d(1.5.feet, 23.feet, Rotation2d()),
            Pose2d(11.5.feet, 23.feet, Rotation2d())
        )

        val trajectory = SimpleObjectProperty(
            FalconTrajectoryGenerator.generateTrajectory(
                waypoints = waypoints,
                constraints = listOf(CentripetalAccelerationConstraint(maxCentripetalAcceleration.value)),
                startVelocity = startVelocity.value.feet.velocity,
                endVelocity = endVelocity.value.feet.velocity,
                maxVelocity = maxVelocity.value.feet.velocity,
                maxAcceleration = maxAcceleration.value.feet.acceleration,
                reversed = reversed.value
            )
        )

        init {
            update()
            waypoints.onChange { update() }
            reversed.onChange { update() }
            optimize.onChange { update() }
            autoPathFinding.onChange { update() }

            startVelocity.onChange { update() }
            endVelocity.onChange { update() }
            maxVelocity.onChange { update() }
            maxAcceleration.onChange { update() }
            maxCentripetalAcceleration.onChange { update() }
        }

        @Synchronized
        private fun update() {
            if (startVelocity.value.isNaN() ||
                endVelocity.value.isNaN() ||
                maxVelocity.value epsilonEquals 0.0 ||
                maxAcceleration.value epsilonEquals 0.0 ||
                maxCentripetalAcceleration.value epsilonEquals 0.0
            ) return

            val wayPoints = if (autoPathFinding.value) {
                val pathFinder = PathFinder(
                    3.5.feet,
                    PathFinder.k2018CubesSwitch,
                    PathFinder.k2018LeftSwitch,
                    PathFinder.k2018Platform
                )
                waypoints.zipWithNext { start, end ->
                    kotlin.runCatching {
                        pathFinder.findPath(start, end)!!
                    }.recover { listOf(start, end) }
                        .getOrThrow()
                }.flatten().toSet().toList()
            } else waypoints.toList()

            this.trajectory.set(
                FalconTrajectoryGenerator.generateTrajectory(
                    waypoints = wayPoints,
                    constraints = listOf(CentripetalAccelerationConstraint(maxCentripetalAcceleration.value)),
                    startVelocity = startVelocity.value.feet.velocity,
                    endVelocity = endVelocity.value.feet.velocity,
                    maxVelocity = maxVelocity.value.feet.velocity,
                    maxAcceleration = maxAcceleration.value.feet.acceleration,
                    reversed = reversed.value
                )
            )

            //VelocityChart.update(trajectory)
        }
    }
}