package org.ghrobotics.falcondashboard.livevisualizer.charts

import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.paint.Color
import org.ghrobotics.falcondashboard.Properties
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import tornadofx.data
import tornadofx.multi
import tornadofx.style

object FieldChart : LineChart<Number, Number>(
    NumberAxis(0.0, 54.0, 1.0),
    NumberAxis(0.0, 27.0, 1.0)
) {

    private val robotSeries = XYChart.Series<Number, Number>()
    private val pathSeries = XYChart.Series<Number, Number>()
    private val robotBoundingBoxSeries = XYChart.Series<Number, Number>()
    private val visionTargetSeries = XYChart.Series<Number, Number>()

    init {
        style {
            backgroundColor = multi(Color.LIGHTGRAY)
        }
        lookup(".chart-plot-background").style +=
            "-fx-background-image: url(\"chart-background.png\");" +
            "-fx-background-size: stretch;" +
            "-fx-background-position: top right;" +
            "-fx-background-repeat: no-repeat;"

        axisSortingPolicy = LineChart.SortingPolicy.NONE
        isLegendVisible = false
        animated = false
        createSymbols = true

        verticalGridLinesVisible = false
        isHorizontalGridLinesVisible = false

        data.add(robotSeries)
        data.add(pathSeries)
        data.add(robotBoundingBoxSeries)
        data.add(visionTargetSeries)
    }

    override fun resize(width: Double, height: Double) {
        val newWidth = height / 27 * 54
        if (newWidth > width) {
            super.resize(width, width / 54 * 27)
        } else {
            super.resize(newWidth, height)
        }
    }

    fun addRobotPathPose(pose2d: Pose2d) {
        @Suppress("UNCHECKED_CAST")
        robotSeries.data(
            pose2d.translation.x.feet,
            pose2d.translation.y.feet
        )
    }

    fun addPathPose(pose2d: Pose2d) {
        @Suppress("UNCHECKED_CAST")
        pathSeries.data(
            pose2d.translation.x.feet,
            pose2d.translation.y.feet
        )
    }

    fun updateRobotPose(pose2d: Pose2d) {
        robotBoundingBoxSeries.data.clear()
        getRobotBoundingBox(pose2d).forEach {
            robotBoundingBoxSeries.data(
                it.translation.x.feet,
                it.translation.y.feet
            )
        }
    }

    fun updateVisionTargets(newVisionTargets: List<Pose2d>) {
        visionTargetSeries.data.clear()
        newVisionTargets.forEach {
            val data = XYChart.Data<Number, Number>(
                it.translation.x.feet,
                it.translation.y.feet
            )
            data.node = VisionTargetNode(
                it.rotation,
                (xAxis as NumberAxis).scaleProperty()
            )
            visionTargetSeries.data.add(data)
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

    fun clear() {
        robotSeries.data.clear()
        pathSeries.data.clear()
    }
}

