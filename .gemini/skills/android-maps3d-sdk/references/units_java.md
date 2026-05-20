# Unit Conversions (Java)

These utilities provide type-safe measurements in meters and conversions to other units (feet, miles, kilometers), which are useful for displaying distances in 3D map applications.

## Meters Class

A class to wrap a value representing a measurement in meters.

```java
public class Meters implements Comparable<Meters> {
    public static final double METERS_PER_FOOT = 3.28084;
    public static final double METERS_PER_KILOMETER = 1000;
    public static final double FEET_PER_METER = 1 / METERS_PER_FOOT;
    public static final double FEET_PER_MILE = 5280;
    public static final double MILES_PER_METER = 0.000621371;

    private final double value;

    public Meters(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public int compareTo(Meters other) {
        return Double.compare(this.value, other.value);
    }

    public Meters minus(Meters other) {
        return new Meters(this.value - other.value);
    }

    public Meters plus(Meters other) {
        return new Meters(this.value + other.value);
    }

    // Factory methods
    public static Meters fromMeters(double value) {
        return new Meters(value);
    }

    public static Meters fromKilometers(double km) {
        return new Meters(km * METERS_PER_KILOMETER);
    }

    public static Meters fromFeet(double feet) {
        return new Meters(feet * FEET_PER_METER);
    }

    public static Meters fromMiles(double miles) {
        return new Meters(miles / MILES_PER_METER);
    }

    // Conversions
    public double toFeet() {
        return value * METERS_PER_FOOT;
    }

    public double toKilometers() {
        return value / METERS_PER_KILOMETER;
    }

    public double toMiles() {
        return value * MILES_PER_METER;
    }
}
```

## Units Converter (Optional)

```java
public class ValueWithUnits {
    public final double value;
    public final String unitLabel;

    public ValueWithUnits(double value, String unitLabel) {
        this.value = value;
        this.unitLabel = unitLabel;
    }
}

public interface UnitsConverter {
    ValueWithUnits toDistanceUnits(Meters meters);
    ValueWithUnits toElevationUnits(Meters meters);
}

class ImperialUnitsConverter implements UnitsConverter {
    @Override
    public ValueWithUnits toDistanceUnits(Meters meters) {
        if (meters.getValue() < Meters.fromMiles(0.25).getValue()) {
            return new ValueWithUnits(meters.toFeet(), "ft");
        } else {
            return new ValueWithUnits(meters.toMiles(), "mi");
        }
    }

    @Override
    public ValueWithUnits toElevationUnits(Meters meters) {
        return new ValueWithUnits(meters.toFeet(), "ft");
    }
}

class MetricUnitsConverter implements UnitsConverter {
    @Override
    public ValueWithUnits toDistanceUnits(Meters meters) {
        if (meters.getValue() < 1000) {
            return new ValueWithUnits(meters.getValue(), "m");
        } else {
            return new ValueWithUnits(meters.toKilometers(), "km");
        }
    }

    @Override
    public ValueWithUnits toElevationUnits(Meters meters) {
        return new ValueWithUnits(meters.getValue(), "m");
    }
}
```
