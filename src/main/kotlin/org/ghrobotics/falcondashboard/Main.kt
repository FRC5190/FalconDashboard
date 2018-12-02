package org.ghrobotics.falcondashboard

import tornadofx.App
import tornadofx.launch

class Main : App(MainView::class) {
    init {
        Settings
        Network
    }

    override fun stop() {
        Settings.save()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch<Main>(args)
        }
    }
}