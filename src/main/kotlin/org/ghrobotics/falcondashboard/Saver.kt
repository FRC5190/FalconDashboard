package org.ghrobotics.falcondashboard

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.GsonBuilder
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.stage.FileChooser
import org.ghrobotics.falcondashboard.generator.GeneratorView
import tornadofx.FileChooserMode
import tornadofx.chooseFile
import java.io.File
import java.io.FileReader
import java.io.FileWriter


object Saver {
    val reversed = SimpleBooleanProperty(false)
    val startVelocity = SimpleDoubleProperty(0.0)
    val endVelocity = SimpleDoubleProperty(0.0)
    var hasLoaded = SimpleBooleanProperty(false)
    private var lastSaveLoadFile: File? = null

    private val gson = GsonBuilder().registerTypeAdapter<Saver> {
        write {
            beginArray()
            value(it.reversed.value)
            value(it.startVelocity.value)
            value(it.endVelocity.value)
            value(WaypointUtil.serializeWaypoints(GeneratorView.waypoints))
            endArray()
        }
        read {
            beginArray()
            reversed.set(nextBoolean())
            startVelocity.set(nextDouble())
            endVelocity.set(nextDouble())
            GeneratorView.waypoints.setAll(WaypointUtil.deserializeWaypoints(nextString()))
            endArray()
            return@read Saver
        }
    }.create()!!

    init {
        Settings.lastLoadFileLocation.value?.let { loadFromFile(File(it)) }
    }

    fun load() {
        val file = chooseFile(
            "Load File",
            arrayOf(FileChooser.ExtensionFilter("falcon dashboard save file", "*.fds")),
            op = {
                Settings.lastLoadFileLocation.value?.let {
                    initialDirectory = File(it).parentFile
                }
            }
        ).firstOrNull() ?: return
        lastSaveLoadFile = file
        Settings.lastLoadFileLocation.set(file.absolutePath)
        hasLoaded.set(true)
        loadFromFile(file)
    }

    private fun loadFromFile(file: File) {
        if (file.exists()) {
            try {
                gson.fromJson<Saver>(FileReader(file))
            } catch (e: Exception) {
            }
        }
    }

    fun save() {
        val file = chooseFile(
            "save",
            arrayOf(FileChooser.ExtensionFilter("Falcon dashboard save file", "*.fds")),
            FileChooserMode.Save,
            op = { initialDirectory = File(Settings.lastSaveFileLocation.value).parentFile }
        ).firstOrNull() ?: return
        saveFile(file)
        lastSaveLoadFile = file
        Settings.lastSaveFileLocation.set(file.absolutePath)
        hasLoaded.set(true)
    }

    private fun saveFile(file: File) {
        val writer = FileWriter(file)
        writer.write(gson.toJson(Saver))
        writer.close()
    }

    fun saveCurrentFile() {
        lastSaveLoadFile?.let {
            saveFile(it)
        }
    }
}
