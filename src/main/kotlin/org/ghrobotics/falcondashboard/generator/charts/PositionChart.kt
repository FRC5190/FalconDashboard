package org.ghrobotics.falcondashboard.generator.charts

import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Tooltip
import javafx.scene.input.MouseButton
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.ghrobotics.falcondashboard.Settings
import org.ghrobotics.falcondashboard.generator.GeneratorView
import org.ghrobotics.falcondashboard.generator.charts.PositionChart.setOnMouseClicked
import org.ghrobotics.falcondashboard.triggerWaypoints
import org.ghrobotics.falcondashboard.ui
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.x_u
import org.ghrobotics.lib.mathematics.twodim.geometry.y_u
import org.ghrobotics.lib.mathematics.units.inMeters
import org.ghrobotics.lib.mathematics.units.meters
import tornadofx.MultiValue
import tornadofx.bind
import tornadofx.data
import tornadofx.style
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.math.cos
import kotlin.math.sin
import AdaptivePurePursuitController
import kotlin.math.PI
import kotlin.math.abs


/**
 * Chart that is used to display the field view for the trajectory
 * generator.
 */
object PositionChart : LineChart<Number, Number>(
    NumberAxis(0.0, 15.98, 1.0),
    NumberAxis(0.0, 8.21, 1.0)
) {
    // Series
    private val seriesXY = Series<Number, Number>()
    private val seriesWayPoints = Series<Number, Number>()
    val followerSeries = XYChart.Series<Number, Number>()
    var isTimerFinished = false
    var triggered = false

    private fun euclideanDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return Math.sqrt(Math.pow(x1-x2, 2.0) + Math.pow(y1-y2, 2.0))
    }

    init {
        // Set styles
        style {
            backgroundColor = MultiValue(arrayOf<Paint>(Color.LIGHTGRAY))
        }
        lookup(".chart-plot-background").style +=
            "-fx-background-image: url(\"chart-background.png\");" +
                "-fx-background-size: stretch;" +
                "-fx-background-position: top right;" +
                "-fx-background-repeat: no-repeat;"

        axisSortingPolicy = SortingPolicy.NONE
        isLegendVisible = false
        animated = false
        createSymbols = true
        verticalGridLinesVisible = false
        isHorizontalGridLinesVisible = false

        data.add(seriesXY)
        data.add(seriesWayPoints)
        data.add(followerSeries)

        PositionChart.setOnMouseEntered {
            ui {
                if(!triggered)
                {
                    triggerWaypoints()
                    triggered = true
                }
            }
        }

        // Add waypoint on double click
        setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY) {
                // Double click to add waypoint
                if (it.clickCount == 2)
                {
                    val plotX = xAxis.getValueForDisplay(xAxis.sceneToLocal(it.sceneX, it.sceneY).x)
                    val plotY = yAxis.getValueForDisplay(yAxis.sceneToLocal(it.sceneX, it.sceneY).y)
                    GeneratorView.waypoints.add(Pose2d(plotX.meters, plotY.meters, Rotation2d()))
                }
                // Control + click to delete waypoint
                else if (it.isControlDown)
                {
                    val x1 = xAxis.getValueForDisplay(xAxis.sceneToLocal(it.sceneX, it.sceneY).x).toDouble()
                    val y1 = yAxis.getValueForDisplay(yAxis.sceneToLocal(it.sceneX, it.sceneY).y).toDouble()
                    // Find the closest waypoint
                    val distances: ArrayList<Double> = ArrayList()
                    for (idx in 0 until GeneratorView.waypoints.size)
                    {
                        val x2 = GeneratorView.waypoints.get(idx).translation.x
                        val y2 = GeneratorView.waypoints.get(idx).translation.y
                        val dist = euclideanDistance(x1, y1, x2, y2)
                        distances.add(dist)
                    }
                    // Find the min (closest) index and it's distance
                    val minDistance = distances.min()!!
                    val minIdx = distances.indexOf(minDistance)
                    // Get robot size from properties
                    val robotSize = Math.sqrt(Math.pow(Settings.robotLength.value,2.0)
                            + Math.pow(Settings.robotWidth.value,2.0))
                    // If the clicked point is inside the robot and there are more than 2 waypoints
                    // get ready to remove waypoints
                    // TODO: Make this check with polygon intersection with actual robot rectangle
                    if (minDistance < robotSize && distances.size > 2)
                    {
                        // Remove the waypoint
                        GeneratorView.waypoints.remove(minIdx, minIdx+1)
                    }
                }
            }
        }

        // Bind data to waypoints table
        seriesWayPoints.data
            .bind(GeneratorView.waypoints) {
                val data = Data<Number, Number>(
                    it.translation.x_u.inMeters(),
                    it.translation.y_u.inMeters()
                )
                val currentPose2d = SimpleObjectProperty(it)
                currentPose2d.addListener { _, oldPose, newPose ->
                    GeneratorView.waypoints[GeneratorView.waypoints.indexOf(oldPose)] = newPose
                }
                val node = PositionNode(
                    data,
                    (xAxis as NumberAxis),
                    (yAxis as NumberAxis),
                    currentPose2d
                )
                data.node = node

                data
            }

        updateSeriesXY()
        GeneratorView.trajectory.addListener { _, _, _ ->
            updateSeriesXY()
        }
    }

    /**
     * Updates the trajectory on the field.
     */
     fun updateSeriesXY() {
        seriesXY.data.clear()

        val duration = GeneratorView.trajectory.value.totalTimeSeconds
        var t = 0.0
        val dt = 0.02

        while (t <= duration) {
            val point = GeneratorView.trajectory.value.sample(t)
            t += dt

            val data = seriesXY.data(
                point.poseMeters.translation.x_u.inMeters(),
                point.poseMeters.translation.y_u.inMeters(),
                point.poseMeters.rotation.degrees
            )
            Tooltip.install(
                data.node,
                Tooltip(
                    "%2.2f meters, %2.2f meters, %2.2f degrees".format(
                        data.xValue,
                        data.yValue,
                        data.extraValue
                    )
                )
            )
            data.node.toBack()
        }
    }



    private fun updateFollowerPose(pose2d: Pose2d) {
        followerSeries.data.clear()
        val data = XYChart.Data<Number, Number>(
            pose2d.translation.x_u.inMeters(),
            pose2d.translation.y_u.inMeters()
        )

        data.node = FollowerNode(
            pose2d.rotation,
            (xAxis as NumberAxis).scaleProperty()
        )

        followerSeries.data.add(data)
    }

    // Live Trajectory
    fun playTrajectory()
    {
        if(Settings.purePursuit.value == false) {
            val duration = GeneratorView.trajectory.value.totalTimeSeconds
            var t = 0.0
            val dt = 0.02
            val timer = Timer("scheduler", true);

            timer.schedule(1000, 20)
            {

                if (t > duration) {
                    ui { followerSeries.data.clear() }
                    isTimerFinished = true
                    timer.cancel()
                    timer.purge()
                }

                val point = GeneratorView.trajectory.value.sample(t)
                t += dt

                val followerPose = Pose2d(
                    point.poseMeters.translation.x_u.inMeters(),
                    point.poseMeters.translation.y_u.inMeters(),
                    Rotation2d(point.poseMeters.rotation.radians)
                )
                ui { updateFollowerPose(followerPose) }

            }

            ui { followerSeries.data.clear() }
        }
        else
        {
            var appc = AdaptivePurePursuitController()
            appc.reset()
            val trajectory = GeneratorView.trajectory.value
            val point = trajectory.sample(0.0)
            var followerPose = Pose2d(
                point.poseMeters.translation.x_u.inMeters(),
                point.poseMeters.translation.y_u.inMeters(),
                Rotation2d(point.poseMeters.rotation.radians)
            )
            val duration = GeneratorView.trajectory.value.totalTimeSeconds
            var t = 0.0
            val dt = 0.02
            val timer = Timer("scheduler", true)
            val targetPose = trajectory.sample(duration)
            var isReached = false
            val translateAccuracy = 0.05
            val rotateAccuracy = 0.1

            timer.schedule(1000, 20)
            {
                val speeds = appc.update(trajectory, followerPose, followerPose.rotation.radians, Settings.reversed.value)
                var leftSpeed = speeds[0]
                var rightSpeed = speeds[1]
                /*
                if(Settings.reversed.value)
                {
                    leftSpeed = speeds[1]
                    rightSpeed = speeds[0]
                }
                */

                println(String.format("Left Speed %f Right Speed %f", leftSpeed, rightSpeed))
                println(String.format("Pose X %f Pose Y %f Theta %f", followerPose.translation.x,
                    followerPose.translation.y, followerPose.rotation.radians))
                val angularSpeed = (rightSpeed - leftSpeed ) / Settings.robotWidth.value
                val linearSpeed = (rightSpeed + leftSpeed) / 2.0
                val xIncrement = linearSpeed * dt * cos(followerPose.rotation.radians)
                val yIncrement = linearSpeed * dt * sin(followerPose.rotation.radians)
                var angIncrement = angularSpeed*dt
                if(followerPose.rotation.radians + angIncrement > PI)
                {
                    angIncrement -= 2*PI
                }
                else if(followerPose.rotation.radians + angIncrement < -PI)
                {
                    angIncrement += 2*PI
                }
                val newPose = Pose2d(
                    followerPose.translation.x + xIncrement,
                    followerPose.translation.y + yIncrement,
                    Rotation2d(followerPose.rotation.radians + angIncrement)
                )
                followerPose = newPose
                t += dt
                ui { updateFollowerPose(followerPose) }
                // TODO: Compare final pose and current pose and set isReached
                val poseDiff = targetPose.poseMeters.minus(followerPose)
                if(abs(poseDiff.translation.x) < translateAccuracy && abs(poseDiff.translation.y) < translateAccuracy && abs(poseDiff.rotation.radians) < translateAccuracy)
                {
                    isReached = true
                    println("Pose reached")
                }
                if (t > duration * 2 || isReached == true)
                {
                    println(String.format("APPC completed in %f", t))
                    timer.purge()
                    timer.cancel()
                }
            }
            ui { followerSeries.data.clear() }
        }

    }

    override fun resize(width: Double, height: Double) = super.resize(1200.0, 600.0)
}