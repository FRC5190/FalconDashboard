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
        LiveDashboard.liveDashboardTable.instance.startClient(Settings.ip.value)

        var lastRobotPose: Pose2d? = null
        var lastPathPose: Pose2d? = null

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
                lastRobotPose = null
                lastPathPose = null
            } else {
                val updateRobotPose = robotPose != lastRobotPose
                val updatePathPose = pathPose != lastPathPose

                ui {
                    if (updateRobotPose) FieldChart.addRobotPose(robotPose)
                    if (updatePathPose) FieldChart.addPathPose(pathPose)
                }

                lastRobotPose = robotPose
                lastPathPose = pathPose
            }
        }
    }
}