package org.ghrobotics.falcondashboard.generator

import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator
import edu.wpi.first.wpilibj.trajectory.TrajectoryUtil
import edu.wpi.first.wpilibj.trajectory.constraint.CentripetalAccelerationConstraint
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.StageStyle
import javafx.util.StringConverter
import kfoenix.jfxbutton
import kfoenix.jfxcheckbox
import kfoenix.jfxtabpane
import org.ghrobotics.falcondashboard.FalconDashboard
import org.ghrobotics.falcondashboard.Saver
import org.ghrobotics.falcondashboard.Saver.endVelocity
import org.ghrobotics.falcondashboard.Saver.lastSaveLoadFile
import org.ghrobotics.falcondashboard.Saver.lastSaveLoadFileProperty
import org.ghrobotics.falcondashboard.Saver.reversed
import org.ghrobotics.falcondashboard.Saver.startVelocity
import org.ghrobotics.falcondashboard.Settings
import org.ghrobotics.falcondashboard.Settings.clampedCubic
import org.ghrobotics.falcondashboard.Settings.lastSaveFileLocation
import org.ghrobotics.falcondashboard.Settings.maxAcceleration
import org.ghrobotics.falcondashboard.Settings.maxCentripetalAcceleration
import org.ghrobotics.falcondashboard.Settings.maxVelocity
import org.ghrobotics.falcondashboard.Settings.trackWidth
import org.ghrobotics.falcondashboard.createNumericalEntry
import org.ghrobotics.falcondashboard.generator.charts.PositionChart
import org.ghrobotics.falcondashboard.generator.charts.VelocityChart
import org.ghrobotics.falcondashboard.generator.fragments.WaypointFragment
import org.ghrobotics.falcondashboard.generator.tables.WaypointsTable
import org.ghrobotics.lib.mathematics.epsilonEquals
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.trajectory.FalconTrajectoryConfig
import org.ghrobotics.lib.mathematics.units.derived.acceleration
import org.ghrobotics.lib.mathematics.units.derived.velocity
import org.ghrobotics.lib.mathematics.units.feet
import org.ghrobotics.lib.mathematics.units.meters
import tornadofx.*
import java.io.File
import java.io.FileWriter

class GeneratorView : View() {

    override val root = hbox {
        stylesheets += resources["/GeneratorStyle.css"]
        vbox {
            style {
                paddingAll = 20.0
                maxWidth = 280.px
                spacing = 5.px
            }
            paddingAll = 5
            prefWidth = 100.0
            hbox {
                text {
                    bind(lastSaveLoadFileProperty, readonly = true, converter = object : StringConverter<File>() {
                        override fun toString(file: File?): String {
                            return file?.name ?: ""
                        }

                        override fun fromString(name: String?): File? = name?.let { File(name) }
                    })
                    alignment = Pos.BASELINE_CENTER
                }
            }
            hbox {
                alignment = Pos.BASELINE_CENTER
                button {
                    paddingAll = 5
                    text = "save"
                    action {
                        Saver.saveCurrentFile()
                    }
                    spacing = 10.0
                }
                button {
                    paddingAll = 5
                    text = "save as"
                    action {
                        Saver.save()
                    }
                    spacing = 10.0
                }

                button {
                    paddingAll = 5
                    text = "load"
                    action {
                        Saver.load()
                    }
                }
            }

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

            createNumericalEntry("Start Velocity (m/s)", startVelocity)
            createNumericalEntry("End Velocity (m/s)", endVelocity)
            createNumericalEntry("Max Velocity (m/s)", maxVelocity)
            createNumericalEntry("Max Acceleration (m/s/s)", maxAcceleration)
            createNumericalEntry("Track Width (m)", trackWidth)
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
                    text = "Send JSON to SmartDashboard"
                    action {
                        FalconDashboard.trajectory =
                            TrajectoryUtil.serializeTrajectory(trajectory.value)
                    }
                }
                jfxbutton {
                    prefWidth = 290.0
                    text = "Export JSON to file"
                    action {
                        val file = chooseFile(
                            "Save json", arrayOf(
                                FileChooser.ExtensionFilter(
                                    "json path file", "*.wpilib.json"
                                )
                            ),
                            FileChooserMode.Save,
                            op = {
                                File(lastSaveFileLocation.value).parentFile.let {
                                    if (it.isDirectory)
                                        initialDirectory = it
                                }
                                lastSaveLoadFile?.let {
                                    initialFileName = it.nameWithoutExtension
                                }
                            }
                        ).firstOrNull() ?: return@action
                        FileWriter(file).use {
                            it.write(TrajectoryUtil.serializeTrajectory(trajectory.value))
                        }
                        lastSaveFileLocation.set(file.absolutePath)
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
        val waypoints = observableListOf(
            Pose2d(1.5.feet, 23.feet, Rotation2d()),
            Pose2d(11.5.feet, 24.feet, Rotation2d())
        )

        private val config: TrajectoryConfig =
            FalconTrajectoryConfig(maxVelocity.value.meters.velocity, maxAcceleration.value.meters.acceleration)
                .setStartVelocity(startVelocity.value.meters.velocity)
                .setEndVelocity(endVelocity.value.meters.velocity)
                .setKinematics(DifferentialDriveKinematics(trackWidth.value))
                .addConstraint(CentripetalAccelerationConstraint(maxCentripetalAcceleration.value))
                .setReversed(reversed.value)

        val trajectory = SimpleObjectProperty(TrajectoryGenerator.generateTrajectory(waypoints, config))

        init {
            update()
            waypoints.onChange { update() }
            reversed.onChange { update() }
            clampedCubic.onChange { update() }
            startVelocity.onChange { update() }
            endVelocity.onChange { update() }
            maxVelocity.onChange { update() }
            maxAcceleration.onChange { update() }
            trackWidth.onChange { update() }
            maxCentripetalAcceleration.onChange { update() }
        }

        @Synchronized
        private fun update() {
            if (startVelocity.value.isNaN() ||
                endVelocity.value.isNaN() ||
                maxVelocity.value epsilonEquals 0.0 ||
                maxAcceleration.value epsilonEquals 0.0 ||
                maxCentripetalAcceleration.value epsilonEquals 0.0 ||
                trackWidth.value epsilonEquals 0.0
            ) return

            val waypoints = waypoints.toList()

            val config =
                FalconTrajectoryConfig(maxVelocity.value.meters.velocity, maxAcceleration.value.meters.acceleration)
                    .setStartVelocity(startVelocity.value.meters.velocity)
                    .setEndVelocity(endVelocity.value.meters.velocity)
                    .setKinematics(DifferentialDriveKinematics(trackWidth.value))
                    .addConstraint(CentripetalAccelerationConstraint(maxCentripetalAcceleration.value))
                    .setReversed(reversed.value)

            if (clampedCubic.value) {
                val startPose = waypoints.first()
                val endPose = this.waypoints.last()
                val interiorWaypoints = waypoints.subList(1, this.waypoints.size - 1).map { it.translation }

                this.trajectory.set(
                    TrajectoryGenerator.generateTrajectory(
                        startPose,
                        interiorWaypoints,
                        endPose,
                        config
                    )
                )
            } else {
                this.trajectory.set(TrajectoryGenerator.generateTrajectory(waypoints, config))
            }
        }
    }
}