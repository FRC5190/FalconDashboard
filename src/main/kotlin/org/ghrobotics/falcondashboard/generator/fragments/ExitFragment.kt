package org.ghrobotics.falcondashboard.generator.fragments

import org.ghrobotics.falcondashboard.Saver
import tornadofx.*

class ExitFragment : Fragment() {

    var shouldExit: Boolean = false
    override val root =
        vbox {
            title = "changes were not saved"
            text("Save your changes to the file?")
            paddingAll = 20
            hbox {
                paddingAll = 10
                button {
                    text = "Yes"
                    action {
                        Saver.saveCurrentFile()
                        shouldExit = true
                        close()
                    }
                    spacing = 10.0
                }
                button {
                    text = "No"
                    action {
                        shouldExit = true
                        close()
                    }
                    spacing = 10.0
                }
                button {
                    text = "Cancel"
                    action {
                        shouldExit = false
                        close()
                    }
                    spacing = 10.0
                }
            }
        }
}