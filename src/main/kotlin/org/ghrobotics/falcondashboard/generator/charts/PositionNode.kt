package org.ghrobotics.falcondashboard.generator.charts

import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import org.ghrobotics.falcondashboard.Properties
import org.ghrobotics.falcondashboard.mapprop
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.units.Rotation2d
import org.ghrobotics.lib.mathematics.units.degree
import org.ghrobotics.lib.mathematics.units.feet
import org.ghrobotics.lib.mathematics.units.radian
import tornadofx.*

class PositionNode(
    private val data: XYChart.Data<Number, Number>,
    private val xAxis: NumberAxis,
    private val yAxis: NumberAxis,
    simpleObjectProperty: SimpleObjectProperty<Pose2d>
) : StackPane() {

    private val robotNode = RobotNode(
        mapprop(
            simpleObjectProperty
        ) { value.rotation },
        xAxis.scaleProperty()
    )

    init {
        var dragging = false

        style {
            backgroundColor = multi(Color.TRANSPARENT)
            padding = box(1.5.em)
        }
        children.add(robotNode)

        robotNode.robotPane.apply {
            var mouseXOffset = 0.0
            var mouseYOffset = 0.0

            setOnMousePressed { event ->
                if (!dragging) {
                    dragging = true
                    val location = event.locationRelativeToNode()
                    mouseXOffset = location.first
                    mouseYOffset = location.second
                }
            }

            setOnMouseDragged { event ->
                val (plotX, plotY) = event.locationRelativeToPlot()

                data.xValue = xAxis.getValueForDisplay(plotX - mouseXOffset)
                data.yValue = yAxis.getValueForDisplay(plotY - mouseYOffset)
            }

            setOnMouseReleased {
                dragging = false
                simpleObjectProperty.value = Pose2d(
                    Translation2d(
                        data.xValue.feet,
                        data.yValue.feet
                    ),
                    simpleObjectProperty.value.rotation
                )
            }

            setOnMouseEntered {
                scene.cursor = Cursor.MOVE
            }
            setOnMouseExited {
                scene.cursor = Cursor.CROSSHAIR
            }
        }

        var rotating = false
        var rotationOffset = 0.degree

        setOnMousePressed { event ->
            if (!rotating && !dragging) {
                rotating = true
                robotNode.robotRotation.unbind()
                rotationOffset = robotNode.robotRotation.get().degree - event.angleRelativeToNode()
            }
        }

        setOnMouseDragged { event ->
            if (rotating) {
                val angle = event.angleRelativeToNode()

                robotNode.robotRotation.set((angle + rotationOffset).degree)
            }
        }

        setOnMouseReleased {
            if (rotating) {
                rotating = false
                simpleObjectProperty.value = Pose2d(
                    simpleObjectProperty.value.translation,
                    -(robotNode.robotRotation.value.degree)
                )
                robotNode.bindRobotRotation()
            }
        }

        setOnMouseEntered {
            scene.cursor = Cursor.CROSSHAIR
        }
        setOnMouseExited {
            scene.cursor = Cursor.DEFAULT
        }
    }

    private fun MouseEvent.angleRelativeToNode(): Rotation2d {
        val (localMouseX, localMouseY) = locationRelativeToNode()

        return Math.atan2(localMouseY, localMouseX).radian
    }

    private fun MouseEvent.locationRelativeToNode(): Pair<Double, Double> {
        val (plotX, plotY) = locationRelativeToPlot()

        val localMouseX = plotX - xAxis.getDisplayPosition(data.xValue)
        val localMouseY = plotY - yAxis.getDisplayPosition(data.yValue)

        return localMouseX to localMouseY
    }

    private fun MouseEvent.locationRelativeToPlot(): Pair<Double, Double> {
        val plotX = xAxis.sceneToLocal(sceneX, sceneY).x
        val plotY = yAxis.sceneToLocal(sceneX, sceneY).y
        return plotX to plotY
    }

}