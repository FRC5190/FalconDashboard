package org.ghrobotics.falcondashboard.generator.fragments

import javafx.scene.layout.Priority
import kfoenix.jfxtextarea
import org.ghrobotics.falcondashboard.Settings
import org.ghrobotics.falcondashboard.generator.GeneratorView
import tornadofx.*
import java.awt.Desktop
import java.net.URI

class CodeFragment : Fragment() {
    override val root = vbox {

        title = "Generated Code"

        style {
            padding = box(1.em)
        }

        prefWidth = 500.0
        prefHeight = 500.0

        jfxtextarea {
            isEditable = false

            vgrow = Priority.ALWAYS

            text = buildString {
                val name = Settings.name.value.decapitalize()
                    .replace("\\s+".toRegex(), "")

                append("val $name = waypoints(\n")
                GeneratorView.waypoints.forEach {
                    append("    Pose2d(${it.translation.x.feet}.feet, ${it.translation.y.feet}.feet, ${it.rotation.degree}.degree)\n")
                }
                append(
                    ").generateTrajectory(\n" +
                        "    \"${Settings.name.value}\",\n" +
                        "    ${Settings.reversed.value},\n" +
                        "    ${Settings.maxVelocity.value}.feet.velocity,\n" +
                        "    ${Settings.maxAcceleration.value}.feet.acceleration,\n" +
                        "    listOf(CentripetalAccelerationConstraint(${Settings.maxCentripetalAcceleration.value}.feet.acceleration)" +
                        "\n)"
                )
            }
        }
        vbox {
            style {
                padding = box(0.5.em, 0.em, 0.em, 0.em)
            }
            add(text(" This code is generated to be used with FalconLibrary"))
            add(hyperlink("https://github.com/5190GreenHopeRobotics/FalconLibrary") {
                setOnAction {
                    Desktop.getDesktop()
                        .browse(URI("https://github.com/5190GreenHopeRobotics/FalconLibrary"))
                }
            })
        }
    }
}