package org.ghrobotics.falcondashboard

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.GsonBuilder
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.FileChooser
import tornadofx.FileChooserMode
import tornadofx.chooseFile
import java.io.File
import java.io.FileReader
import java.io.FileWriter


object Settings {
    val clampedCubic = SimpleBooleanProperty(true)
    val maxVelocity = SimpleDoubleProperty(10.0)
    val maxAcceleration = SimpleDoubleProperty(4.0)
    val trackWidth = SimpleDoubleProperty(4.0)
    val maxCentripetalAcceleration = SimpleDoubleProperty(4.0)
    val ip = SimpleStringProperty("127.0.0.1")
    val lastSaveFileLocation = SimpleStringProperty(System.getProperty("user.home"))
    val lastLoadFileLocation = SimpleStringProperty(null)

    private val gson = GsonBuilder().registerTypeAdapter<Settings> {
        write {
            beginArray()
            value(it.clampedCubic.value)
            value(it.maxVelocity.value)
            value(it.maxAcceleration.value)
            value(it.maxCentripetalAcceleration.value)
            value(it.trackWidth.value)
            value(it.ip.value)
            value(it.lastSaveFileLocation.value)
            value(it.lastLoadFileLocation.value)
            endArray()
        }
        read {
            beginArray()
            clampedCubic.set(nextBoolean())
            maxVelocity.set(nextDouble())
            maxAcceleration.set(nextDouble())
            maxCentripetalAcceleration.set(nextDouble())
            trackWidth.set(nextDouble())
            ip.set(nextString())
            lastSaveFileLocation.set(nextString())
            lastLoadFileLocation.set(nextString())
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

