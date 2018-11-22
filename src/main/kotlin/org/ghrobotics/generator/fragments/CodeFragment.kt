package org.ghrobotics.generator.fragments

import javafx.scene.text.Font
import org.ghrobotics.generator.Main
import tornadofx.Fragment
import tornadofx.hbox
import tornadofx.paddingAll
import tornadofx.text

class CodeFragment : Fragment() {
    override val root = hbox {}

    init {

        val builder = StringBuilder()
        builder.append("val ")
        builder.append(Main.name.value.decapitalize().replace("\\s".toRegex(), ""))
        builder.append(" = ")
        builder.append("waypoints(\n")
        Main.waypoints.toList().forEach {
            builder.append("    Pose2d(${it.translation.x.feet}.feet, ${it.translation.y.feet}.feet, ${it.rotation.degree}.degree)\n")
        }
        builder.append(
            ").generateTrajectory(\n    \"${Main.name.value}\",\n    ${Main.reversed.value},\n" +
                    "    ${Main.maxVelocity.value}.feet.velocity,\n    ${Main.maxAcceleration.value}.feet.acceleration,\n" +
                    "    listOf(CentripetalAccelerationConstraint(${Main.maxCentripetalAcceleration.value}.feet.acceleration)\n)"
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