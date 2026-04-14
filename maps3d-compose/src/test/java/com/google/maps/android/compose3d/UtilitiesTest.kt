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

package com.google.maps.android.compose3d

import com.google.android.gms.maps3d.model.cameraRestriction
import com.google.maps.android.compose3d.utils.altitudeRange
import com.google.maps.android.compose3d.utils.headingRange
import com.google.maps.android.compose3d.utils.tiltRange
import com.google.maps.android.compose3d.utils.toValidCameraRestriction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UtilitiesTest {

    @Test
    fun testToValidCameraRestriction_null() {
        val restriction = null
        assertNull(restriction.toValidCameraRestriction())
    }

    @Test
    fun testToValidCameraRestriction_valid() {
        val restriction = cameraRestriction {
            minAltitude = 10.0
            maxAltitude = 100.0
            minHeading = 0.0
            maxHeading = 180.0
            minTilt = 0.0
            maxTilt = 45.0
        }
        val valid = restriction.toValidCameraRestriction()
        
        assertEquals(10.0, valid?.minAltitude)
        assertEquals(100.0, valid?.maxAltitude)
        assertEquals(0.0, valid?.minHeading)
        assertEquals(180.0, valid?.maxHeading)
        assertEquals(0.0, valid?.minTilt)
        assertEquals(45.0, valid?.maxTilt)
    }

    @Test
    fun testToValidCameraRestriction_defaults() {
        val restriction = cameraRestriction {
            // Leave fields null
        }
        val valid = restriction.toValidCameraRestriction()
        
        assertEquals(altitudeRange.start, valid?.minAltitude)
        assertEquals(altitudeRange.endInclusive, valid?.maxAltitude)
        assertEquals(headingRange.start, valid?.minHeading)
        assertEquals(headingRange.endInclusive, valid?.maxHeading)
        assertEquals(tiltRange.start, valid?.minTilt)
        assertEquals(tiltRange.endInclusive, valid?.maxTilt)
    }

    @Test
    fun testToValidCameraRestriction_swapped() {
        val restriction = cameraRestriction {
            minAltitude = 100.0
            maxAltitude = 10.0
            minHeading = 180.0
            maxHeading = 0.0
            minTilt = 45.0
            maxTilt = 0.0
        }
        val valid = restriction.toValidCameraRestriction()
        
        assertEquals(10.0, valid?.minAltitude)
        assertEquals(100.0, valid?.maxAltitude)
        assertEquals(0.0, valid?.minHeading)
        assertEquals(180.0, valid?.maxHeading)
        assertEquals(0.0, valid?.minTilt)
        assertEquals(45.0, valid?.maxTilt)
    }
}
