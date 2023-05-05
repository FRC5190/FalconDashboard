package org.ghrobotics.falcondashboard.generator.tables

import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import javafx.scene.input.TransferMode
import javafx.util.converter.DoubleStringConverter
import org.ghrobotics.falcondashboard.generator.GeneratorView
import org.ghrobotics.falcondashboard.generator.tables.WaypointsTable.setRowFactory
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d
import org.ghrobotics.lib.mathematics.twodim.geometry.x_u
import org.ghrobotics.lib.mathematics.twodim.geometry.y_u
import org.ghrobotics.lib.mathematics.units.derived.degrees
import org.ghrobotics.lib.mathematics.units.feet
import org.ghrobotics.lib.mathematics.units.inFeet
import tornadofx.column
import kotlin.math.round

object WaypointsTable : TableView<Pose2d>(GeneratorView.waypoints) {

    private val columnX = column<Pose2d, Double>("X") {
        SimpleObjectProperty(round(it.value.translation.x_u.inFeet() * 1E3) / 1E3)
    }

    private val columnY = column<Pose2d, Double>("Y") {
        SimpleObjectProperty(round(it.value.translation.y_u.inFeet() * 1E3) / 1E3)
    }

    private val columnAngle = column<Pose2d, Double>("Angle") {
        SimpleObjectProperty(round(it.value.rotation.degrees))
    }

    private val cellFactory = {
        val cell = TextFieldTableCell<Pose2d, Double>()
        cell.converter = DoubleStringConverter()
        cell
    }

    init {
        isEditable = true

        columnResizePolicy = CONSTRAINED_RESIZE_POLICY

        columns.forEach {
            it.isSortable = false
            it.isReorderable = false
        }

        with(columnX) {
            setCellFactory { cellFactory() }
            setOnEditCommit {
                val history = it.rowValue
                this@WaypointsTable.items[it.tablePosition.row] = Pose2d(
                    Translation2d(it.newValue.feet, history.translation.y_u),
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
                    Translation2d(history.translation.x_u, it.newValue.feet),
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
                    Rotation2d.fromDegrees(it.newValue)
                )
                this@WaypointsTable.refresh()
            }
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
                        this@WaypointsTable.items.add(dropIndex, this@WaypointsTable.items.removeAt(dragIndex))
                    } else {
                        this@WaypointsTable.items.reverse()
                    }
                    it.isDropCompleted = true
                    it.consume()
                }
            }
            return@setRowFactory row
        }
    }

    fun loadFromText(text: String) {
        val lines = text.lines()

        val poses: List<Pose2d?> = lines.map {
            if(it.isEmpty()) return@map null
            var trim = it
                .replace(" ", "")
                .let { it2 -> if(it2.last() == ',') it2.substring(0, it2.length - 1) else it2 }
                .let { it2 -> if(!it2.startsWith("Pose2d", true)) null else it2 } ?: return@map null

            // so at this point all of our text starts with Pose2d and ends with a closing paren.
            // start by removing the starting and closing parenthesis

            trim = trim.substring(7, trim.length- 1)
            val x = trim.substring(0, trim.indexOf(".feet"))
                .let { it2 ->
                    if(it2.startsWith("(") || it2.endsWith(")")) {
                        return@let it2.substring(1, it2.length - 1)
                    } else it2
                }
                .toDouble()
            val trimNoX = trim.substring(trim.indexOf(".feet") + 6, trim.length)
            val y = trimNoX.substring(0, trimNoX.indexOf(".feet"))
                .let { it2 ->
                    if(it2.startsWith("(") || it2.endsWith(")")) {
                        return@let it2.substring(1, it2.length - 1)
                    } else it2
                }
                .toDouble()
            val trimNoY = trimNoX.substring(trimNoX.indexOf(".feet") + 6, trimNoX.length)
            val theta: Double = trimNoY.let { noY ->
                val index: Int = noY.indexOf(".degrees").let { ret ->
                    if(ret < 0) noY.indexOf(".degree") else ret
                }
                val numberWithMaybeParens = noY.substring(0, index)
                if(numberWithMaybeParens.startsWith("(") || numberWithMaybeParens.endsWith(")")) {
                    return@let numberWithMaybeParens.substring(1, numberWithMaybeParens.length - 1).toDouble()
                }
                return@let numberWithMaybeParens.toDouble()
            }
            val pose = Pose2d(x.feet, y.feet, theta.degrees)
            pose
        }
        GeneratorView.waypoints.setAll(poses.filterNotNull())
    }

    fun removeSelectedItemIfPossible() {
        val item = selectionModel.selectedItem
        if (item != null && items.size > 2) GeneratorView.waypoints.remove(item)
    }
}
