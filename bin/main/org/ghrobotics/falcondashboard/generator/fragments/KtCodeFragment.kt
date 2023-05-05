package org.ghrobotics.falcondashboard.generator.fragments

import javafx.scene.layout.Priority
import javafx.scene.text.Font
import kfoenix.jfxtextarea
import org.ghrobotics.falcondashboard.Settings
import org.ghrobotics.falcondashboard.generator.GeneratorView
import org.ghrobotics.lib.mathematics.twodim.geometry.x_u
import org.ghrobotics.lib.mathematics.twodim.geometry.y_u
import org.ghrobotics.lib.mathematics.units.feet
import org.ghrobotics.lib.mathematics.units.inFeet
import org.ghrobotics.lib.mathematics.units.meter
import tornadofx.*
import java.awt.Desktop
import java.net.URI
import java.text.DecimalFormat

class KtCodeFragment : Fragment() {
    override val root = vbox {

        title = "Generated Code"

        style {
            padding = box(1.em)
        }

        prefWidth = 800.0
        prefHeight = 500.0

        jfxtextarea {
            font = Font.font("Monospaced")
            isEditable = false

            vgrow = Priority.ALWAYS

            text = buildString {

//                append(
//                    "import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d\n" +
//                            "import org.ghrobotics.lib.mathematics.twodim.trajectory.DefaultTrajectoryGenerator\n" +
//                            "import org.ghrobotics.lib.mathematics.twodim.trajectory.constraints.CentripetalAccelerationConstraint\n" +
//                            "import org.ghrobotics.lib.mathematics.units.degree\n" +
//                            "import org.ghrobotics.lib.mathematics.units.derivedunits.acceleration\n" +
//                            "import org.ghrobotics.lib.mathematics.units.derivedunits.velocity\n" +
//                            "import org.ghrobotics.lib.mathematics.units.feet\n\n\n\n"
//                )

                val name = Settings.name.value.decapitalize()
                    .replace("\\s+".toRegex(), "")

                val dm = DecimalFormat("##.###")

//                append("val $name = DefaultTrajectoryGenerator.generateTrajectory(\n")
                append("wayPoints = listOf(\n")
                GeneratorView.waypoints.forEach {
                    append(
                        "    Pose2d(${dm.format(it.translation.x_u.inFeet())}.feet, " +
                                "${dm.format(it.translation.y_u.inFeet())}.feet, " +
                                "${dm.format(it.rotation.degrees)}.degrees)"
                    )
                    if (it != GeneratorView.waypoints.last()) append(",")
                    append("\n")
                }
                append("),\n")
//                append(
//                    "    constraints = listOf(CentripetalAccelerationConstraint(${Settings.maxCentripetalAcceleration.value}.feet.acceleration),\n" +
//                            "    startVelocity = 0.0.feet.velocity,\n" +
//                            "    endVelocity = 0.0.feet.velocity,\n" +
//                            "    maxVelocity = ${Settings.maxVelocity.value}.feet.velocity,\n" +
//                            "    maxAcceleration = ${Settings.maxAcceleration.value}.feet.acceleration,\n" +
//                            "    reversed = ${Settings.reversed.value}\n)"
//                )
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