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

import android.content.res.Resources;
// ValueWithUnitsTemplate and Meters are in the same package, so direct import is not strictly needed
// but can be added for clarity if preferred.
// import com.google.android.apidemos.java.common.ValueWithUnitsTemplate;
// import com.google.android.apidemos.java.common.Meters;

public abstract class UnitsConverter {
    public abstract ValueWithUnitsTemplate toDistanceUnits(Meters meters);
    public abstract ValueWithUnitsTemplate toElevationUnits(Meters meters);

    public String toDistanceString(Resources resources, Meters meters) {
        ValueWithUnitsTemplate result = toDistanceUnits(meters);
        return resources.getString(result.getUnitsTemplate(), result.getValue());
    }

    public String toElevationString(Resources resources, Meters meters) {
        ValueWithUnitsTemplate result = toElevationUnits(meters);
        return resources.getString(result.getUnitsTemplate(), result.getValue());
    }
}
