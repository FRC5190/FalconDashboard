package org.ghrobotics.generator

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.units.degree
import org.ghrobotics.lib.mathematics.units.feet
import tornadofx.App
import tornadofx.launch
import tornadofx.observable

class Main : App(MainView::class) {
    companion object {
        private val kRunContext = newSingleThreadContext("JAVAFX")

        val waypoints = arrayListOf(
            Pose2d(1.5.feet, 23.feet, 0.degree),
            Pose2d(11.5.feet, 23.feet, 0.degree)
        ).observable()

        @JvmStatic
        fun main(args: Array<String>) {
            GlobalScope.launch(kRunContext) {
                launch<Main>(args)
            }
        }
    }
}