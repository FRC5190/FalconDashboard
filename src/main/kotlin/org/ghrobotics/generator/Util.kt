package org.ghrobotics.generator

import javafx.beans.property.DoublePropertyBase
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.util.converter.NumberStringConverter
import tornadofx.*

fun Parent.createNumericalEntry(name: String, property: DoublePropertyBase) = hbox {
    paddingAll = 5
    textfield {
        bind(property, converter = NumberStringConverter())
        prefWidth = 50.0
    }
    text("    $name") { alignment = Pos.CENTER_LEFT }
}