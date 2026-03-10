package com.example.maps3djava.markers.data;

public class Monster {
    public final String id;
    public final String label;
    public final double latitude;
    public final double longitude;
    public final double altitude;
    public final double heading;
    public final double tilt;
    public final double range;
    public final double markerLatitude;
    public final double markerLongitude;
    public final double markerAltitude;
    public final String drawable;
    public final int altitudeMode;

    public Monster(
            String id,
            String label,
            double latitude,
            double longitude,
            double altitude,
            double heading,
            double tilt,
            double range,
            double markerLatitude,
            double markerLongitude,
            double markerAltitude,
            String drawable,
            int altitudeMode) {
        this.id = id;
        this.label = label;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.heading = heading;
        this.tilt = tilt;
        this.range = range;
        this.markerLatitude = markerLatitude;
        this.markerLongitude = markerLongitude;
        this.markerAltitude = markerAltitude;
        this.drawable = drawable;
        this.altitudeMode = altitudeMode;
    }
}
