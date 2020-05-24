package org.ghrobotics.falcondashboard

import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.ghrobotics.falcondashboard.generator.GeneratorView
import org.ghrobotics.falcondashboard.generator.charts.PositionChart
import org.ghrobotics.falcondashboard.livevisualizer.charts.FieldChart
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.units.meters
import org.ghrobotics.lib.wrappers.networktables.FalconNetworkTable
import kotlin.math.cos
import kotlin.math.sin

object Network {

    init {
        FalconNetworkTable.getTable("Live_Dashboard").instance.startClient(Settings.ip.value)

        var lastIsFollowingPath = false

        var lastRobotPose: Pose2d? = null
        var lastPathPose: Pose2d? = null
        var lastTurretPose: Pose2d? = null
        var lastTurretLock: Boolean? = null

        GlobalScope.launch {
            while (isActive) {
                ui {
                    // TODO: Find a way to erase the green little dot as well
                    if (PositionChart.isTimerFinished) {
                        triggerWaypoints()
                        PositionChart.followerSeries.data.clear()
                        PositionChart.isTimerFinished = false
                    }
                }

                /*
                ui {
                    if(GeneratorView.isInitialized)
                    {
                        triggerWaypoints()
                        GeneratorView.isInitialized = false
                        println("Network Initialized")
                    }
                }
                */

                ui {
                    FieldChart.updateVisionTargets(FalconDs.visionTargets)
                }

                val robotPose = Pose2d(
                    FalconDs.robotX.meters,
                    FalconDs.robotY.meters,
                    Rotation2d(FalconDs.robotHeading)
                )

                val pathPose = Pose2d(
                    FalconDs.pathX.meters,
                    FalconDs.pathY.meters,
                    Rotation2d(FalconDs.pathHeading)
                )

                val turretPose = Pose2d(
                    FalconDs.robotX.meters + Properties.kTurretOffsetX* cos(FalconDs.robotHeading),
                    FalconDs.robotY.meters + Properties.kTurretOffsetX* sin(FalconDs.robotHeading),
                    Rotation2d(FalconDs.turretAngle)
                )

                val isTurretLocked = FalconDs.isTurretLocked

                val updateRobotPose = robotPose != lastRobotPose
                if (updateRobotPose) ui { FieldChart.updateRobotPose(robotPose) }
                lastRobotPose = robotPose

                val updateTurretLock = isTurretLocked != lastTurretLock
                val updateTurretPose = turretPose != lastTurretPose
                if (updateTurretPose || updateTurretLock) ui { FieldChart.updateTurretPose(turretPose, isTurretLocked) }
                lastTurretPose = turretPose

                if (FalconDs.isFollowingPath) {
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