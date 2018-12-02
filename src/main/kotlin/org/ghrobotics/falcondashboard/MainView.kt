package org.ghrobotics.falcondashboard

import javafx.scene.Parent
import kfoenix.jfxtabpane
import org.ghrobotics.falcondashboard.generator.GeneratorView
import org.ghrobotics.falcondashboard.livevisualizer.LiveVisualizerView
import tornadofx.View
import tornadofx.plusAssign
import tornadofx.tab

class MainView : View("FRC 5190 Falcon Dashboard") {
    override val root: Parent = jfxtabpane {
        stylesheets += resources["/AppStyle.css"]
        tab("Generator") {
            this += GeneratorView()
            isClosable = false
        }
        tab("Live Visualizer") {
            this += LiveVisualizerView()
            isClosable = false
        }
    }
}