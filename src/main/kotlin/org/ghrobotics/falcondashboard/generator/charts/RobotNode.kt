package org.ghrobotics.falcondashboard.generator.charts

import edu.wpi.first.wpilibj.geometry.Rotation2d
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import org.ghrobotics.falcondashboard.Settings
import org.ghrobotics.falcondashboard.mapprop
import org.ghrobotics.lib.mathematics.units.inMeters
import org.ghrobotics.lib.mathematics.units.meters
import tornadofx.*

open class RobotNode(
    private val robotRotationProperty: ReadOnlyProperty<Rotation2d>,
    scaleProperty: ReadOnlyDoubleProperty
) : StackPane() {

    val robotRotation = SimpleDoubleProperty()

    fun bindRobotRotation() {
        robotRotation
            .bind(mapprop<Rotation2d, Number>(
                robotRotationProperty
            ) { (-value).degrees })
    }

    init {
        style {
            backgroundColor = multi(Color.TRANSPARENT)
            borderColor = multi(box(Color.TRANSPARENT)) // BLUE
            borderRadius = multi(box(0.5.em))
            borderWidth = multi(box(0.25.em))
        }
        rotateProperty().bind(robotRotation)
        bindRobotRotation()
        usePrefHeight = true
        usePrefWidth = true
        prefHeightProperty()
            .bind(scaleProperty.multiply(Settings.robotWidth.value.meters.inMeters()))
        prefWidthProperty()
            .bind(scaleProperty.multiply(Settings.robotLength.value.meters.inMeters()))


        // New Robot
        val polygon = Polygon()
        polygon.fill = Color.TRANSPARENT
        polygon.stroke = Color.RED
        polygon.strokeWidth = 2.5 // 0.25.em.value
        // TODO: Add transformation (translateX) (needed because of he arrow head)
        polygon.points.addAll(
            arrayOf(
                0.0,0.0,
                // -prefHeight*0.2, prefWidth/2,
                0.0, prefHeight,
                prefWidth, prefHeight,
                prefWidth*1.2, prefHeight/2,
                prefWidth, 0.0
            )
        )
        polygon.translateX = prefWidth*0.1
        children.add(polygon)



    }

}