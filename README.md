# Falcon Dashboard
Dashboard used by FRC 5190 to generate trajectories and visualize live robot motion to debug trajectory tracking and computer vision code.

## Pre-Requisites
JDK 11 or higher is required to build Falcon Dashboard. Older versions will cause compilation errors.

## Running
Falcon Dashboard uses the Gradle build system. Run `./gradlew run` in the root project directory.

## Sending Data to Falcon Dashboard
You can send data over NetworkTables to visualize the robot pose and trajectory data on the live visualizer. All entries are sent over the `Live_Dashboard` table.

### Robot Pose Data
 - `robotX`: The x position of the robot on the field in feet.
 - `robotY`: The y position of the robot on the field in feet.
 - `robotHeading` The heading of the robot in radians.
 
### Trajectory Data
 - `pathX`: The x position of the current reference point in feet.
 - `pathY`: The y position of the current reference point in feet.
 - `isFollowingPath`: Whether the robot is currently tracking a trajectory or not.
 
### Vision Targets
 - `visionTargets`: A list of `Pose2d` objects representing the vision targets in the field's coordinate system.
 
