package org.ghrobotics.generator

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import org.ghrobotics.generator.charts.PositionChart
import org.ghrobotics.generator.charts.VelocityChart
import org.ghrobotics.lib.mathematics.epsilonEquals
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.trajectory.DefaultTrajectoryGenerator
import org.ghrobotics.lib.mathematics.twodim.trajectory.PathFinder
import org.ghrobotics.lib.mathematics.twodim.trajectory.constraints.CentripetalAccelerationConstraint
import org.ghrobotics.lib.mathematics.units.degree
import org.ghrobotics.lib.mathematics.units.derivedunits.acceleration
import org.ghrobotics.lib.mathematics.units.derivedunits.velocity
import org.ghrobotics.lib.mathematics.units.feet
import tornadofx.App
import tornadofx.launch
import tornadofx.observable
import tornadofx.onChange

class Main : App(MainView::class) {
    companion object {

        val waypoints = arrayListOf(
            Pose2d(1.5.feet, 23.feet, 0.degree),
            Pose2d(11.5.feet, 23.feet, 0.degree)
        ).observable()

        val name = SimpleStringProperty("Baseline")

        val reversed = SimpleBooleanProperty(false)
        val pathfinder = SimpleBooleanProperty(false)
        val startVelocity = SimpleDoubleProperty(0.0)
        val endVelocity = SimpleDoubleProperty(0.0)
        val maxVelocity = SimpleDoubleProperty(10.0)
        val maxAcceleration = SimpleDoubleProperty(4.0)
        val maxCentripetalAcceleration = SimpleDoubleProperty(4.0)

        init {
            update()
            waypoints.onChange { update() }

            reversed.onChange { update() }
            pathfinder.onChange { update() }

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

            val wayPoints = if (pathfinder.value) {
                val pathFinder = PathFinder(
                    3.5.feet,
                    PathFinder.k2018CubesSwitch,
                    PathFinder.k2018LeftSwitch,
                    PathFinder.k2018Platform
                )
                Main.waypoints.zipWithNext { start, end ->
                    kotlin.runCatching {
                        pathFinder.findPath(start, end)!!
                    }.recover { listOf(start, end) }
                        .getOrThrow()
                }.flatten().toSet().toList()
            } else Main.waypoints.toList()

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

        @JvmStatic
        fun main(args: Array<String>) {
            launch<Main>(args)
        }
    }
}