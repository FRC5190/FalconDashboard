package org.ghrobotics.falcondashboard.livevisualizer.charts

import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import org.ghrobotics.falcondashboard.Properties
import org.ghrobotics.lib.mathematics.units.Rotation2d
import tornadofx.*

class VisionTargetNode(
    private val rotation: Rotation2d,
    scaleProperty: ReadOnlyDoubleProperty
) : StackPane() {

    init {
        style {
            backgroundColor = multi(Color.TRANSPARENT)
            borderColor = multi(box(Color.GREEN))
            borderWidth = multi(box(0.25.em))
        }
        rotate = (-rotation).degree

        usePrefHeight = true
        usePrefWidth = true
        prefHeightProperty()
            .bind(scaleProperty.multiply(Properties.targetWidth.feet))
        prefWidthProperty()
            .bind(scaleProperty.multiply(Properties.targetThiccness.feet))
    }

}