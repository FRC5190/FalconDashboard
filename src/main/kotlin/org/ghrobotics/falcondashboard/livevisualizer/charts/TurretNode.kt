package org.ghrobotics.falcondashboard.livevisualizer.charts
import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.geometry.Translation2d
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import org.ghrobotics.falcondashboard.Properties
import org.ghrobotics.lib.mathematics.units.inMeters
import tornadofx.*


class TurretNode(
    rotation: Rotation2d,
    turretLock: Boolean,
    scaleProperty: ReadOnlyDoubleProperty) : StackPane()
{
    init {

        style {
            backgroundColor = multi(Color.TRANSPARENT)
            borderColor = multi(box(Color.TRANSPARENT))
            borderWidth = multi(box(0.25.em))
        }
        rotate = (-rotation).degrees
        usePrefHeight = true
        usePrefWidth = true

        children.removeAll()
        val c = Circle()
        c.fill = Color.MAROON
        c.stroke = Color.TRANSPARENT
        children.add(c)
        val rect = Rectangle()
        // If Turret is locked
        if(turretLock) {
            rect.fill = Color.FORESTGREEN
        }
        else
        {
            rect.fill = Color.MAROON
        }
        rect.stroke  = Color.TRANSPARENT
        children.add(rect)

        /*
        val polygon = Polygon()
        polygon.getPoints().addAll(
            arrayOf(
                0.0, 0.0,
                20.0, 10.0,
                10.0, 20.0
            )
        )
        */


        prefHeightProperty()
            .bind(scaleProperty.multiply(Properties.kTurretSize.inMeters()))
        prefWidthProperty()
            .bind(scaleProperty.multiply(Properties.kTurretSize.inMeters()))

        c.radius = prefHeight/2
        rect.width = prefWidth*1
        rect.height = prefHeight/4
        rect.translateX = translateX+prefHeight/2


    }
}