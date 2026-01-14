package io.hymods.lib.utils;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;

/**
 * Utility class for mathematical and geometric operations
 */
public class MathUtils {

    /**
     * Calculates the distance between two points
     * 
     * @param  from Starting point
     * @param  to   Ending point
     * 
     * @return      The distance
     */
    public static double distance(Vector3d from, Vector3d to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calculates the squared distance between two points (faster than distance)
     * 
     * @param  from Starting point
     * @param  to   Ending point
     * 
     * @return      The squared distance
     */
    public static double distanceSquared(Vector3d from, Vector3d to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Normalizes a vector
     * 
     * @param  vector The vector to normalize
     * 
     * @return        A new normalized vector
     */
    public static Vector3d normalize(Vector3d vector) {
        double length = Math.sqrt(
            vector.getX() * vector.getX() +
                vector.getY() * vector.getY() +
                vector.getZ() * vector.getZ()
        );
        if (length == 0) {
            return new Vector3d(0, 0, 0);
        }
        return new Vector3d(vector.getX() / length, vector.getY() / length, vector.getZ() / length);
    }

    /**
     * Calculates the dot product of two vectors
     * 
     * @param  a First vector
     * @param  b Second vector
     * 
     * @return   The dot product
     */
    public static double dot(Vector3d a, Vector3d b) {
        return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
    }

    /**
     * Calculates the cross product of two vectors
     * 
     * @param  a First vector
     * @param  b Second vector
     * 
     * @return   The cross product
     */
    public static Vector3d cross(Vector3d a, Vector3d b) {
        return new Vector3d(
            a.getY() * b.getZ() - a.getZ() * b.getY(),
            a.getZ() * b.getX() - a.getX() * b.getZ(),
            a.getX() * b.getY() - a.getY() * b.getX()
        );
    }

    /**
     * Calculates the angle between two vectors in radians
     * 
     * @param  a First vector
     * @param  b Second vector
     * 
     * @return   The angle in radians
     */
    public static double angleBetween(Vector3d a, Vector3d b) {
        Vector3d normalizedA = normalize(a);
        Vector3d normalizedB = normalize(b);
        double dotProduct = dot(normalizedA, normalizedB);
        return Math.acos(Math.max(-1, Math.min(1, dotProduct)));
    }

    /**
     * Calculates the angle between two vectors in degrees
     * 
     * @param  a First vector
     * @param  b Second vector
     * 
     * @return   The angle in degrees
     */
    public static double angleBetweenDegrees(Vector3d a, Vector3d b) {
        return Math.toDegrees(angleBetween(a, b));
    }

    /**
     * Lerps between two values
     * 
     * @param  start Starting value
     * @param  end   Ending value
     * @param  t     Interpolation factor (0-1)
     * 
     * @return       The interpolated value
     */
    public static double lerp(double start, double end, double t) {
        return start + (end - start) * t;
    }

    /**
     * Lerps between two vectors
     * 
     * @param  start Starting vector
     * @param  end   Ending vector
     * @param  t     Interpolation factor (0-1)
     * 
     * @return       The interpolated vector
     */
    public static Vector3d lerp(Vector3d start, Vector3d end, double t) {
        return new Vector3d(
            lerp(start.getX(), end.getX(), t),
            lerp(start.getY(), end.getY(), t),
            lerp(start.getZ(), end.getZ(), t)
        );
    }

    /**
     * Clamps a value between min and max
     * 
     * @param  value The value to clamp
     * @param  min   Minimum value
     * @param  max   Maximum value
     * 
     * @return       The clamped value
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Converts Euler angles to a direction vector
     * 
     * @param  yaw   Yaw in radians
     * @param  pitch Pitch in radians
     * 
     * @return       Direction vector
     */
    public static Vector3d eulerToDirection(float yaw, float pitch) {
        double x = -Math.sin(yaw) * Math.cos(pitch);
        double y = -Math.sin(pitch);
        double z = Math.cos(yaw) * Math.cos(pitch);
        return new Vector3d(x, y, z);
    }

    /**
     * Converts a direction vector to Euler angles
     * 
     * @param  direction The direction vector
     * 
     * @return           Vector3f containing yaw, pitch, and roll (roll is always 0)
     */
    public static Vector3f directionToEuler(Vector3d direction) {
        Vector3d normalized = normalize(direction);
        float pitch = (float) Math.asin(-normalized.getY());
        float yaw = (float) Math.atan2(-normalized.getX(), normalized.getZ());
        return new Vector3f(yaw, pitch, 0);
    }

    /**
     * Checks if a point is inside a box
     * 
     * @param  point  The point to check
     * @param  boxMin Minimum corner of the box
     * @param  boxMax Maximum corner of the box
     * 
     * @return        true if the point is inside the box
     */
    public static boolean isPointInBox(Vector3d point, Vector3d boxMin, Vector3d boxMax) {
        return point.getX() >= boxMin.getX() && point.getX() <= boxMax.getX() &&
            point.getY() >= boxMin.getY() && point.getY() <= boxMax.getY() &&
            point.getZ() >= boxMin.getZ() && point.getZ() <= boxMax.getZ();
    }

    /**
     * Checks if a point is inside a sphere
     * 
     * @param  point  The point to check
     * @param  center Center of the sphere
     * @param  radius Radius of the sphere
     * 
     * @return        true if the point is inside the sphere
     */
    public static boolean isPointInSphere(Vector3d point, Vector3d center, double radius) {
        return distanceSquared(point, center) <= radius * radius;
    }

    /**
     * Projects a vector onto another vector
     * 
     * @param  vector The vector to project
     * @param  onto   The vector to project onto
     * 
     * @return        The projected vector
     */
    public static Vector3d project(Vector3d vector, Vector3d onto) {
        double scalar = dot(vector, onto) / dot(onto, onto);
        return new Vector3d(onto.getX() * scalar, onto.getY() * scalar, onto.getZ() * scalar);
    }

    /**
     * Reflects a vector off a surface
     * 
     * @param  vector The incident vector
     * @param  normal The surface normal
     * 
     * @return        The reflected vector
     */
    public static Vector3d reflect(Vector3d vector, Vector3d normal) {
        double dotProduct = dot(vector, normal);
        return new Vector3d(
            vector.getX() - 2 * dotProduct * normal.getX(),
            vector.getY() - 2 * dotProduct * normal.getY(),
            vector.getZ() - 2 * dotProduct * normal.getZ()
        );
    }

    /**
     * Rotates a vector around the Y axis
     * 
     * @param  vector The vector to rotate
     * @param  angle  The angle in radians
     * 
     * @return        The rotated vector
     */
    public static Vector3d rotateAroundY(Vector3d vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector3d(
            vector.getX() * cos - vector.getZ() * sin,
            vector.getY(),
            vector.getX() * sin + vector.getZ() * cos
        );
    }

    /**
     * Gets the closest point on a line to a given point
     * 
     * @param  point     The point
     * @param  lineStart Start of the line
     * @param  lineEnd   End of the line
     * 
     * @return           The closest point on the line
     */
    public static Vector3d closestPointOnLine(Vector3d point, Vector3d lineStart, Vector3d lineEnd) {
        Vector3d line = new Vector3d(
            lineEnd.getX() - lineStart.getX(),
            lineEnd.getY() - lineStart.getY(),
            lineEnd.getZ() - lineStart.getZ()
        );

        Vector3d toPoint = new Vector3d(
            point.getX() - lineStart.getX(),
            point.getY() - lineStart.getY(),
            point.getZ() - lineStart.getZ()
        );

        double t = clamp(dot(toPoint, line) / dot(line, line), 0, 1);

        return new Vector3d(
            lineStart.getX() + line.getX() * t,
            lineStart.getY() + line.getY() * t,
            lineStart.getZ() + line.getZ() * t
        );
    }

}
