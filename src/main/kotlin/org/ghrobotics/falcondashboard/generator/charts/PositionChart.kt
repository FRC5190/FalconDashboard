package org.ghrobotics.falcondashboard.generator.charts

import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Tooltip
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.ghrobotics.falcondashboard.Properties
import org.ghrobotics.falcondashboard.generator.GeneratorView
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2dWithCurvature
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TimedEntry
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TimedTrajectory
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TrajectorySamplePoint
import org.ghrobotics.lib.mathematics.units.feet
import org.ghrobotics.lib.mathematics.units.second
import tornadofx.MultiValue
import tornadofx.data
import tornadofx.style

object PositionChart : LineChart<Number, Number>(
    NumberAxis(0.0, 54.0, 1.0),
    NumberAxis(0.0, 27.0, 1.0)
) {

    private val seriesXY = XYChart.Series<Number, Number>()
    private val seriesRobotStart = XYChart.Series<Number, Number>()
    private val seriesRobotEnd = XYChart.Series<Number, Number>()
    private val seriesWayPoints = XYChart.Series<Number, Number>()

    init {
        style {
            backgroundColor = MultiValue(arrayOf<Paint>(Color.LIGHTGRAY))
        }
        lookup(".chart-plot-background").style +=
            "-fx-background-image: url(\"chart-background.png\");" +
            "-fx-background-size: stretch;" +
            "-fx-background-position: top right;" +
            "-fx-background-repeat: no-repeat;"

        axisSortingPolicy = LineChart.SortingPolicy.NONE
        isLegendVisible = false
        animated = false

        data.add(seriesXY)
        data.add(seriesRobotStart)
        data.add(seriesRobotEnd)
        data.add(seriesWayPoints)

        update(GeneratorView.trajectory.value)
        updateWaypoints(GeneratorView.waypoints)
        GeneratorView.trajectory.addListener { _, _, trajectory ->
            update(trajectory)
            updateWaypoints(GeneratorView.waypoints)
        }
    }

    override fun resize(width: Double, height: Double) {
        super.resize(height / 27 * 54, height)
    }

    private fun updateWaypoints(wayPoints: List<Pose2d>) {
        seriesWayPoints.data.clear()
        wayPoints.forEach { pose2d ->
            seriesWayPoints.data(
                pose2d.translation.x.feet,
                pose2d.translation.y.feet,
                pose2d.rotation.degree
            ) {
                var currentPose2d = pose2d

                node.setOnMouseDragged { event ->
                    val newMouseX = event.sceneX
                    val newMouseY = event.sceneY

                    val localMouseX = xAxis.sceneToLocal(newMouseX, newMouseY).x
                    val localMouseY = yAxis.sceneToLocal(newMouseX, newMouseY).y

                    xValue = xAxis.getValueForDisplay(localMouseX)
                    yValue = yAxis.getValueForDisplay(localMouseY)
                }
                node.setOnMouseReleased {
                    // Send update
                    val newPose2d = Pose2d(
                        Translation2d(
                            xValue.feet,
                            yValue.feet
                        ),
                        pose2d.rotation
                    )
                    GeneratorView.waypoints[GeneratorView.waypoints.indexOf(currentPose2d)] = newPose2d
                    currentPose2d = newPose2d
                }
            }
        }
    }

    private fun update(trajectory: TimedTrajectory<Pose2dWithCurvature>) {
        val iterator = trajectory.iterator()

        seriesXY.data.clear()
        while (!iterator.isDone) {
            val point: TrajectorySamplePoint<TimedEntry<Pose2dWithCurvature>> = iterator.advance(0.02.second)
            val data = seriesXY.data(
                point.state.state.pose.translation.x.feet,
                point.state.state.pose.translation.y.feet,
                point.state.state.pose.rotation.degree
            )
            Tooltip.install(
                data.node,
                Tooltip(
                    "%2.2f feet, %2.2f feet, %2.2f degrees".format(
                        data.xValue,
                        data.yValue,
                        data.extraValue
                    )
                )
            )
        }

        seriesRobotStart.data.clear()
        getRobotBoundingBox(trajectory.firstState.state.pose).forEach {
            seriesRobotStart.data(it.translation.x.feet, it.translation.y.feet)
        }

        seriesRobotEnd.data.clear()
        getRobotBoundingBox(trajectory.lastState.state.pose).forEach {
            seriesRobotEnd.data(it.translation.x.feet, it.translation.y.feet)
        }

        seriesWayPoints.data.forEach { it.node.toFront() }
    }


    private fun getRobotBoundingBox(center: Pose2d): Array<Pose2d> {
        val tl = center.transformBy(
            Pose2d(Translation2d(-Properties.robotLength / 2, Properties.robotWidth / 2))
        )

        val tr = center.transformBy(
            Pose2d(Translation2d(Properties.robotLength / 2, Properties.robotWidth / 2))
        )

        val bl = center.transformBy(
            Pose2d(Translation2d(-Properties.robotLength / 2, -Properties.robotWidth / 2))
        )

        val br = center.transformBy(
            Pose2d(Translation2d(Properties.robotLength / 2, -Properties.robotWidth / 2))
        )
        return arrayOf(tl, tr, br, bl, tl)
    }
}