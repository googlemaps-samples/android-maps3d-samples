// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.maps3dkotlin.markers

import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.markerOptions

/**
 * Demonstrates the use of different altitude modes for markers in a 3D map.
 *
 * This activity showcases four markers with different altitude modes:
 * - **ABSOLUTE:** Marker at a fixed altitude above sea level.
 * - **RELATIVE_TO_GROUND:** Marker at a fixed height above the ground.
 * - **CLAMP_TO_GROUND:** Marker is always attached to the ground.
 * - **RELATIVE_TO_MESH:** Marker's altitude is relative to the 3D mesh (buildings, terrain features).
 */
class MarkersActivity : SampleBaseActivity() {
    override val TAG = this::class.java.simpleName

    override val initialCamera = camera {
        center = latLngAltitude {
            latitude = 52.51974795
            longitude = 13.40715553
            altitude = 150.0
        }
        heading = 252.7
        tilt = 79.0
        range = 1500.0
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        super.onMap3DViewReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.SATELLITE)

        // Marker 1: Absolute
        googleMap3D.addMarker(markerOptions {
            position = latLngAltitude {
                latitude = 52.519605780912585
                longitude = 13.406867190588198
                altitude = 150.0
            }
            label = "Absolute (150m)"
            altitudeMode = AltitudeMode.ABSOLUTE
            isExtruded = true
            isDrawnWhenOccluded = true
            collisionBehavior = CollisionBehavior.REQUIRED_AND_HIDES_OPTIONAL
        })

        // Marker 2: Relative to Ground
        googleMap3D.addMarker(markerOptions {
            position = latLngAltitude {
                latitude = 52.519882191069016
                longitude = 13.407410777254293
                altitude = 50.0
            }
            label = "Relative to Ground (50m)"
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND
            isExtruded = true
            isDrawnWhenOccluded = true
        })

        // Marker 3: Clamped to Ground
        googleMap3D.addMarker(markerOptions {
            position = latLngAltitude {
                latitude = 52.52027645136134
                longitude = 13.408271658592406
                altitude = 0.0  // altitude is effectively ignored by CLAMP_TO_GROUND for rendering,
                // but might be relevant if you read the marker's position later.
                // For CLAMP_TO_GROUND, it's often set to 0.0.
            }
            label = "Clamped to Ground"
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND
            isExtruded = true
            isDrawnWhenOccluded = true
        })

        // Marker 4: Relative to Mesh
        googleMap3D.addMarker(markerOptions {
            position = latLngAltitude {
                latitude = 52.520835071144226
                longitude = 13.409426847943774
                altitude = 10.0 // Altitude relative to 3D mesh (buildings, terrain features)
            }
            label = "Relative to Mesh (10m)"
            altitudeMode = AltitudeMode.RELATIVE_TO_MESH
            isExtruded = true
            isDrawnWhenOccluded = true
        })
    }
}

