package org.ghrobotics.falcondashboard.generator.charts

import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import org.ghrobotics.falcondashboard.Properties
import org.ghrobotics.falcondashboard.mapprop
import org.ghrobotics.lib.mathematics.units.Rotation2d
import tornadofx.*
import java.io.File
import java.net.URI

open class RobotNode(
    private val robotRotationProperty: ReadOnlyProperty<Rotation2d>,
    scaleProperty: ReadOnlyDoubleProperty
) : StackPane() {

    val robotRotation = SimpleDoubleProperty()

    fun bindRobotRotation() {
        robotRotation
            .bind(mapprop<Rotation2d, Number>(
                robotRotationProperty
            ) { (-value).degree })
    }

    val robotPane = object : StackPane() {
        init {
            style {
                backgroundColor = multi(Color.TRANSPARENT)
                borderColor = multi(box(Color.RED))
                borderRadius = multi(box(0.5.em))
                borderWidth = multi(box(0.25.em))
//                backgroundImage = multi(File("BBQRobot.png").toURI())
            }
            rotateProperty().bind(robotRotation)
            bindRobotRotation()
            usePrefHeight = true
            usePrefWidth = true
            prefHeightProperty()
                .bind(scaleProperty.multiply(Properties.robotWidth.feet))
            prefWidthProperty()
                .bind(scaleProperty.multiply(Properties.robotLength.feet))
        }
    }

    init {
        children.add(robotPane)
        println("RobotPane Width = " + robotPane.width)
        println("RobotPane Height = " + robotPane.height)
    }

}