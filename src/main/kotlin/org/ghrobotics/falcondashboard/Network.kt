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

        var lastIsFollowingPath = false

        var lastRobotPose: Pose2d? = null
        var lastPathPose: Pose2d? = null

        GlobalScope.launchFrequency(50) {

            ui {
                FieldChart.updateVisionTargets(LiveDashboard.visionTargets)
            }

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

            val updateRobotPose = robotPose != lastRobotPose
            if (updateRobotPose) ui { FieldChart.updateRobotPose(robotPose) }
            lastRobotPose = robotPose

            if (LiveDashboard.isFollowingPath) {
                if (!lastIsFollowingPath) {
                    // Only reset path cache when another path starts
                    ui { FieldChart.clear() }
                    lastRobotPose = null
                    lastPathPose = null
                    lastIsFollowingPath = true
                } else {
                    val updatePathPose = pathPose != lastPathPose

                    if (updatePathPose || updateRobotPose) {
                        ui {
                            if (updateRobotPose) FieldChart.addRobotPathPose(robotPose)
                            if (updatePathPose) FieldChart.addPathPose(pathPose)
                        }
                    }

                    lastPathPose = pathPose
                }
            } else {
                lastIsFollowingPath = false
            }
        }
    }
}