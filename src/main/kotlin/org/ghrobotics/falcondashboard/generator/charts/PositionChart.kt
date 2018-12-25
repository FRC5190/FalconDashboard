package org.ghrobotics.falcondashboard.generator.charts

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Tooltip
import javafx.scene.input.MouseButton
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.ghrobotics.falcondashboard.generator.GeneratorView
import org.ghrobotics.falcondashboard.generator.charts.PositionChart.setOnMouseClicked
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2dWithCurvature
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TimedEntry
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TrajectorySamplePoint
import org.ghrobotics.lib.mathematics.units.feet
import org.ghrobotics.lib.mathematics.units.second
import tornadofx.MultiValue
import tornadofx.bind
import tornadofx.data
import tornadofx.style

object PositionChart : LineChart<Number, Number>(
    NumberAxis(0.0, 54.0, 1.0),
    NumberAxis(0.0, 27.0, 1.0)
) {

    private val seriesXY = XYChart.Series<Number, Number>()
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
        createSymbols = true

        data.add(seriesXY)
        data.add(seriesWayPoints)

        setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY) {
                if (it.clickCount == 2) {
                    val plotX = xAxis.getValueForDisplay(xAxis.sceneToLocal(it.sceneX, it.sceneY).x)
                    val plotY = yAxis.getValueForDisplay(yAxis.sceneToLocal(it.sceneX, it.sceneY).y)
                    GeneratorView.waypoints.add(Pose2d(Translation2d(plotX.feet, plotY.feet)))
                }
            }
        }

        seriesWayPoints.data
            .bind(GeneratorView.waypoints) {
                val data = XYChart.Data<Number, Number>(
                    it.translation.x.feet,
                    it.translation.y.feet
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

    private fun updateSeriesXY() {
        seriesXY.data.clear()

        val iterator = GeneratorView.trajectory.value.iterator()

        while (!iterator.isDone) {
            val point: TrajectorySamplePoint<TimedEntry<Pose2dWithCurvature>> =
                iterator.advance(0.02.second)
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
            data.node.toBack()
        }
    }

    override fun resize(width: Double, height: Double) = super.resize(height / 27 * 54, height)

}