/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019-2020 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.ghrobotics.falcondashboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.wpi.first.wpilibj.geometry.Pose2d;

import java.util.Arrays;
import java.util.List;

public final class WaypointUtil {
    private static final ObjectReader READER = new ObjectMapper().readerFor(Pose2d[].class);
    private static final ObjectWriter WRITER = new ObjectMapper().writerFor(Pose2d[].class);

    private WaypointUtil() {
        throw new UnsupportedOperationException("This is a utility class!");
    }

    /**
     * Deserializes a Trajectory from PathWeaver-style JSON.
     *
     * @param json the string containing the serialized JSON
     * @return the trajectory represented by the JSON
     * @throws JsonProcessingException if deserializing the JSON fails
     */
    public static List<Pose2d> deserializeWaypoints(String json) throws JsonProcessingException {
        Pose2d[] poses = READER.readValue(json);
        return Arrays.asList(poses);
    }

    /**
     * Serializes a Trajectory to PathWeaver-style JSON.
     *
     * @param waypoints the waypoints to export
     * @return the string containing the serialized JSON
     * @throws JsonProcessingException if serializing the Trajectory fails
     */
    public static String serializeWaypoints(List<Pose2d> waypoints) throws JsonProcessingException {
        Pose2d[] values = new Pose2d[waypoints.size()];
        waypoints.toArray(values);
        return WRITER.writeValueAsString(values);
    }
}
