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

import java.util.Objects;

public final class Meters implements Comparable<Meters> {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meters meters = (Meters) o;
        return Double.compare(meters.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value + " m";
    }

    public Meters minus(Meters other) {
        return new Meters(this.value - other.value);
    }

    public Meters plus(Meters other) {
        return new Meters(this.value + other.value);
    }

    public double toFeet() {
        return this.value * UnitUtils.METERS_PER_FOOT;
    }

    public double toMeters() { // Added for completeness, as per Kotlin's toMeters
        return this.value;
    }

    public double toKilometers() {
        return this.value / UnitUtils.METERS_PER_KILOMETER;
    }

    public double toMiles() {
        return this.value * UnitUtils.MILES_PER_METER;
    }
}
