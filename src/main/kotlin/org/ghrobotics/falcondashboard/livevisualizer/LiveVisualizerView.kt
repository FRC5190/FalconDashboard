package org.ghrobotics.falcondashboard.livevisualizer

import org.ghrobotics.falcondashboard.livevisualizer.charts.FieldChart
import tornadofx.View

class LiveVisualizerView : View() {
    override val root = FieldChart
}