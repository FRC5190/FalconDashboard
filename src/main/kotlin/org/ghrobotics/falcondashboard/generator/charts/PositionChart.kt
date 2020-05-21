package org.ghrobotics.falcondashboard.generator.charts

import edu.wpi.first.wpilibj.geometry.Rotation2d
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.control.Tooltip
import javafx.scene.input.MouseButton
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.ghrobotics.falcondashboard.Properties
import org.ghrobotics.falcondashboard.generator.GeneratorView
import org.ghrobotics.falcondashboard.generator.charts.PositionChart.setOnMouseClicked
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.x_u
import org.ghrobotics.lib.mathematics.twodim.geometry.y_u
import org.ghrobotics.lib.mathematics.units.meters
import org.ghrobotics.lib.mathematics.units.inMeters
import tornadofx.MultiValue
import tornadofx.bind
import tornadofx.data
import tornadofx.style

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

    fun euclideanDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return Math.sqrt(Math.pow(x1-x2,2.0) + Math.pow(y1-y2,2.0))
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
                    println("Control down ")
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
                    print("Min of list: ")
                    println(minDistance)
                    // Get robot size from properties
                    val robotSize = Math.sqrt(Math.pow(Properties.kRobotLength.value,2.0)
                            + Math.pow(Properties.kRobotWidth.value,2.0))
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
    private fun updateSeriesXY() {
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

    override fun resize(width: Double, height: Double) = super.resize(1200.0, 600.0)
}