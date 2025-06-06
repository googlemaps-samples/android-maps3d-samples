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

public final class JavaStringUtils {
    private JavaStringUtils() {}

    public static String toCompassDirection(Double heading) {
        if (heading == null) return "N/A"; // Or some other default for null heading
        double h = heading.doubleValue();
        // Normalize heading to be between 0 and 360
        h = ((h % 360) + 360) % 360;

        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        // Each step is 45 degrees. Add 22.5 for rounding to nearest direction.
        int index = (int)Math.round(h / 45.0);
        return directions[index % 8]; // Use % 8 to handle the case where index might become 8 (for N)
    }
}
