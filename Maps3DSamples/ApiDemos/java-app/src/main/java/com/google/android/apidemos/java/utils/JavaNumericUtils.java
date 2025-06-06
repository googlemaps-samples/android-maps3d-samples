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

package com.google.android.apidemos.java.utils;

public final class JavaNumericUtils {
    private JavaNumericUtils() {}

    /**
     * Wraps a value within a given range [min, max).
     * If value is min, it returns min. If value is max, it returns min (exclusive max).
     * If value is outside, it wraps around.
     */
    public static float wrapIn(float value, float min, float max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        float range = max - min;
        float result = value;
        while (result < min) {
            result += range;
        }
        while (result >= max) { // If value is max, it should wrap to min
            result -= range;
        }
        return result;
    }

    public static double wrapIn(double value, double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        double range = max - min;
        double result = value;
        while (result < min) {
            result += range;
        }
        while (result >= max) { // If value is max, it should wrap to min
            result -= range;
        }
        return result;
    }
}
