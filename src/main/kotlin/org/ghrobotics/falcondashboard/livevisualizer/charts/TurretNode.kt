package org.ghrobotics.falcondashboard.livevisualizer.charts
import edu.wpi.first.wpilibj.geometry.Rotation2d
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import org.ghrobotics.falcondashboard.Properties
import org.ghrobotics.lib.mathematics.units.inMeters
import tornadofx.*
import kotlin.math.tan


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
        prefHeightProperty()
            .bind(scaleProperty.multiply(Properties.kTurretSize.inMeters()))
        prefWidthProperty()
            .bind(scaleProperty.multiply(Properties.kTurretSize.inMeters()))

        children.removeAll()
        // Circle of turret
        val c = Circle()
        c.fill = Color.BLACK
        c.stroke = Color.TRANSPARENT
        c.radius = prefHeight/2
        children.add(c)
        // Rectangle barrel of turret
        val rect = Rectangle()
        rect.fill = Color.BLACK
        rect.stroke  = Color.TRANSPARENT
        rect.width = prefWidth*1
        rect.height = prefHeight/4
        rect.translateX = translateX+prefHeight/2
        children.add(rect)

        // Triangle
        val triangle = Polygon()
        triangle.stroke = Color.TRANSPARENT
        var sightSize = scaleProperty.multiply(Properties.kCameraSight.inMeters()).value
        var centroid = sightSize*1.0/2.0
        triangle.translateX = centroid
        triangle.translateY = 0.0
        triangle.points.addAll(
            arrayOf(
                0.0, 0.0,
                sightSize, sightSize  * tan(Properties.kCameraFOV.value),
                sightSize,  -sightSize * tan(Properties.kCameraFOV.value)
            )
        )
        // If Turret is locked
        if(turretLock) {
            triangle.fill = Color.rgb(0, 255, 0, 0.2);
        }
        else
        {
            triangle.fill = Color.rgb(255, 0, 0, 0.2);
        }
        children.add(triangle)

    }
}