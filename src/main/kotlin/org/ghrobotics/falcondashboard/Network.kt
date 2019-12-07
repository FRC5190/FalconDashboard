package org.ghrobotics.falcondashboard

import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ghrobotics.falcondashboard.livevisualizer.charts.FieldChart
import org.ghrobotics.lib.debug.FalconDashboard
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.units.feet
import org.ghrobotics.lib.wrappers.networktables.FalconNetworkTable

object Network {

    init {
        FalconNetworkTable.getTable("Live_Dashboard").instance.startClient(Settings.ip.value)

        var lastIsFollowingPath = false

        var lastRobotPose: Pose2d? = null
        var lastPathPose: Pose2d? = null

        GlobalScope.launch {
            while (isActive) {

                ui {
                    FieldChart.updateVisionTargets(FalconDashboard.visionTargets)
                }

                val robotPose = Pose2d(
                    FalconDashboard.robotX.feet,
                    FalconDashboard.robotY.feet,
                    Rotation2d(FalconDashboard.robotHeading)
                )

                val pathPose = Pose2d(
                    FalconDashboard.pathX.feet,
                    FalconDashboard.pathY.feet,
                    Rotation2d(FalconDashboard.pathHeading)
                )

                val updateRobotPose = robotPose != lastRobotPose
                if (updateRobotPose) ui { FieldChart.updateRobotPose(robotPose) }
                lastRobotPose = robotPose

                if (FalconDashboard.isFollowingPath) {
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