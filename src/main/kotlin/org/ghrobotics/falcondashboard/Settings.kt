package org.ghrobotics.falcondashboard

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.GsonBuilder
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import java.io.File
import java.io.FileReader
import java.io.FileWriter


object Settings {
    val name = SimpleStringProperty("Baseline")
    var reversed = SimpleBooleanProperty(false)
    val clampedCubic = SimpleBooleanProperty(true)
    val purePursuit = SimpleBooleanProperty(true)
    val robotWidth = SimpleDoubleProperty(Properties.kRobotWidth.value)
    val robotLength = SimpleDoubleProperty(Properties.kRobotLength.value)
    val startVelocity = SimpleDoubleProperty(0.0)
    val endVelocity = SimpleDoubleProperty(0.0)
    val maxVelocity = SimpleDoubleProperty(2.0)
    val maxAcceleration = SimpleDoubleProperty(1.5)
    val maxCentripetalAcceleration = SimpleDoubleProperty(2.0)
    val ip = SimpleStringProperty("127.0.1.1")
    val trajectoryTime = SimpleStringProperty("Trajectory Time (s): ")

    private val gson = GsonBuilder().registerTypeAdapter<Settings> {
        write {
            beginArray()
            value(it.reversed.value)
            value(it.clampedCubic.value)
            value(it.purePursuit.value)
            value(it.startVelocity.value)
            value(it.endVelocity.value)
            value(it.maxVelocity.value)
            value(it.maxAcceleration.value)
            value(it.maxCentripetalAcceleration.value)
            value(it.ip.value)
            value(it.trajectoryTime.value)
            endArray()
        }
        read {
            beginArray()
            reversed.set(nextBoolean())
            clampedCubic.set(nextBoolean())
            purePursuit.set(nextBoolean())
            startVelocity.set(nextDouble())
            endVelocity.set(nextDouble())
            maxVelocity.set(nextDouble())
            maxAcceleration.set(nextDouble())
            maxCentripetalAcceleration.set(nextDouble())
            ip.set(nextString())
            trajectoryTime.set(nextString())
            endArray()
            return@read Settings
        }
    }.create()!!

    init {
        val file = File("settings.json")
        if (file.exists()) {
            try {
                gson.fromJson<Settings>(FileReader(file))
            } catch (e: Exception) {
                file.delete()
                val writer = FileWriter(file)
                writer.write(gson.toJson(Settings))
                writer.close()
            }
        } else {
            val writer = FileWriter(file)
            writer.write(gson.toJson(Settings))
            writer.close()
        }
    }

    fun save() {
        val writer = FileWriter(File("settings.json"))
        writer.write(gson.toJson(Settings))
        writer.close()
    }
}

