package org.ghrobotics.falcondashboard.generator

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.GsonBuilder
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.StageStyle
import kfoenix.jfxbutton
import kfoenix.jfxcheckbox
import kfoenix.jfxtabpane
import kfoenix.jfxtextfield
import org.ghrobotics.falcondashboard.createNumericalEntry
import org.ghrobotics.falcondashboard.generator.GeneratorView.Settings.autoPathFinding
import org.ghrobotics.falcondashboard.generator.GeneratorView.Settings.endVelocity
import org.ghrobotics.falcondashboard.generator.GeneratorView.Settings.maxAcceleration
import org.ghrobotics.falcondashboard.generator.GeneratorView.Settings.maxCentripetalAcceleration
import org.ghrobotics.falcondashboard.generator.GeneratorView.Settings.maxVelocity
import org.ghrobotics.falcondashboard.generator.GeneratorView.Settings.name
import org.ghrobotics.falcondashboard.generator.GeneratorView.Settings.reversed
import org.ghrobotics.falcondashboard.generator.GeneratorView.Settings.startVelocity
import org.ghrobotics.falcondashboard.generator.charts.PositionChart
import org.ghrobotics.falcondashboard.generator.charts.VelocityChart
import org.ghrobotics.falcondashboard.generator.fragments.CodeFragment
import org.ghrobotics.falcondashboard.generator.fragments.WaypointFragment
import org.ghrobotics.falcondashboard.generator.tables.WaypointsTable
import org.ghrobotics.lib.mathematics.epsilonEquals
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.trajectory.DefaultTrajectoryGenerator
import org.ghrobotics.lib.mathematics.twodim.trajectory.PathFinder
import org.ghrobotics.lib.mathematics.twodim.trajectory.constraints.CentripetalAccelerationConstraint
import org.ghrobotics.lib.mathematics.units.degree
import org.ghrobotics.lib.mathematics.units.derivedunits.acceleration
import org.ghrobotics.lib.mathematics.units.derivedunits.velocity
import org.ghrobotics.lib.mathematics.units.feet
import tornadofx.*
import java.io.File
import java.io.FileReader
import java.io.FileWriter

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
            tab("Position") {
                add(PositionChart)
                isClosable = false
            }
            tab("Velocity") {
                add(VelocityChart)
                isClosable = false
            }
        }
    }

    companion object {

        val waypoints = arrayListOf(
            Pose2d(1.5.feet, 23.feet, 0.degree),
            Pose2d(11.5.feet, 23.feet, 0.degree)
        ).observable()

        private val gson = GsonBuilder().registerTypeAdapter<Settings> {
            write {
                beginArray()
                value(it.reversed.value)
                value(it.autoPathFinding.value)
                value(it.startVelocity.value)
                value(it.endVelocity.value)
                value(it.maxVelocity.value)
                value(it.maxAcceleration.value)
                value(it.maxCentripetalAcceleration.value)
                endArray()
            }
            read {
                beginArray()
                Settings.reversed.set(nextBoolean())
                Settings.autoPathFinding.set(nextBoolean())
                Settings.startVelocity.set(nextDouble())
                Settings.endVelocity.set(nextDouble())
                Settings.maxVelocity.set(nextDouble())
                Settings.maxAcceleration.set(nextDouble())
                Settings.maxCentripetalAcceleration.set(nextDouble())
                endArray()
                return@read Settings
            }
        }.create()!!

        init {
            val file = File("settings.json")
            if (file.exists()) {
                gson.fromJson<Settings>(FileReader(file))
            } else {
                val writer = FileWriter(file)
                writer.write(gson.toJson(Settings))
                writer.close()
            }

            update()
            waypoints.onChange { update() }
            reversed.onChange { update() }
            autoPathFinding.onChange { update() }

            startVelocity.onChange { update() }
            endVelocity.onChange { update() }
            maxVelocity.onChange { update() }
            maxAcceleration.onChange { update() }
            maxCentripetalAcceleration.onChange { update() }
        }

        fun saveSettings() {
            val writer = FileWriter(File("settings.json"))
            writer.write(gson.toJson(Settings))
            writer.close()
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

            val trajectory = DefaultTrajectoryGenerator.generateTrajectory(
                wayPoints = wayPoints,
                constraints = listOf(CentripetalAccelerationConstraint(maxCentripetalAcceleration.value.feet.acceleration)),
                startVelocity = startVelocity.value.feet.velocity,
                endVelocity = endVelocity.value.feet.velocity,
                maxVelocity = maxVelocity.value.feet.velocity,
                maxAcceleration = maxAcceleration.value.feet.acceleration,
                reversed = reversed.value
            )
            PositionChart.update(trajectory)
            VelocityChart.update(trajectory)
        }
    }

    object Settings {
        val name = SimpleStringProperty("Baseline")
        val reversed = SimpleBooleanProperty(false)
        val autoPathFinding = SimpleBooleanProperty(false)
        val startVelocity = SimpleDoubleProperty(0.0)
        val endVelocity = SimpleDoubleProperty(0.0)
        val maxVelocity = SimpleDoubleProperty(10.0)
        val maxAcceleration = SimpleDoubleProperty(4.0)
        val maxCentripetalAcceleration = SimpleDoubleProperty(4.0)
    }
}