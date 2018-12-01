package org.ghrobotics.falcondashboard.livevisualizer.charts

import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.ghrobotics.falcondashboard.Properties
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import tornadofx.MultiValue
import tornadofx.style

object FieldChart : LineChart<Number, Number>(
    NumberAxis(0.0, 54.0, 1.0),
    NumberAxis(0.0, 27.0, 1.0)
) {

    private val robotSeries = XYChart.Series<Number, Number>()
    private val pathSeries = XYChart.Series<Number, Number>()
    private val robotBoundingBoxSeries = XYChart.Series<Number, Number>()

    init {
        style {
            backgroundColor = MultiValue(arrayOf<Paint>(Color.LIGHTGRAY))
        }
        lookup(".chart-plot-background").style +=
                "-fx-background-image: url(\"chart-background.png\");" +
                "-fx-background-size: stretch;" +
                "-fx-background-position: top right;" +
                "-fx-background-repeat: no-repeat;"

        setMinSize(54 * 30.0, 28 * 30.0)
        setPrefSize(54 * 30.0, 28 * 30.0)
        setMaxSize(54 * 30.0, 28 * 30.0)

        axisSortingPolicy = LineChart.SortingPolicy.NONE
        isLegendVisible = false
        animated = false
        createSymbols = false

        data.add(robotSeries)
        data.add(pathSeries)
        data.add(robotBoundingBoxSeries)
    }

    fun updateRobot(robotPose: Pose2d) {
        robotSeries.data.add(XYChart.Data(robotPose.translation.x.feet, robotPose.translation.y.feet))
        robotBoundingBoxSeries.data.clear()
        getRobotBoundingBox(robotPose).forEach {
            robotBoundingBoxSeries.data.add(
                XYChart.Data(it.translation.x.feet, it.translation.y.feet)
            )
        }
    }

    fun updatePath(pathPose: Pose2d) {
        pathSeries.data.add(XYChart.Data(pathPose.translation.x.feet, pathPose.translation.y.feet))
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

    fun clear() {
        robotSeries.data.clear()
        pathSeries.data.clear()
    }
}

