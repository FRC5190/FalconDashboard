package org.ghrobotics.falcondashboard.livevisualizer.charts

import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.paint.Color
import org.ghrobotics.falcondashboard.Properties
import org.ghrobotics.falcondashboard.Properties.kFieldHeight
import org.ghrobotics.falcondashboard.Properties.kFieldWidth
import org.ghrobotics.lib.mathematics.twodim.geometry.Transform2d
import org.ghrobotics.lib.mathematics.twodim.geometry.x_u
import org.ghrobotics.lib.mathematics.twodim.geometry.y_u
import org.ghrobotics.lib.mathematics.units.*
import tornadofx.data
import tornadofx.multi
import tornadofx.style



object FieldChart : LineChart<Number, Number>(
    NumberAxis(0.0, kFieldWidth, 0.5),
    NumberAxis(0.0, kFieldHeight, 0.5)
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
        createSymbols = false

        verticalGridLinesVisible = false
        isHorizontalGridLinesVisible = false

        data.add(robotSeries)
        data.add(pathSeries)
        data.add(robotBoundingBoxSeries)
        data.add(visionTargetSeries)
    }

    override fun resize(width: Double, height: Double) {
        val newWidth = height / kFieldHeight * kFieldWidth
        if (newWidth > width) {
            super.resize(width, width / kFieldWidth * kFieldHeight)
        } else {
            super.resize(newWidth, height)
        }
    }

    fun addRobotPathPose(pose2d: Pose2d) {
        @Suppress("UNCHECKED_CAST")
        robotSeries.data(
            pose2d.translation.x_u.inMeters(),
            pose2d.translation.y_u.inMeters()
        )
    }

    fun addPathPose(pose2d: Pose2d) {
        @Suppress("UNCHECKED_CAST")
        pathSeries.data(
            pose2d.translation.x_u.inMeters(),
            pose2d.translation.y_u.inMeters()
        )
    }

    fun updateRobotPose(pose2d: Pose2d) {
        robotBoundingBoxSeries.data.clear()
        getRobotBoundingBox(pose2d).forEach {
            robotBoundingBoxSeries.data(
                it.translation.x_u.inMeters(),
                it.translation.y_u.inMeters()
            )
        }
    }

    fun updateVisionTargets(newVisionTargets: List<Pose2d>) {
        visionTargetSeries.data.clear()
        newVisionTargets.forEach {
            val data = XYChart.Data<Number, Number>(
                it.translation.x_u.inMeters(),
                it.translation.y_u.inMeters()
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
            Transform2d(-Properties.kRobotLength / 2, Properties.kRobotWidth / 2, Rotation2d())
        )

        val tr = center.transformBy(
            Transform2d(Properties.kRobotLength / 2, Properties.kRobotWidth / 2, Rotation2d())
        )

        val mid = center.transformBy(
            Transform2d(Properties.kRobotLength / 2.0 + 4.inches, 0.inches, Rotation2d())
        )

        val bl = center.transformBy(
            Transform2d(-Properties.kRobotLength / 2, -Properties.kRobotWidth / 2, Rotation2d())
        )

        val br = center.transformBy(
            Transform2d(Properties.kRobotLength / 2, -Properties.kRobotWidth / 2, Rotation2d())
        )

        return arrayOf(tl, tr, mid, br, bl, tl)
    }

    fun clear() {
        robotSeries.data.clear()
        pathSeries.data.clear()
    }
}

