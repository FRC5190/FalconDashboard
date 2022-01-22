import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import org.ghrobotics.falcondashboard.Properties;

/** Add your docs here. */
public class AdaptivePurePursuitController {
    private static int m_lastClosestPointIndex;
    private static Translation2d m_lookahead;
    private static double deltaAngle;
    public AdaptivePurePursuitController() {
        deltaAngle = 0;

    }

    public double[] update(
            Trajectory trajectory, Pose2d currentRobotPose, double heading, boolean reversed) {

        final double epsilon = 1E-10;
        if (heading == 0.0) {
            heading = epsilon;
        }
        m_lookahead = calculateLookahead(trajectory, currentRobotPose);
        double curvature = calculateCurvature(currentRobotPose, m_lookahead, heading);
        double targetVel = getPointVelocity(trajectory, m_lastClosestPointIndex);
        byte negate = 1;
        if (reversed == true) {
            negate = 1;
        }
        double[] velocityArray = new double[2];
        double leftVel =
                negate
                        * (targetVel * (2.0 + (curvature * 0.71)) / 2.0); // Robot width
        double rightVel =
                negate
                        * (targetVel * (2.0 - (curvature * 0.71)) / 2.0);
        velocityArray[0] = leftVel;
        velocityArray[1] = rightVel;
        System.out.println("APPC Target Vel " + targetVel + " Left Vel: " + leftVel + " Right Vel: " + rightVel);
        return velocityArray;
    }

    public void reset() {
        m_lastClosestPointIndex = 0;
    }

    public static Translation2d calculateLookahead(Trajectory trajectory, Pose2d currentRobotPose) {
        m_lastClosestPointIndex =
                findClosestPointIndex(trajectory, currentRobotPose, m_lastClosestPointIndex);
        Translation2d lookahead = null;
        for (int i = m_lastClosestPointIndex; i < trajectory.getStates().size() - 2; i++) {

            Translation2d startPos = getPointPose(trajectory, i).getTranslation();
            Translation2d finishPos = getPointPose(trajectory, i + 1).getTranslation();
            Translation2d d = finishPos.minus(startPos);
            Translation2d f = startPos.minus(currentRobotPose.getTranslation());

            double a = calcDot(d, d);
            double b = 2 * calcDot(f, d);
            double c =
                    calcDot(f, f) - Math.pow(Properties.INSTANCE.getLookahead(), 2.0);
            double dis = (b * b) - (4 * a * c);
            if (dis < 0) {
                continue;
            } else {
                dis = Math.sqrt(dis);
                double t1 = (-b - dis) / (2 * a);
                double t2 = (-b + dis) / (2 * a);
                if (t1 >= 0 && t1 <= 1) {
                    Translation2d temp = d.times(t1);
                    lookahead = startPos.plus(temp);
                    // System.out.println("t1 lookahead : " + lookahead);
                    break;
                } else if (t2 >= 0 && t2 <= 1) {
                    Translation2d temp = d.times(t2);
                    lookahead = startPos.plus(temp);
                    // System.out.println("t2 lookahead : " + lookahead);
                    break;
                }
            }
        }
        if (lookahead == null) {

            lookahead =
                    trajectory.getStates().get(trajectory.getStates().size() - 1).poseMeters.getTranslation();
        } else {

            double distToEnd =
                    currentRobotPose
                            .getTranslation()
                            .getDistance(
                                    trajectory
                                            .getStates()
                                            .get(trajectory.getStates().size() - 1)
                                            .poseMeters
                                            .getTranslation());
            if (distToEnd < Properties.INSTANCE.getLookahead()) {
                lookahead =
                        trajectory
                                .getStates()
                                .get(trajectory.getStates().size() - 1)
                                .poseMeters
                                .getTranslation();
            }
        }
        return lookahead;
    }

    public static double calcDot(Translation2d firstVec, Translation2d secondVec) {
        return firstVec.getX() * secondVec.getX() + firstVec.getY() * secondVec.getY();
    }
    
    private static double calculateCurvature1619(Pose2d currentRobotPose, double heading){

        Translation2d delta = m_lookahead.minus(currentRobotPose.getTranslation());
        double magnitude = Math.sqrt(Math.pow(delta.getX(), 2) + Math.pow(delta.getY(), 2));
        double angle = Math.toDegrees(Math.atan2(delta.getY(), Math.abs(delta.getX()) > 0.3 ? delta.getX() : 0.3 * Math.signum(delta.getX())));

        deltaAngle = heading - angle;

        if (Math.abs(deltaAngle) > 180) deltaAngle = -Math.signum(deltaAngle) * (360 - Math.abs(deltaAngle));

        double curvature = (Math.abs(deltaAngle) > 90 ? Math.signum(deltaAngle) : Math.sin(Math.toRadians(deltaAngle))) / (magnitude / 2);

        if (Double.isInfinite(curvature) || Double.isNaN(curvature)) return 0.0;

        return curvature;

    }

    private static double calculateCurvature(
            Pose2d currentRobotPose, Translation2d lookahead, double heading) {

        double a = -Math.tan(heading);
        byte b = 1;
        double c =
                -a * currentRobotPose.getTranslation().getX() - currentRobotPose.getTranslation().getY();
        double x =
                Math.abs(a * lookahead.getX() + b * lookahead.getY() + c) / ((Math.sqrt(a * a + b * b)));
        double curvature =
                (2.0 * x) / (Math.pow(Properties.INSTANCE.getLookahead(), 2.0));
        double side =
                Math.signum(
                        Math.sin(heading) * (lookahead.getX() - currentRobotPose.getTranslation().getX())
                                - Math.cos(heading)
                                * (lookahead.getY() - currentRobotPose.getTranslation().getY()));
        System.out.println("CURV : " + curvature * side);
        return curvature * side;
    }

    

    public static int findClosestPointIndex(Trajectory trajectory, Pose2d point, int lastIndex) {
        Translation2d lastPose = getPointPose(trajectory, lastIndex).getTranslation();
        double minDistance = point.getTranslation().getDistance(lastPose);
        int index = lastIndex;
        for (int i = lastIndex; i < trajectory.getStates().size() - 1; i++) {
            double tempDist =
                    point
                            .getTranslation()
                            .getDistance(trajectory.getStates().get(i).poseMeters.getTranslation());
            if (tempDist < minDistance|| index ==0) {
                index = i;
                minDistance = tempDist;
            }
        }
        return index;
    }

    public static double getPointCurvature(Trajectory trajectory, int index) {

        return trajectory.getStates().get(index).curvatureRadPerMeter;
    }

    public static Pose2d getPointPose(Trajectory trajectory, int index) {

        return trajectory.getStates().get(index).poseMeters;
    }

    public static double getPointVelocity(Trajectory trajectory, int index) {
        System.out.println("Index: " + index + " Pose: " + trajectory.getStates().get(index).poseMeters + " Point Vel: " + trajectory.getStates().get(index).velocityMetersPerSecond);
        return trajectory.getStates().get(index).velocityMetersPerSecond;
    }
}
