package org.ghrobotics.falcondashboard.generator

import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator
import edu.wpi.first.wpilibj.trajectory.TrajectoryUtil
import edu.wpi.first.wpilibj.trajectory.constraint.CentripetalAccelerationConstraint
import javafx.beans.property.SimpleObjectProperty
import javafx.event.Event
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import kfoenix.jfxbutton
import kfoenix.jfxcheckbox
import kfoenix.jfxtabpane
import org.ghrobotics.falcondashboard.MainView
import org.ghrobotics.falcondashboard.Settings.autoPathFinding
import org.ghrobotics.falcondashboard.Settings.clampedCubic
import org.ghrobotics.falcondashboard.Settings.endVelocity
import org.ghrobotics.falcondashboard.Settings.maxAcceleration
import org.ghrobotics.falcondashboard.Settings.maxCentripetalAcceleration
import org.ghrobotics.falcondashboard.Settings.maxVelocity
import org.ghrobotics.falcondashboard.Settings.reversed
import org.ghrobotics.falcondashboard.Settings.robotLength
import org.ghrobotics.falcondashboard.Settings.robotWidth
import org.ghrobotics.falcondashboard.Settings.startVelocity
import org.ghrobotics.falcondashboard.Settings.trajectoryTime
import org.ghrobotics.falcondashboard.createNumericalEntry
import org.ghrobotics.falcondashboard.generator.charts.PositionChart
import org.ghrobotics.falcondashboard.generator.charts.VelocityChart
import org.ghrobotics.falcondashboard.generator.fragments.KtCodeFragment
import org.ghrobotics.falcondashboard.generator.fragments.WaypointFragment
import org.ghrobotics.falcondashboard.generator.tables.WaypointsTable
import org.ghrobotics.falcondashboard.saveToJSON
import org.ghrobotics.falcondashboard.triggerWaypoints
import org.ghrobotics.lib.mathematics.epsilonEquals
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.trajectory.FalconTrajectoryConfig
import org.ghrobotics.lib.mathematics.units.derived.acceleration
import org.ghrobotics.lib.mathematics.units.derived.velocity
import org.ghrobotics.lib.mathematics.units.meters
import tornadofx.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.concurrent.schedule

class GeneratorView : View() {

    override val root = hbox {
        stylesheets += resources["/GeneratorStyle.css"]
        vbox {
            style {
                paddingAll = 20.0
                maxWidth = 300.px
                spacing = 5.px
                // backgroundColor = multi(Color.web("#353535"))
            }

            /*
            hbox {

                paddingAll = 5
                jfxtextfield {
                    bind(name)
                    prefWidth = 290.0
                }

            }
            */

            jfxcheckbox {
                paddingAll = 5
                text = "Reversed"
                bind(reversed)
            }
            jfxcheckbox {
                paddingAll = 5
                text = "Clamped Cubic"
                bind(clampedCubic)
            }
            /*
            jfxcheckbox {
                paddingAll = 5
                text = "Auto Path Finding (Experimental)"
                bind(autoPathFinding)
            }
            */
            text("Trajectory Time (s): ") {
                alignment = Pos.CENTER_LEFT
                bind(trajectoryTime)
            }

            createNumericalEntry("Robot Width (m)", robotWidth)
            createNumericalEntry("Robot Length (m)", robotLength)
            // Removing these two for now
            // createNumericalEntry("Start Velocity (m/s)", startVelocity)
            // createNumericalEntry("End Velocity (m/s)", endVelocity)
            createNumericalEntry("Max Velocity (m/s)", maxVelocity)
            createNumericalEntry("Max Acceleration (m/s/s)", maxAcceleration)
            createNumericalEntry("Max Centripetal Acceleration (m/s/s)", maxCentripetalAcceleration)

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
                    text = "Save to file"
                    action {
                        WaypointsTable.saveToFile()
                    }
                }
                jfxbutton {
                    prefWidth = 290.0
                    text = "Load from file"
                    action {
                        WaypointsTable.loadFromFile()
                    }
                }
                jfxbutton {
                    prefWidth = 290.0
                    text = "Save To JSON"
                    action {
                        // find<CodeFragment>().openModal(stageStyle = StageStyle.UTILITY)
                        val txt = TrajectoryUtil.serializeTrajectory(GeneratorView.trajectory.value)
                        saveToJSON(txt)
                    }
                }
                jfxbutton {
                    prefWidth = 290.0
                    text = "Generate Code"
                    action {
                        find<KtCodeFragment>().openModal(stageStyle = StageStyle.UTILITY)
                    }
                }

                /*
                jfxbutton {
                    prefWidth = 290.0
                    text = "Trigger waypoints"
                    action {
                        // PositionChart.followerBoundingBoxSeries.data.clear()
                        // Pose2d(3.0.meters,5.0.meters)
                        triggerWaypoints()
                    }
                }
                */

                jfxbutton {
                    prefWidth = 290.0
                    style = "-fx-graphic: url(\"green_play_48px.png\");"
                    action {
                        PositionChart.playTrajectory()
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

        var isInitialized = true

        val waypoints = observableListOf(
            Pose2d(3.meters, 6.meters, Rotation2d()),
            Pose2d(5.meters, 7.5.meters, Rotation2d())
        )

        private val config: TrajectoryConfig =
            FalconTrajectoryConfig(maxVelocity.value.meters.velocity, maxAcceleration.value.meters.acceleration)
                .setStartVelocity(startVelocity.value.meters.velocity)
                .setEndVelocity(endVelocity.value.meters.velocity)
                .addConstraint(CentripetalAccelerationConstraint(maxCentripetalAcceleration.value))
                .setReversed(reversed.value)

        val trajectory = SimpleObjectProperty(TrajectoryGenerator.generateTrajectory(waypoints, config))

        init {

            waypoints.onChange { update() }
            reversed.onChange { update() }
            clampedCubic.onChange { update() }
            autoPathFinding.onChange { update() }

            robotLength.onChange { update() }
            robotWidth.onChange { update() }
            startVelocity.onChange { update() }
            endVelocity.onChange { update() }
            maxVelocity.onChange { update() }
            maxAcceleration.onChange { update() }
            maxCentripetalAcceleration.onChange { update() }
        }

        @Synchronized
        private fun update() {
            if (robotLength.value.isNaN() ||
                robotWidth.value.isNaN() ||
                startVelocity.value.isNaN() ||
                endVelocity.value.isNaN() ||
                maxVelocity.value epsilonEquals 0.0 ||
                maxAcceleration.value epsilonEquals 0.0 ||
                maxCentripetalAcceleration.value epsilonEquals 0.0
            ) return

            val config = FalconTrajectoryConfig(maxVelocity.value.meters.velocity, maxAcceleration.value.meters.acceleration)
                .setStartVelocity(startVelocity.value.meters.velocity)
                .setEndVelocity(endVelocity.value.meters.velocity)
                .addConstraint(CentripetalAccelerationConstraint(maxCentripetalAcceleration.value))
                .setReversed(reversed.value)

            if(clampedCubic.value) {
                val startPose = waypoints.first()
                val endPose = waypoints.last()
                val interiorWaypoints = waypoints.subList(1, waypoints.size - 1).map { it.translation }

                this.trajectory.set(TrajectoryGenerator.generateTrajectory(startPose, interiorWaypoints, endPose, config))
            } else {
                this.trajectory.set(TrajectoryGenerator.generateTrajectory(waypoints, config))
            }
            // Update trajectory time
            val time = BigDecimal(this.trajectory.get().totalTimeSeconds).setScale(2, RoundingMode.HALF_EVEN)
            trajectoryTime.set("Trajectory Time (s): " + time)
            // TODO: Change robot width and height here
            // val mouseDrag = MouseDragEvent(MouseDragEvent.MOUSE_DRAGGED, 1.0, 2.0, 3.0, 4.0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null)
            // val mouseEvent = MouseEvent(MouseEvent.MOUSE_DRAGGED, 1.0, 2.0, 3.0, 4.0, MouseButton.PRIMARY, 1, false, false, false, false, true, false, false, true, true, true, null)
        }
    }
}