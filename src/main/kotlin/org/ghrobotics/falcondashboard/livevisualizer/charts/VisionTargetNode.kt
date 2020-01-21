package org.ghrobotics.falcondashboard.livevisualizer.charts

import edu.wpi.first.wpilibj.geometry.Rotation2d
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import org.ghrobotics.falcondashboard.Properties
import org.ghrobotics.lib.mathematics.units.inFeet
import tornadofx.*

class VisionTargetNode(
    rotation: Rotation2d,
    scaleProperty: ReadOnlyDoubleProperty
) : StackPane() {

    init {
        style {
            backgroundColor = multi(Color.TRANSPARENT)
            borderColor = multi(box(Color.GREEN))
            borderWidth = multi(box(0.25.em))
        }
        rotate = (-rotation).degrees

        usePrefHeight = true
        usePrefWidth = true
        prefHeightProperty()
            .bind(scaleProperty.multiply(Properties.kTargetWidth.inFeet()))
        prefWidthProperty()
            .bind(scaleProperty.multiply(Properties.kTargetThickness.inFeet()))
    }

}