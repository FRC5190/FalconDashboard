package org.ghrobotics.generator

import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.stage.StageStyle
import javafx.util.converter.NumberStringConverter
import org.ghrobotics.generator.charts.PositionChart
import org.ghrobotics.generator.charts.VelocityChart
import org.ghrobotics.generator.fragments.WaypointFragment
import org.ghrobotics.generator.tables.WaypointsTable
import tornadofx.*

class MainView : View() {
    override val root: Parent = hbox { }

    init {
        title = "FRC 5190 Trajectory Generator"

        with(root) {

            this += tabpane {
                tab("Position") {
                    add(PositionChart)
                    isClosable = false
                }
                tab("Velocity") {
                    add(VelocityChart)
                    isClosable = false
                }
            }

            this += vbox {
                style {
                    paddingAll = 20.0
                    maxWidth = 300.px
                }

                createTextField("Start Velocity (f/s)", Main.startVelocity)
                createTextField("End Velocity (f/s)", Main.endVelocity)
                createTextField("Max Velocity (f/s)", Main.maxVelocity)
                createTextField("Max Acceleration (f/s/s)", Main.maxAcceleration)
                createTextField("Max Centripetal Acceleration (f/s/s)", Main.maxCentripetalAcceleration)

                this += WaypointsTable(Main.waypoints)
                button {
                    text = "Add Waypoint"
                    action {
                        find<WaypointFragment>().openModal(stageStyle = StageStyle.UTILITY)
                    }
                }
            }
        }
    }

    private fun Parent.createTextField(name: String, property: SimpleDoubleProperty) = hbox {
        paddingAll = 5
        textfield {
            bind(property, converter = NumberStringConverter())
            prefWidth = 50.0
        }
        text("    $name") { alignment = Pos.CENTER_LEFT }
    }
}