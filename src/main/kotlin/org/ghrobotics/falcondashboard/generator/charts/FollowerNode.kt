package org.ghrobotics.falcondashboard.generator.charts

import edu.wpi.first.wpilibj.geometry.Rotation2d
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import org.ghrobotics.falcondashboard.Properties
import org.ghrobotics.lib.mathematics.units.inMeters
import tornadofx.*

// TODO: Fix little green dot
class FollowerNode (
    rotation: Rotation2d,
    scaleProperty: ReadOnlyDoubleProperty) : StackPane() {
    init {

        style {
            backgroundColor = multi(Color.TRANSPARENT)
            borderColor = multi(box(Color.TRANSPARENT))
            // borderWidth = multi(box(0.25.em))
        }
        rotate = (-rotation).degrees
        usePrefHeight = true
        usePrefWidth = true
        // TODO: Check why this is reversed
        prefHeightProperty()
            .bind(scaleProperty.multiply(Properties.kRobotWidth.inMeters()))
        prefWidthProperty()
            .bind(scaleProperty.multiply(Properties.kRobotLength.inMeters()))

        children.removeAll()
        val polygon = Polygon()
        polygon.fill = Color.TRANSPARENT
        polygon.stroke = Color.YELLOW
        polygon.strokeWidth = 2.5 // 0.25.em.value
        // TODO: Add transformation (translateX) (needed because of the arrow head)
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