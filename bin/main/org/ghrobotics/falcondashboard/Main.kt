package org.ghrobotics.falcondashboard

import edu.wpi.first.networktables.NetworkTablesJNI
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator
import edu.wpi.first.wpiutil.CombinedRuntimeLoader
import edu.wpi.first.wpiutil.WPIUtilJNI
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
    WPIUtilJNI.Helper.setExtractOnStaticLoad(false);
    NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);
    CombinedRuntimeLoader.loadLibraries(Main::class.java, "wpiutiljni", "ntcorejni")
    launch<Main>(args)
}