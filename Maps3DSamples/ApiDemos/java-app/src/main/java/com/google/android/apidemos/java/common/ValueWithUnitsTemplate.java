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

public class ValueWithUnitsTemplate {
    public final double value;
    public final int unitsTemplate; // Using int for @StringRes

    public ValueWithUnitsTemplate(double value, int unitsTemplate) {
        this.value = value;
        this.unitsTemplate = unitsTemplate;
    }

    public double getValue() {
        return value;
    }

    public int getUnitsTemplate() {
        return unitsTemplate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueWithUnitsTemplate that = (ValueWithUnitsTemplate) o;
        return Double.compare(that.value, value) == 0 && unitsTemplate == that.unitsTemplate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unitsTemplate);
    }

    @Override
    public String toString() {
        return "ValueWithUnitsTemplate{" +
                "value=" + value +
                ", unitsTemplate=" + unitsTemplate +
                '}';
    }
}
