# Camera Utilities (Java)

These utility functions help ensure that camera parameters are within acceptable ranges for the Maps 3D SDK, preventing crashes due to invalid values.

## Camera Validation

Use these static methods to sanitize camera parameters before applying them to the map.

```java
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;

public class CameraUtils {

    public static final double DEFAULT_HEADING = 0.0;
    public static final double DEFAULT_TILT = 60.0;
    public static final double DEFAULT_RANGE = 1500.0;
    public static final double DEFAULT_ROLL = 0.0;

    /**
     * Validates a LatLngAltitude object, clamping values to valid ranges.
     */
    public static LatLngAltitude toValidLocation(LatLngAltitude location) {
        if (location == null) {
            return new LatLngAltitude(0, 0, 0);
        }
        
        double lat = Math.max(-90.0, Math.min(90.0, location.getLatitude()));
        double lng = Math.max(-180.0, Math.min(180.0, location.getLongitude()));
        double alt = Math.max(0.0, Math.min(LatLngAltitude.MAX_ALTITUDE_METERS, location.getAltitude()));
        
        return new LatLngAltitude(lat, lng, alt);
    }
    
    /**
     * Validates heading, wrapping values to [0, 360).
     */
    public static double toHeading(Double heading) {
        if (heading == null) return DEFAULT_HEADING;
        return wrapIn(heading, 0.0, 360.0);
    }
    
    /**
     * Validates tilt, clamping values to [0, 90].
     */
    public static double toTilt(Double tilt) {
        if (tilt == null) return DEFAULT_TILT;
        return Math.max(0.0, Math.min(90.0, tilt));
    }
    
    /**
     * Validates roll, wrapping values to [-360, 360].
     */
    public static double toRoll(Double roll) {
        if (roll == null) return DEFAULT_ROLL;
        return wrapIn(roll, -360.0, 360.0);
    }
    
    /**
     * Validates range, clamping values to [0, 63170000].
     */
    public static double toRange(Double range) {
        if (range == null) return DEFAULT_RANGE;
        return Math.max(0.0, Math.min(63170000.0, range));
    }
    
    /**
     * Helper to wrap values within a range [lower, upper).
     */
    public static double wrapIn(double value, double lower, double upper) {
        double range = upper - lower;
        if (range <= 0) {
            throw new IllegalArgumentException("Upper bound must be greater than lower bound");
        }
        double offset = value - lower;
        return lower + (offset - Math.floor(offset / range) * range);
    }
}
```

### Usage Example

When updating the camera, use these methods to ensure values are valid:

```java
LatLngAltitude validCenter = CameraUtils.toValidLocation(currentCenter);
double validHeading = CameraUtils.toHeading(currentHeading);
double validTilt = CameraUtils.toTilt(currentTilt);

// Rebuild your camera object using these valid values...
```

## Path and Animation Utilities

These utilities help with path smoothing, simplification, heading calculation, and distance calculations.

```java
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

public class PathUtils {

    /**
     * Smooths a path of LatLng points using Chaikin's algorithm.
     */
    public static List<LatLng> smoothPath(List<LatLng> path, int iterations) {
        if (path.size() < 3 || iterations <= 0) return path;

        List<LatLng> currentPath = path;
        for (int iter = 0; iter < iterations; iter++) {
            List<LatLng> nextPath = new ArrayList<>();
            nextPath.add(currentPath.get(0));

            for (int i = 0; i < currentPath.size() - 1; i++) {
                LatLng p0 = currentPath.get(i);
                LatLng p1 = currentPath.get(i + 1);

                LatLng q = new LatLng(
                    p0.latitude * 0.75 + p1.latitude * 0.25,
                    p0.longitude * 0.75 + p1.longitude * 0.25
                );

                LatLng r = new LatLng(
                    p0.latitude * 0.25 + p1.latitude * 0.75,
                    p0.longitude * 0.25 + p1.longitude * 0.75
                );

                nextPath.add(q);
                nextPath.add(r);
            }

            nextPath.add(currentPath.get(currentPath.size() - 1));
            currentPath = nextPath;
        }

        return currentPath;
    }

    /**
     * Calculates the heading (bearing) from one LatLng to another.
     */
    public static double calculateHeading(LatLng from, LatLng to) {
        double lat1 = Math.toRadians(from.latitude);
        double lon1 = Math.toRadians(from.longitude);
        double lat2 = Math.toRadians(to.latitude);
        double lon2 = Math.toRadians(to.longitude);

        double dLon = lon2 - lon1;
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                   Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360.0) % 360.0;
    }

    /**
     * Simplifies a path of LatLng points using the Ramer-Douglas-Peucker algorithm.
     */
    public static List<LatLng> simplifyPath(List<LatLng> path, double epsilon) {
        if (path.size() < 3) return path;

        double maxDistance = 0.0;
        int index = 0;
        LatLng first = path.get(0);
        LatLng last = path.get(path.size() - 1);

        for (int i = 1; i < path.size() - 1; i++) {
            double distance = perpendicularDistance(path.get(i), first, last);
            if (distance > maxDistance) {
                index = i;
                maxDistance = distance;
            }
        }

        if (maxDistance > epsilon) {
            List<LatLng> left = simplifyPath(path.subList(0, index + 1), epsilon);
            List<LatLng> right = simplifyPath(path.subList(index, path.size()), epsilon);
            
            List<LatLng> result = new ArrayList<>(left.subList(0, left.size() - 1));
            result.addAll(right);
            return result;
        } else {
            List<LatLng> result = new ArrayList<>();
            result.add(first);
            result.add(last);
            return result;
        }
    }

    private static double perpendicularDistance(LatLng point, LatLng start, LatLng end) {
        double x = point.longitude;
        double y = point.latitude;
        double x1 = start.longitude;
        double y1 = start.latitude;
        double x2 = end.longitude;
        double y2 = end.latitude;

        double area = Math.abs((y2 - y1) * x - (x2 - x1) * y + x2 * y1 - y2 * x1);
        double bottom = Math.sqrt(Math.pow(y2 - y1, 2.0) + Math.pow(x2 - x1, 2.0));
        return area / bottom;
    }

    /**
     * Calculates the distance in meters between two [LatLng] points using the Haversine formula.
     */
    public static double haversineDistance(LatLng p1, LatLng p2) {
        double r = 6371000.0; // Earth radius in meters
        double lat1 = Math.toRadians(p1.latitude);
        double lon1 = Math.toRadians(p1.longitude);
        double lat2 = Math.toRadians(p2.latitude);
        double lon2 = Math.toRadians(p2.longitude);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.pow(Math.sin(dLat / 2), 2.0) +
                   Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return r * c;
    }

    /**
     * Standardized "Double-Wait" utility for Java.
     * Returns a CompletableFuture that completes when the map becomes steady.
     * Use this after starting a camera animation to ensure the scene is fully loaded.
     */
    public static java.util.concurrent.CompletableFuture<Boolean> awaitArrivedAndSteady(com.google.android.gms.maps3d.GoogleMap3D map) {
        java.util.concurrent.CompletableFuture<Boolean> future = new java.util.concurrent.CompletableFuture<>();
        map.setOnMapSteadyListener(isSteady -> {
            if (isSteady) {
                map.setOnMapSteadyListener(null);
                future.complete(true);
            }
        });
        return future;
    }
}

```

