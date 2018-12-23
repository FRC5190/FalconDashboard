package org.ghrobotics.falcondashboard.generator.charts

import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import org.ghrobotics.falcondashboard.Properties
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import tornadofx.*

class PositionNode(
    private val simpleObjectProperty: SimpleObjectProperty<Pose2d>,
    scaleProperty: ReadOnlyDoubleProperty
) : StackPane() {

    val selectedProperty = SimpleBooleanProperty(false)

    init {
        style {
            backgroundColor = multi(Color.TRANSPARENT)
            borderColor = multi(box(Color.BLUE))
            borderRadius = multi(box(0.5.em))
            borderWidth = multi(box(0.25.em))
        }
        updateOrientation()
        simpleObjectProperty.addListener { _, _, _ -> updateOrientation() }
        selectedProperty.addListener { _, _, newValue ->
            println("SELECTED $newValue")
        }
        updateScale(scaleProperty.get())
        scaleProperty.addListener { _, _, newScale -> updateScale(newScale.toDouble()) }
    }

    private fun updateScale(newScale: Double) {
        minWidth = Properties.robotLength.feet * newScale
        minHeight = Properties.robotWidth.feet * newScale
    }

    fun updateOrientation() {
        val rotation = simpleObjectProperty.get().rotation
        rotate = -rotation.degree
    }

}