package org.ghrobotics.falcondashboard

import com.sun.javafx.application.LauncherImpl
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
}

fun main(args: Array<String>) {
    LauncherImpl.launchApplication(Main::class.java, args)
}