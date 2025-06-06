/*
 * Copyright 2025 Google LLC
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

// Meters and ValueWithUnitsTemplate are in the same package.

public final class MetricUnitsConverter extends UnitsConverter {
    public static final MetricUnitsConverter INSTANCE = new MetricUnitsConverter();
    // Placeholders for R.string resources.
    private static final int R_STRING_IN_METERS = 0x7f080003;     // Example placeholder
    private static final int R_STRING_IN_KILOMETERS = 0x7f080004; // Example placeholder

    private MetricUnitsConverter() {}

    @Override
    public ValueWithUnitsTemplate toDistanceUnits(Meters meters) {
        if (meters.getValue() < 1000.0) {
            return new ValueWithUnitsTemplate(meters.getValue(), R_STRING_IN_METERS);
        } else {
            // Convert meters to kilometers for the value
            return new ValueWithUnitsTemplate(meters.getValue() / UnitUtils.METERS_PER_KILOMETER, R_STRING_IN_KILOMETERS);
        }
    }

    @Override
    public ValueWithUnitsTemplate toElevationUnits(Meters meters) {
        return new ValueWithUnitsTemplate(meters.getValue(), R_STRING_IN_METERS);
    }
}
