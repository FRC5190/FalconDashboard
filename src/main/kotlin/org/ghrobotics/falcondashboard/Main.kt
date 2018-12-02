package org.ghrobotics.falcondashboard

import org.ghrobotics.falcondashboard.generator.GeneratorView
import tornadofx.*

class Main : App(MainView::class) {
    init {
        Network
    }

    override fun stop() {
        GeneratorView.saveSettings()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch<Main>(args)
        }
    }
}