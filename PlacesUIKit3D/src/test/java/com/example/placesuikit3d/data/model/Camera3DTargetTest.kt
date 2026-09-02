/*
 * Copyright 2026 Google LLC
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

package com.example.placesuikit3d.data.model

import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [Camera3DTarget].
 */
class Camera3DTargetTest {

    @Test
    fun camera3DTarget_convertsToValidCamera() {
        val target = Camera3DTarget(
            latitude = 40.0177,
            longitude = -105.2819,
            altitude = 1620.0,
            heading = 45.0,
            tilt = 60.0,
            range = 500.0,
            roll = 0.0,
        )

        val camera = target.toCamera()
        assertThat(camera.center.latitude).isEqualTo(40.0177)
        assertThat(camera.center.longitude).isEqualTo(-105.2819)
        assertThat(camera.center.altitude).isEqualTo(1620.0)
        assertThat(camera.heading).isEqualTo(45.0)
        assertThat(camera.tilt).isEqualTo(60.0)
        assertThat(camera.range).isEqualTo(500.0)
    }

    @Test
    fun camera3DTarget_fromLocation_buildsCorrectTarget() {
        val location = latLngAltitude {
            latitude = 39.9989
            longitude = -105.2828
            altitude = 1750.0
        }

        val target = Camera3DTarget.fromLocation(location, heading = 90.0, tilt = 65.0, range = 350.0)
        assertThat(target.latitude).isEqualTo(39.9989)
        assertThat(target.longitude).isEqualTo(-105.2828)
        assertThat(target.altitude).isEqualTo(1750.0)
        assertThat(target.heading).isEqualTo(90.0)
        assertThat(target.tilt).isEqualTo(65.0)
        assertThat(target.range).isEqualTo(350.0)
    }

    @Test
    fun camera3DTarget_default_hasSensibleValues() {
        val defaultTarget = Camera3DTarget.DEFAULT
        assertThat(defaultTarget.tilt).isAtLeast(45.0)
        assertThat(defaultTarget.range).isGreaterThan(1000.0)
    }
}
