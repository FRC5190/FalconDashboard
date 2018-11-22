package org.ghrobotics.generator.tables

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TableView
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.converter.DoubleStringConverter
import org.ghrobotics.generator.Main
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.units.degree
import org.ghrobotics.lib.mathematics.units.feet
import tornadofx.column
import tornadofx.times
import kotlin.math.round

object WaypointsTable : TableView<Pose2d>(Main.waypoints) {

    private val columnX = column<Pose2d, Double>("X") {
        SimpleObjectProperty(round(it.value.translation.x.feet * 1E3) / 1E3)
    }

    private val columnY = column<Pose2d, Double>("Y") {
        SimpleObjectProperty(round(it.value.translation.y.feet * 1E3) / 1E3)
    }

    private val columnAngle = column<Pose2d, Double>("Angle") {
        SimpleObjectProperty(round(it.value.rotation.degree))
    }

    private val cellFactory = {
        val cell = TextFieldTableCell<Pose2d, Double>()
        cell.converter = DoubleStringConverter()
        cell
    }

    init {
        isEditable = true

        columnX
        columnY
        columnAngle

        with(columnX) {
            setCellFactory { cellFactory() }
            setOnEditCommit {
                val history = it.rowValue
                this@WaypointsTable.items[it.tablePosition.row] = Pose2d(
                    Translation2d(it.newValue.feet, history.translation.y),
                    history.rotation
                )
                this@WaypointsTable.refresh()
            }
        }
        with(columnY) {
            setCellFactory { cellFactory() }
            setOnEditCommit {
                val history = it.rowValue
                this@WaypointsTable.items[it.tablePosition.row] = Pose2d(
                    Translation2d(history.translation.x, it.newValue.feet),
                    history.rotation
                )
                this@WaypointsTable.refresh()
            }
        }
        with(columnAngle) {
            setCellFactory { cellFactory() }
            setOnEditCommit {
                val history = it.rowValue
                this@WaypointsTable.items[it.tablePosition.row] = Pose2d(
                    history.translation,
                    it.newValue.degree
                )
                this@WaypointsTable.refresh()
            }
        }


        columns.forEach {
            it.prefWidthProperty().bind(widthProperty() * 0.28)
            it.isResizable = false
        }
    }

    fun removeSelectedItemIfPossible() {
        val item = selectionModel.selectedItem
        if (item != null && items.size > 2) Main.waypoints.remove(item)
    }
}
