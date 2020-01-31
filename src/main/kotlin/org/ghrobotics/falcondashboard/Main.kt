package org.ghrobotics.falcondashboard

import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import org.ghrobotics.falcondashboard.generator.fragments.ExitFragment
import org.ghrobotics.falcondashboard.generator.fragments.InvalidTrajectoryFragment
import tornadofx.App
import tornadofx.find
import tornadofx.launch

class Main : App(MainView::class) {
    init {
        Settings
        Network
        Saver

        TrajectoryGenerator.setErrorHandler { _, _ ->
            find<InvalidTrajectoryFragment>().openModal(StageStyle.UTILITY)
        }
    }

    override fun start(stage: Stage) {
        stage.onCloseRequest = EventHandler<WindowEvent> {
            if (Saver.hasChanged()) {
                it.consume()
                val exitFragment = find<ExitFragment>(params = *arrayOf("exit" to Runnable { stop() }))
                exitFragment.openModal(StageStyle.UTILITY, escapeClosesWindow = false, block = true)
                if (exitFragment.shouldExit)
                    Platform.exit()
            }
        }
        super.start(stage)
    }

    override fun stop() {
        Settings.save()
        super.stop()
    }
}

fun main(args: Array<String>) {
    launch<Main>(args)
}