package org.ghrobotics.falcondashboard

import javafx.scene.Parent
import org.ghrobotics.falcondashboard.generator.GeneratorView
import tornadofx.App
import tornadofx.View
import tornadofx.hbox
import tornadofx.launch

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
    override val root: Parent = hbox {
        stylesheets += resources["/FalconStyle.css"]
        add(GeneratorView())
    }
}