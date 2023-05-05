package org.ghrobotics.falcondashboard.generator.charts

import edu.wpi.first.wpilibj.geometry.Rotation2d
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.control.Tooltip
import javafx.scene.input.MouseButton
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.ghrobotics.falcondashboard.generator.GeneratorView
import org.ghrobotics.falcondashboard.generator.charts.PositionChart.setOnMouseClicked
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.x_u
import org.ghrobotics.lib.mathematics.twodim.geometry.y_u
import org.ghrobotics.lib.mathematics.units.feet
import org.ghrobotics.lib.mathematics.units.inFeet
import tornadofx.MultiValue
import tornadofx.bind
import tornadofx.data
import tornadofx.style

/**
 * Chart that is used to display the field view for the trajectory
 * generator.
 */
object PositionChart : LineChart<Number, Number>(
    NumberAxis(0.0, 54.0, 1.0),
    NumberAxis(0.0, 27.0, 1.0)
) {
    // Series
    private val seriesXY = Series<Number, Number>()
    private val seriesWayPoints = Series<Number, Number>()

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

        // Add waypoint on double click
        setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY) {
                if (it.clickCount == 2) {
                    val plotX = xAxis.getValueForDisplay(xAxis.sceneToLocal(it.sceneX, it.sceneY).x)
                    val plotY = yAxis.getValueForDisplay(yAxis.sceneToLocal(it.sceneX, it.sceneY).y)
                    GeneratorView.waypoints.add(Pose2d(plotX.feet, plotY.feet, Rotation2d()))
                }
            }
        }

        // Bind data to waypoints table
        seriesWayPoints.data
            .bind(GeneratorView.waypoints) {
                val data = Data<Number, Number>(
                    it.translation.x_u.inFeet(),
                    it.translation.y_u.inFeet()
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
    private fun updateSeriesXY() {
        seriesXY.data.clear()

        val duration = GeneratorView.trajectory.value.totalTimeSeconds
        var t = 0.0
        val dt = 0.02

        while (t <= duration) {
            val point = GeneratorView.trajectory.value.sample(t)
            t += dt

            val data = seriesXY.data(
                point.poseMeters.translation.x_u.inFeet(),
                point.poseMeters.translation.y_u.inFeet(),
                point.poseMeters.rotation.degrees
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