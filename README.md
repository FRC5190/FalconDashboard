# Falcon Dashboard
Dashboard used by FRC 5190 to generate trajectories and visualize live robot motion to debug trajectory tracking and computer vision code.
This forked version was modified for TRIGON 5990.

## Changes From Original Version
This modified version uses meters instead of feet.
It also has a saving functunallity on a per path level.
DiffrentialKinematicsConstraint has been added.
Network Tables entries have been renamed to be used with TRIGON robot code table keys.

## Pre-Requisites
JDK 11 is required to build Falcon Dashboard. Older versions will cause compilation errors.
JDK 12 will cause a runtime error

## Running
Falcon Dashboard uses the Gradle build system. Run `./gradlew run` in the root project directory.

## Sending Data to Falcon Dashboard
You can send data over NetworkTables to visualize the robot pose and trajectory data on the live visualizer. All entries are sent over the `Live_Dashboard` table.

### Robot Pose Data
 - `robotX`: The x position of the robot on the field in meters.
 - `robotY`: The y position of the robot on the field in meters.
 - `robotHeading` The heading of the robot in angles.
 
### Trajectory Data
 - `pathX`: The x position of the current reference point in meters.
 - `pathY`: The y position of the current reference point in meters.
 - `isFollowingPath`: Whether the robot is currently tracking a trajectory or not.
 
