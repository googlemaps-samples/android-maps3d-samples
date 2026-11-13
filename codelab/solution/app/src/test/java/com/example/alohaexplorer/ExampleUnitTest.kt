package com.example.alohaexplorer

import com.google.android.gms.maps3d.model.latLngAltitude
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Local unit tests for the Aloha Explorer 3D Maps Codelab geometry utilities.
 */
class ExampleUnitTest {
    @Test
    fun extrudePolygon_createsBottomTopAndSideWallFaces() {
        val baseSquare = listOf(
            latLngAltitude { latitude = 21.3071; longitude = -157.8587; altitude = 0.0 },
            latLngAltitude { latitude = 21.3067; longitude = -157.8583; altitude = 0.0 },
            latLngAltitude { latitude = 21.3064; longitude = -157.8587; altitude = 0.0 },
            latLngAltitude { latitude = 21.3068; longitude = -157.8591; altitude = 0.0 }
        )

        val faces = extrudePolygon(baseSquare, 35.0)

        // 1 bottom face + 1 top face + 4 side wall faces = 6 faces
        assertEquals(6, faces.size)
        // Top face vertices should all be at altitude 35.0
        assertTrue(faces[1].all { it.altitude == 35.0 })
    }

    @Test
    fun extrudePolygon_returnsEmptyForInvalidInput() {
        val twoPoints = listOf(
            latLngAltitude { latitude = 21.3071; longitude = -157.8587; altitude = 0.0 },
            latLngAltitude { latitude = 21.3067; longitude = -157.8583; altitude = 0.0 }
        )

        assertTrue(extrudePolygon(twoPoints, 35.0).isEmpty())
        assertTrue(extrudePolygon(emptyList(), 35.0).isEmpty())
    }
}
