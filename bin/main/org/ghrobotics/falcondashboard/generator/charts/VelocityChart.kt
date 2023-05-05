package org.ghrobotics.falcondashboard.generator.charts

import edu.wpi.first.wpilibj.trajectory.Trajectory
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.ghrobotics.falcondashboard.generator.GeneratorView
import tornadofx.MultiValue
import tornadofx.data
import tornadofx.style
import kotlin.math.abs

object VelocityChart : LineChart<Number, Number>(NumberAxis(), NumberAxis()) {


    init {
        style {
            backgroundColor = MultiValue(arrayOf<Paint>(Color.LIGHTGRAY))
        }

        setMinSize(54 * 25.0, 27 * 25.0)

        axisSortingPolicy = SortingPolicy.NONE
        isLegendVisible = false
        createSymbols = false
        animated = false

        update(GeneratorView.trajectory.value)
        GeneratorView.trajectory.addListener { _, _, newValue -> update(newValue) }
    }

    private fun update(trajectory: Trajectory) {
        data.clear()

        val seriesVelocity = Series<Number, Number>()

        with(seriesVelocity) {
            val duration = trajectory.totalTimeSeconds
            var t = 0.0
            val dt = 0.02

            while (t <= duration) {
                val point = trajectory.sample(t)
                t += dt
                data(point.timeSeconds, abs(point.velocityMetersPerSecond) * 3.2808)
            }
            this@VelocityChart.data.add(this)
        }
    }
}