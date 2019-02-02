package org.ghrobotics.falcondashboard

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.github.salomonbrys.kotson.value
import com.google.gson.GsonBuilder
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Parent
import javafx.stage.StageStyle
import tornadofx.Fragment
import tornadofx.onChange
import tornadofx.text
import tornadofx.vbox
import java.io.File
import java.io.FileReader
import java.io.FileWriter


object Settings {
    val name = SimpleStringProperty("Baseline")
    val reversed = SimpleBooleanProperty(false)
    val optimize = SimpleBooleanProperty(true)
    val autoPathFinding = SimpleBooleanProperty(false)
    val startVelocity = SimpleDoubleProperty(0.0)
    val endVelocity = SimpleDoubleProperty(0.0)
    val maxVelocity = SimpleDoubleProperty(10.0)
    val maxAcceleration = SimpleDoubleProperty(4.0)
    val maxCentripetalAcceleration = SimpleDoubleProperty(4.0)
    val ip = SimpleStringProperty("127.0.1.1")

    private val gson = GsonBuilder().registerTypeAdapter<Settings> {
        write {
            beginArray()
            value(it.reversed.value)
            value(it.optimize.value)
            value(it.autoPathFinding.value)
            value(it.startVelocity.value)
            value(it.endVelocity.value)
            value(it.maxVelocity.value)
            value(it.maxAcceleration.value)
            value(it.maxCentripetalAcceleration.value)
            value(it.ip.value)
            endArray()
        }
        read {
            beginArray()
            Settings.reversed.set(nextBoolean())
            Settings.optimize.set(nextBoolean())
            Settings.autoPathFinding.set(nextBoolean())
            Settings.startVelocity.set(nextDouble())
            Settings.endVelocity.set(nextDouble())
            Settings.maxVelocity.set(nextDouble())
            Settings.maxAcceleration.set(nextDouble())
            Settings.maxCentripetalAcceleration.set(nextDouble())
            Settings.ip.set(nextString())
            endArray()
            return@read Settings
        }
    }.create()!!

    init {
        val file = File("settings.json")
        if (file.exists()) {
            gson.fromJson<Settings>(FileReader(file))
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

