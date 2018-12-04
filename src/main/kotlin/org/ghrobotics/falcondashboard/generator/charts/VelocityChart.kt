package org.ghrobotics.falcondashboard.generator.charts

import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.ghrobotics.falcondashboard.generator.GeneratorView
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2dWithCurvature
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TimedEntry
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TimedTrajectory
import org.ghrobotics.lib.mathematics.twodim.trajectory.types.TrajectorySamplePoint
import org.ghrobotics.lib.mathematics.units.derivedunits.feetPerSecond
import org.ghrobotics.lib.mathematics.units.second
import tornadofx.MultiValue
import tornadofx.data
import tornadofx.style
import kotlin.math.absoluteValue

object VelocityChart : LineChart<Number, Number>(NumberAxis(), NumberAxis()) {


    init {
        style {
            backgroundColor = MultiValue(arrayOf<Paint>(Color.LIGHTGRAY))
        }

        setMinSize(54 * 25.0, 27 * 25.0)

        axisSortingPolicy = LineChart.SortingPolicy.NONE
        isLegendVisible = false
        createSymbols = false
        animated = false

        update(GeneratorView.trajectory.value)
        GeneratorView.trajectory.addListener { _, _, newValue -> update(newValue) }
    }

    fun update(trajectory: TimedTrajectory<Pose2dWithCurvature>) {
        data.clear()

        val seriesVelocity = XYChart.Series<Number, Number>()

        with(seriesVelocity) {
            val iterator = trajectory.iterator()

            while (!iterator.isDone) {
                val point: TrajectorySamplePoint<TimedEntry<Pose2dWithCurvature>> = iterator.advance(0.02.second)
                data(point.state.t.second, point.state.velocity.feetPerSecond.absoluteValue)
            }
            this@VelocityChart.data.add(this)
        }
    }
}