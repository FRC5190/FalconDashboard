package org.ghrobotics.falcondashboard.generator.fragments

import javafx.scene.layout.Priority
import javafx.scene.text.Font
import kfoenix.jfxtextarea
import org.ghrobotics.falcondashboard.Settings
import org.ghrobotics.falcondashboard.generator.GeneratorView
import tornadofx.*
import java.awt.Desktop
import java.net.URI
import java.text.DecimalFormat

class CodeFragment : Fragment() {
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
//                        "import org.ghrobotics.lib.mathematics.twodim.trajectory.DefaultTrajectoryGenerator\n" +
//                        "import org.ghrobotics.lib.mathematics.twodim.trajectory.constraints.CentripetalAccelerationConstraint\n" +
//                        "import org.ghrobotics.lib.mathematics.units.degree\n" +
//                        "import org.ghrobotics.lib.mathematics.units.derivedunits.acceleration\n" +
//                        "import org.ghrobotics.lib.mathematics.units.derivedunits.velocity\n" +
//                        "import org.ghrobotics.lib.mathematics.units.feet\n\n\n\n"
//                )

                val dm = DecimalFormat("##.###")

                val firstX = GeneratorView.waypoints.first().translation.x.feet
                val firstY = GeneratorView.waypoints.first().translation.y.feet

                var prevX = 0.0
                var prevY = 0.0

                var initialAngle = 90
                var prevAngle = 0.0
                GeneratorView.waypoints.forEach {


                    append(
                        if(it != GeneratorView.waypoints.first()) {
                            "queueTask(add_forwards_spline -s " + dm.format(prevY) + "," + dm.format(prevX) + "," + dm.format(prevAngle) + " 2," +
                                    dm.format(firstY - it.translation.y.feet) + "," + dm.format(it.translation.x.feet - firstX) + "," +
                                    dm.format(initialAngle - it.rotation.degree) + "2,5,5,0,0"
                        }else {
                            ""
                        }
//                        "Pose2d(${dm.format( it.translation.x.feet - firstX)}.feet, " +
//                            "${dm.format(firstY - it.translation.y.feet)}.feet, " +
//                            "${dm.format(it.rotation.degree)}.degree)"
                    )
                    if(it != GeneratorView.waypoints.first()) {
                        append(";")
                        append("\n")
                    }

                    prevX = (it.translation.x.feet - firstX)
                    prevY = (firstY - it.translation.y.feet)
                    prevAngle = initialAngle - it.rotation.degree
                }
//                append("    ),\n")
//                append(
//                    "    constraints = listOf(CentripetalAccelerationConstraint(${Settings.maxCentripetalAcceleration.value}.feet.acceleration),\n" +
//                        "    startVelocity = 0.0.feet.velocity,\n" +
//                        "    endVelocity = 0.0.feet.velocity,\n" +
//                        "    maxVelocity = ${Settings.maxVelocity.value}.feet.velocity,\n" +
//                        "    maxAcceleration = ${Settings.maxAcceleration.value}.feet.acceleration,\n" +
//                        "    reversed = ${Settings.reversed.value}\n)"
//                )
            }
        }
        vbox {
            style {
                padding = box(0.5.em, 0.em, 0.em, 0.em)
            }
            add(text(" This code is generated to be added in an AutonTask with BBQLibs"))
            add(hyperlink("https://github.com/FRC2714/2019Competition") {
                setOnAction {
                    Desktop.getDesktop()
                        .browse(URI("https://github.com/FRC2714/2019Competition"))
                }
            })
        }
    }
}