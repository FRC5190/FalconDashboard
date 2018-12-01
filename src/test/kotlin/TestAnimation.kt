import TestTrajectory.trajectory
import com.team254.lib.physics.DCMotorTransmission
import com.team254.lib.physics.DifferentialDrive
import edu.wpi.first.networktables.NetworkTableInstance
import org.ghrobotics.lib.debug.LiveDashboard
import org.ghrobotics.lib.mathematics.twodim.control.RamseteController
import org.ghrobotics.lib.mathematics.twodim.control.TrajectoryFollower
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Twist2d
import org.ghrobotics.lib.mathematics.twodim.trajectory.DefaultTrajectoryGenerator
import org.ghrobotics.lib.mathematics.twodim.trajectory.constraints.AngularAccelerationConstraint
import org.ghrobotics.lib.mathematics.twodim.trajectory.constraints.CentripetalAccelerationConstraint
import org.ghrobotics.lib.mathematics.twodim.trajectory.constraints.DifferentialDriveDynamicsConstraint
import org.ghrobotics.lib.mathematics.units.*
import org.ghrobotics.lib.mathematics.units.derivedunits.acceleration
import org.ghrobotics.lib.mathematics.units.derivedunits.velocity
import org.ghrobotics.lib.mathematics.units.derivedunits.volt
import java.lang.Thread.sleep
import kotlin.math.pow

fun main(args: Array<String>) {

    LiveDashboard.liveDashboardTable.instance.startServer()

    LiveDashboard.pathReset = true

    lateinit var trajectoryFollower: TrajectoryFollower
    val kBeta = 2.0
    val kZeta = 0.85

    val iterator = TestTrajectory.trajectory.iterator()
    trajectoryFollower = RamseteController(TestTrajectory.drive, kBeta, kZeta)
    trajectoryFollower.resetTrajectory(trajectory)

    val error = Pose2d(1.feet, 50.inch, 5.degree)
    var totalpose = iterator.currentState.state.state.pose.transformBy(error)

    var time = 0.second
    val dt = 20.millisecond

    val xList = arrayListOf<Double>()
    val yList = arrayListOf<Double>()

    val refXList = arrayListOf<Double>()
    val refYList = arrayListOf<Double>()

    while (!iterator.isDone) {

        val pt = iterator.advance(dt)
        val output = trajectoryFollower.getOutputFromKinematics(totalpose, time)

        val wheelstate = DifferentialDrive.WheelState(
            output.leftSetPoint * dt / 3.inch,
            output.rightSetPoint * dt / 3.inch
        )

        val k = TestTrajectory.drive.solveForwardKinematics(wheelstate)

        time += dt

        totalpose += Twist2d(
            k.linear.meter,
            0.meter,
            k.angular.radian * 1.05
        ).asPose

        LiveDashboard.robotX = totalpose.translation.x.feet
        LiveDashboard.robotY = totalpose.translation.y.feet
        LiveDashboard.robotHeading = totalpose.rotation.radian

        LiveDashboard.pathX = pt.state.state.pose.translation.x.feet
        LiveDashboard.pathY = pt.state.state.pose.translation.y.feet

        sleep(20)

    }
}

object TestTrajectory {
        private const val kRobotLinearInertia = 60.0 // kg
        private const val kRobotAngularInertia = 10.0 // kg m^2
        private const val kRobotAngularDrag = 12.0  // N*m / (rad/sec)
        private const val kDriveVIntercept = 1.055  // V
        private const val kDriveKv = 0.135  // V per rad/s
        private const val kDriveKa = 0.012  // V per rad/s^2

        private val kDriveWheelRadiusInches = 3.0.inch
        private val kWheelBaseDiameter = 29.5.inch

        private val transmission = DCMotorTransmission(
            speedPerVolt = 1 / kDriveKv,
            torquePerVolt = kDriveWheelRadiusInches.value.pow(2) * kRobotLinearInertia / (2.0 * kDriveKa),
            frictionVoltage = kDriveVIntercept
        )

        val drive = DifferentialDrive(
            mass = kRobotLinearInertia,
            moi = kRobotAngularInertia,
            angularDrag = kRobotAngularDrag,
            wheelRadius = kDriveWheelRadiusInches.value,
            effectiveWheelBaseRadius = kWheelBaseDiameter.value / 2.0,
            leftTransmission = transmission,
            rightTransmission = transmission
        )

        private val kMaxCentripetalAcceleration = 4.feet.acceleration
        private val kMaxAcceleration = 4.feet.acceleration
        private val kMaxAngularAcceleration = 2.radian.acceleration
        private val kMaxVelocity = 10.feet.velocity

        private const val kTolerance = 0.1

        private val kSideStart = Pose2d(1.54.feet, 23.234167.feet, 180.degree)
        private val kNearScaleEmpty = Pose2d(23.7.feet, 20.2.feet, 160.degree)

        val trajectory = DefaultTrajectoryGenerator.generateTrajectory(
            listOf(
                kSideStart,
                kSideStart + Pose2d((-13).feet, 0.feet, 0.degree),
                kSideStart + Pose2d((-19.5).feet, 5.feet, (-90).degree),
                kSideStart + Pose2d((-19.5).feet, 14.feet, (-90).degree),
                kNearScaleEmpty.mirror
            ),
            listOf(
                CentripetalAccelerationConstraint(kMaxCentripetalAcceleration),
                DifferentialDriveDynamicsConstraint(drive, 9.0.volt),
                AngularAccelerationConstraint(kMaxAngularAcceleration)
            ),
            0.0.feet.velocity,
            0.0.feet.velocity,
            kMaxVelocity,
            kMaxAcceleration,
            true
        )
    }
