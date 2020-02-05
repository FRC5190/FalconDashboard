package org.ghrobotics.falcondashboard

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.Gson
import com.google.gson.JsonObject
import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.units.derived.degrees
import org.ghrobotics.lib.mathematics.units.derived.toRotation2d
import org.ghrobotics.lib.mathematics.units.meters
import org.ghrobotics.lib.wrappers.networktables.FalconNetworkTable
import org.ghrobotics.lib.wrappers.networktables.delegate
import org.ghrobotics.lib.wrappers.networktables.get

/**
 * Helper object for sending data to Falcon Dashboard
 */
object FalconDashboard {
    private val smartDashboard = FalconNetworkTable.getTable("SmartDashboard/Falcon")
    private val logging = FalconNetworkTable.getTable("Logging/Drivetrain")

    var robotX by logging["X Position"].delegate(0.0)
    var robotY by logging["Y Position"].delegate(0.0)
    var robotHeading by logging["Odometry angle"].delegate(0.0)

    var isFollowingPath by smartDashboard["IsFollowingPath"].delegate(false)
    var pathX by smartDashboard["PathX"].delegate(0.0)
    var pathY by smartDashboard["PathY"].delegate(0.0)
    var pathHeading by smartDashboard["PathHeading"].delegate(0.0)

    var trajectory by smartDashboard["Serialized Trajectory"].delegate("")

    private val visionTargetEntry = smartDashboard["visionTargets"]
    var visionTargets: List<Pose2d>
        set(value) {
            visionTargetEntry.setStringArray(
                value.map {
                    jsonObject(
                        "x" to it.translation.x,
                        "y" to it.translation.y,
                        "angle" to it.rotation.degrees
                    ).toString()
                }.toTypedArray()
            )
        }
        get() = visionTargetEntry.getStringArray(emptyArray())
            .map {
                val data = kGson.fromJson<JsonObject>(it)
                Pose2d(
                    data["x"].asDouble.meters,
                    data["y"].asDouble.meters,
                    data["angle"].asDouble.degrees.toRotation2d()
                )
            }

    private val kGson = Gson()
}
