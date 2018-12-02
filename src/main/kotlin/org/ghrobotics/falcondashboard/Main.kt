package org.ghrobotics.falcondashboard

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