package org.ghrobotics.generator

import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.ContentDisplay
import javafx.stage.StageStyle
import javafx.util.converter.NumberStringConverter
import org.ghrobotics.generator.Main.Companion.name
import org.ghrobotics.generator.charts.PositionChart
import org.ghrobotics.generator.charts.VelocityChart
import org.ghrobotics.generator.fragments.CodeFragment
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
                    spacing = 5.px
                }

                hbox {
                    paddingAll = 5
                    textfield {
                        bind(Main.name)
                        prefWidth = 290.0
                    }
                }

                checkbox {
                    paddingAll = 5
                    text = "Reversed"
                    bind(Main.reversed)
                }

                createNumericalEntry("Start Velocity (f/s)", Main.startVelocity)
                createNumericalEntry("End Velocity (f/s)", Main.endVelocity)
                createNumericalEntry("Max Velocity (f/s)", Main.maxVelocity)
                createNumericalEntry("Max Acceleration (f/s/s)", Main.maxAcceleration)
                createNumericalEntry("Max Centripetal Acceleration (f/s/s)", Main.maxCentripetalAcceleration)

                button {
                    text = "Add Velocity Limit Constraint in Region"
                    maxWidth = 290.0
                }

                this += WaypointsTable

                vbox {
                    spacing = 5.0
                    button {
                        prefWidth = 290.0
                        text = "Add Waypoint"
                        action {
                            find<WaypointFragment>().openModal(stageStyle = StageStyle.UTILITY)
                        }
                    }
                    button {
                        prefWidth = 290.0
                        text = "Remove Waypoint"
                        action {
                            WaypointsTable.removeSelectedItemIfPossible()
                        }
                    }
                    button {
                        prefWidth = 290.0
                        text = "Generate"
                        action {
                            find<CodeFragment>().openModal(stageStyle = StageStyle.UTILITY)
                        }
                    }
                }
            }
        }
    }
}