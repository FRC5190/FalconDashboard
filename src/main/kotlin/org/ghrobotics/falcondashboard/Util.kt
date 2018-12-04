package org.ghrobotics.falcondashboard

import javafx.application.Platform
import javafx.beans.property.DoublePropertyBase
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.util.converter.NumberStringConverter
import kfoenix.jfxtextfield
import tornadofx.*

fun Parent.createNumericalEntry(name: String, property: DoublePropertyBase) = hbox {
    paddingAll = 5
    jfxtextfield {
        bind(property, converter = NumberStringConverter())
        prefWidth = 40.0
        minWidth = 40.0
    }
    text("    $name") { alignment = Pos.CENTER_LEFT }
}

fun ui(block: () -> Unit) {
    Platform.runLater(block)
}