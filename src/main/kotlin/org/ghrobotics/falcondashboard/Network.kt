package org.ghrobotics.falcondashboard

import org.ghrobotics.lib.debug.LiveDashboard

object Network {
    init {
        LiveDashboard.liveDashboardTable.instance.startClient("127.0.1.1")
    }
}