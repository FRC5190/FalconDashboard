package org.ghrobotics.falcondashboard.generator.fragments

import javafx.scene.text.Font
import org.ghrobotics.falcondashboard.Settings
import org.ghrobotics.falcondashboard.generator.GeneratorView
import tornadofx.Fragment
import tornadofx.hbox
import tornadofx.paddingAll
import tornadofx.text

class CodeFragment : Fragment() {
    override val root = hbox {}

    init {

        val builder = StringBuilder()
        builder.append("val ")
        builder.append(Settings.name.value.decapitalize().replace("\\s".toRegex(), ""))
        builder.append(" = ")
        builder.append("waypoints(\n")
        GeneratorView.waypoints.toList().forEach {
            builder.append("    Pose2d(${it.translation.x.feet}.feet, ${it.translation.y.feet}.feet, ${it.rotation.degree}.degree)\n")
        }
        builder.append(
            ").generateTrajectory(\n    \"${Settings.name.value}\",\n    ${Settings.reversed.value},\n" +
                    "    ${Settings.maxVelocity.value}.feet.velocity,\n    ${Settings.maxAcceleration.value}.feet.acceleration,\n" +
                    "    listOf(CentripetalAccelerationConstraint(${Settings.maxCentripetalAcceleration.value}.feet.acceleration)\n)"
        )

        with(root) {
            title = "Generated Code"
            paddingAll = 20.0

            prefWidth = 500.0
            prefHeight = 500.0

            text(builder.toString()) {
                font = Font("Consolas", 12.0)
            }
        }
    }
}