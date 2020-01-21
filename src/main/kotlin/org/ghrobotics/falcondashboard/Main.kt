package org.ghrobotics.falcondashboard

import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator
import javafx.stage.StageStyle
import org.ghrobotics.falcondashboard.generator.fragments.InvalidTrajectoryFragment
import tornadofx.App
import tornadofx.find
import tornadofx.launch

class Main : App(MainView::class) {
    init {
        Settings
        Network

        TrajectoryGenerator.setErrorHandler { _, _ ->  
            find<InvalidTrajectoryFragment>().openModal(StageStyle.UTILITY)
        }
    }

    override fun stop() {
        Settings.save()
    }
}

fun main(args: Array<String>) {
    launch<Main>(args)
}