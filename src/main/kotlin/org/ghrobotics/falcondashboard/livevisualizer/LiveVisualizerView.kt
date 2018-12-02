package org.ghrobotics.falcondashboard.livevisualizer

import javafx.scene.input.KeyCode
import javafx.stage.StageStyle
import kfoenix.jfxtextfield
import org.ghrobotics.falcondashboard.Settings
import org.ghrobotics.falcondashboard.livevisualizer.charts.FieldChart
import tornadofx.*

class LiveVisualizerView : View() {
    override val root = vbox {
        hbox {
            paddingAll = 20
            jfxtextfield(Settings.ip.value) {
                prefWidth = 100.0
                setOnKeyPressed {
                    if (it.code == KeyCode.ENTER) {
                        Settings.ip.set(this@jfxtextfield.text)
                        object : Fragment() {
                            override val root = vbox {
                                text("Please Restart Falcon Dashboard for changes to take effect.")
                            }

                        }.openModal(stageStyle = StageStyle.UTILITY)
                    }
                }
            }
        }
        add(FieldChart)

    }
}