/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apidemos.java.common;

public final class UnitUtils {

    private UnitUtils() {
        // Prevent instantiation
    }

    public static final double METERS_PER_FOOT = 3.28084;
    public static final double METERS_PER_KILOMETER = 1000;
    public static final double FEET_PER_METER = 1 / METERS_PER_FOOT;
    public static final double FEET_PER_MILE = 5280;
    public static final double MILES_PER_METER = 0.000621371;

    public static Meters toMeters(Number num) {
        return new Meters(num.doubleValue());
    }

    public static Meters kmToMeters(Number num) { // Renamed from 'toKilometers'
        return new Meters(num.doubleValue() * METERS_PER_KILOMETER);
    }

    public static Meters feetToMeters(Number num) { // Renamed from 'toFeet'
        return new Meters(num.doubleValue() * (1 / METERS_PER_FOOT)); // or num.doubleValue() * FEET_PER_METER
    }

    public static Meters milesToMeters(Number num) { // Renamed from 'toMiles'
        return new Meters(num.doubleValue() / MILES_PER_METER);
    }

    public static UnitsConverter getUnitsConverter(String countryCode) {
        // TODO: Consider other countries that use imperial units for distances?
        if ("US".equals(countryCode)) {
            return ImperialUnitsConverter.INSTANCE;
        } else {
            return MetricUnitsConverter.INSTANCE;
        }
    }
}
