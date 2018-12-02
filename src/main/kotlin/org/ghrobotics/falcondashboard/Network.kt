package org.ghrobotics.falcondashboard

import kotlinx.coroutines.GlobalScope
import org.ghrobotics.falcondashboard.livevisualizer.charts.FieldChart
import org.ghrobotics.lib.debug.LiveDashboard
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.units.feet
import org.ghrobotics.lib.mathematics.units.radian
import org.ghrobotics.lib.utils.launchFrequency

object Network {
    init {
        LiveDashboard.liveDashboardTable.instance.startClient("127.0.1.1")
        GlobalScope.launchFrequency(50) {
            val robotPose = Pose2d(
                LiveDashboard.robotX.feet,
                LiveDashboard.robotY.feet,
                LiveDashboard.robotHeading.radian
            )

            val pathPose = Pose2d(
                LiveDashboard.pathX.feet,
                LiveDashboard.pathY.feet,
                LiveDashboard.pathHeading.radian
            )

            if (LiveDashboard.pathReset) {
                LiveDashboard.pathReset = false
                ui { FieldChart.clear() }
            }

            ui {
                FieldChart.updateRobot(robotPose)
                FieldChart.updatePath(pathPose)
            }
        }
    }
}