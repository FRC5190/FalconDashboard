package org.ghrobotics.falcondashboard

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
        prefWidth = 50.0
    }
    text("    $name") { alignment = Pos.CENTER_LEFT }
}