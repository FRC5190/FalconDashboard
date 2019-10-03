package org.ghrobotics.falcondashboard

import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ghrobotics.falcondashboard.livevisualizer.charts.FieldChart
import org.ghrobotics.lib.debug.LiveDashboard
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.units.feet

object Network {

    init {
        LiveDashboard.liveDashboardTable.instance.startClient(Settings.ip.value)

        var lastIsFollowingPath = false

        var lastRobotPose: Pose2d? = null
        var lastPathPose: Pose2d? = null

        GlobalScope.launch {
            while (isActive) {

                ui {
                    FieldChart.updateVisionTargets(LiveDashboard.visionTargets)
                }

                val robotPose = Pose2d(
                    LiveDashboard.robotX.feet,
                    LiveDashboard.robotY.feet,
                    Rotation2d(LiveDashboard.robotHeading)
                )

                val pathPose = Pose2d(
                    LiveDashboard.pathX.feet,
                    LiveDashboard.pathY.feet,
                    Rotation2d(LiveDashboard.pathHeading)
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
                delay(20)
            }
        }
    }
}