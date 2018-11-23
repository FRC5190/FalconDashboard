package org.ghrobotics.generator.tables

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import javafx.scene.input.TransferMode
import javafx.util.converter.DoubleStringConverter
import org.ghrobotics.generator.Main
import org.ghrobotics.generator.tables.WaypointsTable.setRowFactory
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

        setRowFactory { _ ->
            val row = TableRow<Pose2d>()

            row.setOnDragDetected {
                if (!row.isEmpty) {
                    val index = row.index
                    val db = startDragAndDrop(TransferMode.MOVE)
                    db.dragView = row.snapshot(null, null)

                    val cc = ClipboardContent()
                    cc.putString(index.toString())
                    db.setContent(cc)
                    it.consume()
                }
            }

            row.setOnDragOver {
                if (it.dragboard.hasString()) {
                    if (row.index != it.dragboard.getContent(DataFormat.PLAIN_TEXT).toString().toInt()) {
                        it.acceptTransferModes(*TransferMode.COPY_OR_MOVE)
                        it.consume()
                    }
                }
                it.consume()
            }

            row.setOnDragDropped {
                val db = it.dragboard
                if (db.hasString()) {
                    val dragIndex = db.getContent(DataFormat.PLAIN_TEXT).toString().toInt()
                    val dropIndex = if (row.isEmpty) {
                        this@WaypointsTable.items.size
                    } else row.index

                    if (this@WaypointsTable.items.size > 2) {
                        it.isDropCompleted = true
                        this@WaypointsTable.items.add(dropIndex, this@WaypointsTable.items.removeAt(dragIndex))
                        it.consume()
                    } else {
                        it.isDropCompleted = true
                        this@WaypointsTable.items.reverse()
                        it.consume()
                    }

                }
            }
            return@setRowFactory row
        }
    }

    fun removeSelectedItemIfPossible() {
        val item = selectionModel.selectedItem
        if (item != null && items.size > 2) Main.waypoints.remove(item)
    }
}
