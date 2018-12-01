package org.ghrobotics.falcondashboard.livevisualizer.charts

import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import tornadofx.*

object FieldChart : LineChart<Number, Number>(
    NumberAxis(0.0, 54.0, 1.0),
    NumberAxis(0.0, 27.0, 1.0)
) {

    private val mainSeries = XYChart.Series<Number, Number>()

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
        animated = true

        data.add(mainSeries)
    }

    fun update(robotPose: Pose2d) {
        mainSeries.data.add(XYChart.Data(robotPose.translation.x.feet, robotPose.translation.y.feet))
    }

    fun clear() {
        mainSeries.data.clear()
    }
}

