/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright 2019, Green Hope Falcons
 */

package org.ghrobotics.falcondashboard

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.Gson
import com.google.gson.JsonObject
import edu.wpi.first.wpilibj.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.units.derived.degrees
import org.ghrobotics.lib.mathematics.units.derived.toRotation2d
import org.ghrobotics.lib.mathematics.units.meters
import org.ghrobotics.lib.wrappers.networktables.FalconNetworkTable
import org.ghrobotics.lib.wrappers.networktables.delegate
import org.ghrobotics.lib.wrappers.networktables.get

@Deprecated("This usage will no longer be supported. Use the FalconDashboard object instead.")
typealias LiveDashboard = FalconDs

/**
 * Helper object for sending data to Falcon Dashboard
 */
object FalconDs {
    private val falconDashboardTable = FalconNetworkTable.getTable("Live_Dashboard")

    // TODO: Change default values back to 0
    var robotX by falconDashboardTable["robotX"].delegate(5.0)
    var robotY by falconDashboardTable["robotY"].delegate(5.0)
    var robotHeading by falconDashboardTable["robotHeading"].delegate(1.0)

    var turretAngle by falconDashboardTable["turretYaw"].delegate(1.5)

    var isFollowingPath by falconDashboardTable["isFollowingPath"].delegate(false)
    var pathX by falconDashboardTable["pathX"].delegate(0.0)
    var pathY by falconDashboardTable["pathY"].delegate(0.0)
    var pathHeading by falconDashboardTable["pathHeading"].delegate(0.0)

    private val visionTargetEntry = falconDashboardTable["visionTargets"]
    var visionTargets: List<Pose2d>
        set(value) {
            var setArray = DoubleArray(3)
            setArray.set(0, value.get(0).translation.x)
            setArray.set(1, value.get(0).translation.y)
            setArray.set(2, value.get(0).rotation.degrees)
            visionTargetEntry.setDoubleArray(setArray)
        }
        get() {
            val poses= mutableListOf<Pose2d>()
            val defaultTarget = doubleArrayOf(3.0, 3.0, 0.0) // (3.0,3.0,0.0)
            var arr = visionTargetEntry.getDoubleArray(defaultTarget)
            var pose = Pose2d(
                arr[0].meters,
                arr[1].meters,
                arr[2].degrees.toRotation2d()
            )
            poses.add(pose)
            return poses.toList()
        }
        /*
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
         */


    private val kGson = Gson()
}
