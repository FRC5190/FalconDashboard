import org.gradle.internal.os.OperatingSystem
import edu.wpi.first.gradlerio.wpi.dependencies.WPIVendorDepsExtension
import edu.wpi.first.toolchain.NativePlatforms
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.3.61"
    id("edu.wpi.first.GradleRIO") version "2020.1.1-beta-3a"
    id("com.google.osdetector") version "1.6.2"
}

application {
    mainClassName = "org.ghrobotics.falcondashboard.MainKt"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Wrapper>().configureEach {
    gradleVersion = "6.0.1"
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven { setUrl("https://jitpack.io") }
    }
    dependencies {
        // Native configs for JavaFX
        createNativeConfigurations()

        // Kotlin Standard Library and Coroutines
        compile(kotlin("stdlib"))
        compile("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.1.0")

        // WPILib and Vendors
        wpi.deps.wpilib().forEach { compile(it) }

        native(
            group = "edu.wpi.first.ntcore",
            name = "ntcore-jni",
            version = wpi.wpilibVersion,
            classifierFunction = ::wpilibClassifier
        )

        compile("org.ghrobotics.FalconLibrary:core:d23c13ec24")
        compile("org.ghrobotics.FalconLibrary:wpi:d23c13ec24")

        // TornadoFX
        compile("no.tornado:tornadofx:1.7.17")

        // JavaFX
        javafx("base")
        javafx("controls")
        javafx("fxml")
        javafx("graphics")

        // Kotson
        compile("com.github.salomonbrys.kotson:kotson:2.5.0")

        // Material Theme
        compile("com.jfoenix:jfoenix:9.0.6")
        compile("com.github.bkenn:kfoenix:0.1.3")
    }
}



