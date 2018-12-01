package org.ghrobotics.falcondashboard

import javafx.scene.Parent
import kfoenix.jfxtabpane
import org.ghrobotics.falcondashboard.generator.GeneratorView
import org.ghrobotics.falcondashboard.livevisualizer.LiveVisualizerView
import tornadofx.*

class Main : App(MainView::class) {
    init {
        Network
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch<Main>(args)
        }
    }
}

class MainView : View("FRC 5190 Trajectory Generator") {
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