package org.ghrobotics.falcondashboard.generator.charts

import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Tooltip
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.ghrobotics.falcondashboard.Properties
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2dWithCurvature
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TimedEntry
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TimedTrajectory
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TrajectorySamplePoint
import org.ghrobotics.lib.mathematics.units.second
import tornadofx.MultiValue
import tornadofx.data
import tornadofx.style
import java.text.DecimalFormat

object PositionChart : LineChart<Number, Number>(
    NumberAxis(0.0, 54.0, 1.0),
    NumberAxis(0.0, 27.0, 1.0)
) {

    init {
        style {
            backgroundColor = MultiValue(arrayOf<Paint>(Color.LIGHTGRAY))
        }
        lookup(".chart-plot-background").style +=
                "-fx-background-image: url(\"chart-background.png\");" +
                "-fx-background-size: stretch;" +
                "-fx-background-position: top right;" +
                "-fx-background-repeat: no-repeat;"

        setMinSize(54 * 25.0, 28 * 25.0)
        setPrefSize(54 * 25.0, 28 * 25.0)
        setMaxSize(54 * 25.0, 28 * 25.0)

        axisSortingPolicy = LineChart.SortingPolicy.NONE
        isLegendVisible = false
        animated = false
    }

    fun update(trajectory: TimedTrajectory<Pose2dWithCurvature>) {
        data.clear()

        val seriesXY = XYChart.Series<Number, Number>()
        val seriesRobotStart = XYChart.Series<Number, Number>()
        val seriesRobotEnd = XYChart.Series<Number, Number>()

        with(seriesXY) {
            val iterator = trajectory.iterator()

            while (!iterator.isDone) {
                val point: TrajectorySamplePoint<TimedEntry<Pose2dWithCurvature>> = iterator.advance(0.02.second)
                data(
                    point.state.state.pose.translation.x.feet,
                    point.state.state.pose.translation.y.feet,
                    point.state.state.pose.rotation.degree
                )
            }
            this@PositionChart.data.add(this)

            data.forEach { entry ->
                val format = DecimalFormat("##.##")

                val x = format.format(entry.xValue)
                val y = format.format(entry.yValue)
                val a = format.format(entry.extraValue)

                val t = Tooltip("$x feet, $y feet, $a degrees")
                Tooltip.install(entry.node, t)
            }
        }


        with(seriesRobotStart) {
            getRobotBoundingBox(trajectory.firstState.state.pose).forEach {
                data(it.translation.x.feet, it.translation.y.feet)
            }
            this@PositionChart.data.add(this)
        }

        with(seriesRobotEnd) {
            getRobotBoundingBox(trajectory.lastState.state.pose).forEach {
                data(it.translation.x.feet, it.translation.y.feet)
            }
            this@PositionChart.data.add(this)
        }

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