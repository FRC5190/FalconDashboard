package org.ghrobotics.falcondashboard

import javafx.application.Platform
import javafx.beans.property.DoublePropertyBase
import javafx.beans.property.ReadOnlyObjectPropertyBase
import javafx.beans.property.ReadOnlyProperty
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.converter.NumberStringConverter
import kfoenix.jfxtextfield
import org.ghrobotics.falcondashboard.generator.charts.PositionChart
import org.ghrobotics.falcondashboard.generator.tables.WaypointsTable
import tornadofx.*
import java.io.File


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


fun triggerWaypoints() {
    // println("Waypoints triggered")
    val loopSize = WaypointsTable.items.size
    for (idx in 0 until loopSize)
    {
        WaypointsTable.items.add(0, WaypointsTable.items[WaypointsTable.items.lastIndex])
        WaypointsTable.items.removeAt(WaypointsTable.items.lastIndex)
    }
    PositionChart.followerSeries.data.clear()

}
fun saveToJSON(text: String)
{
    val dir = File("Paths/JSON")
    dir.mkdirs()
    // Create file chooser
    val fileChooser = FileChooser()
    // val path = Paths.get("").toAbsolutePath().toFile()
    fileChooser.setInitialDirectory(dir)
    fileChooser.title = "Save File" //set the title of the Dialog window
    val defaultSaveName = "path.json"
    fileChooser.initialFileName = defaultSaveName
    fileChooser.extensionFilters.addAll(
        FileChooser.ExtensionFilter("JSON Files", "*.json")
    )
    // Open window
    val stg = Stage()
    // Get filename
    val file = fileChooser.showSaveDialog(stg)
    // Close the window
    stg.close()
    file.printWriter().use { out ->
        out.println(text)
    }
}

fun <R, T> mapprop(receiver: ReadOnlyProperty<R>, getter: ReadOnlyProperty<R>.() -> T): ReadOnlyProperty<T> =
    object : ReadOnlyObjectPropertyBase<T>() {
        override fun getName() = receiver.name
        override fun getBean() = receiver.bean

        init {
            receiver.onChange {
                fireValueChangedEvent()
            }
        }

        override fun get() = getter.invoke(receiver)
    }