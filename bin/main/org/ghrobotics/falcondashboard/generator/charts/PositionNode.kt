package org.ghrobotics.falcondashboard.generator.charts

import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Cursor
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import org.ghrobotics.falcondashboard.mapprop
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.units.feet
import tornadofx.box
import tornadofx.em
import tornadofx.multi
import tornadofx.style

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
        var rotationOffset = Rotation2d()

        setOnMousePressed { event ->
            if (!rotating && !dragging) {
                rotating = true
                robotNode.robotRotation.unbind()
                rotationOffset =
                    Rotation2d.fromDegrees(robotNode.robotRotation.get()) - event.angleRelativeToNode()
            }
        }

        setOnMouseDragged { event ->
            if (rotating) {
                val angle = event.angleRelativeToNode()

                robotNode.robotRotation.set((angle + rotationOffset).degrees)
            }
        }

        setOnMouseReleased {
            if (rotating) {
                rotating = false
                simpleObjectProperty.value = Pose2d(
                    simpleObjectProperty.value.translation,
                    Rotation2d.fromDegrees(-robotNode.robotRotation.value)
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
        return Rotation2d(localMouseX, localMouseY)
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