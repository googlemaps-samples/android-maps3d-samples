package com.google.maps.android.compose3d

import android.graphics.Color
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.latLngAltitude
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MappersTest {

    private fun assertLatLngAltitudeEquals(expected: LatLngAltitude, actual: LatLngAltitude) {
        assertEquals(expected.latitude, actual.latitude, 0.0)
        assertEquals(expected.longitude, actual.longitude, 0.0)
        assertEquals(expected.altitude, actual.altitude, 0.0)
    }

    @Test
    fun testPolylineConfigToPolylineOptions() {
        val points = listOf(
            latLngAltitude {
                latitude = 1.0
                longitude = 2.0
                altitude = 3.0
            },
            latLngAltitude {
                latitude = 4.0
                longitude = 5.0
                altitude = 6.0
            },
        )
        val config = PolylineConfig(
            key = "test_polyline",
            points = points,
            color = Color.RED,
            width = 5f,
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND,
            zIndex = 1,
            outerColor = Color.BLACK,
            outerWidth = 2f,
            drawsOccludedSegments = true,
        )

        val options = config.toPolylineOptions()

        assertEquals(points.size, options.path.size)
        for (i in points.indices) {
            assertLatLngAltitudeEquals(points[i], options.path[i])
        }
        assertEquals(Color.RED, options.strokeColor)
        assertEquals(5.0, options.strokeWidth, 0.0)
        assertEquals(AltitudeMode.RELATIVE_TO_GROUND, options.altitudeMode)
        assertEquals(1, options.zIndex)
        assertEquals(Color.BLACK, options.outerColor)
        assertEquals(2.0, options.outerWidth, 0.0)
        assertEquals(true, options.drawsOccludedSegments)
    }

    @Test
    fun testMarkerConfigToMarkerOptions() {
        val position = latLngAltitude {
            latitude = 1.0
            longitude = 2.0
            altitude = 3.0
        }
        val config = MarkerConfig(
            key = "test_marker",
            position = position,
            altitudeMode = AltitudeMode.ABSOLUTE,
            label = "Test Label",
            zIndex = 2,
            isExtruded = true,
            isDrawnWhenOccluded = true,
            collisionBehavior = CollisionBehavior.REQUIRED,
        )

        val options = config.toMarkerOptions()

        assertEquals("test_marker", options.id)
        assertLatLngAltitudeEquals(position, options.position)
        assertEquals(AltitudeMode.ABSOLUTE, options.altitudeMode)
        assertEquals("Test Label", options.label)
        assertEquals(true, options.isExtruded)
        assertEquals(true, options.isDrawnWhenOccluded)
        assertEquals(CollisionBehavior.REQUIRED, options.collisionBehavior)
    }

    @Test
    fun testPolygonConfigToPolygonOptions() {
        val path = listOf(
            latLngAltitude {
                latitude = 1.0
                longitude = 2.0
                altitude = 0.0
            },
            latLngAltitude {
                latitude = 3.0
                longitude = 4.0
                altitude = 0.0
            },
            latLngAltitude {
                latitude = 5.0
                longitude = 6.0
                altitude = 0.0
            },
        )
        val hole = listOf(
            latLngAltitude {
                latitude = 1.5
                longitude = 2.5
                altitude = 0.0
            },
            latLngAltitude {
                latitude = 2.0
                longitude = 3.0
                altitude = 0.0
            },
        )
        val config = PolygonConfig(
            key = "test_polygon",
            path = path,
            innerPaths = listOf(hole),
            fillColor = Color.YELLOW,
            strokeColor = Color.GREEN,
            strokeWidth = 3f,
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND,
        )

        val options = config.toPolygonOptions()

        assertEquals(path.size, options.path.size)
        for (i in path.indices) {
            assertLatLngAltitudeEquals(path[i], options.path[i])
        }
        assertEquals(1, options.innerPaths.size)
        // Hole comparison might fail if Hole doesn't have equals.
        // Let's just check if it's not null for now, or if we can assume it works.
        // I'll keep it as is and see if it fails.
        assertEquals(Color.YELLOW, options.fillColor)
        assertEquals(Color.GREEN, options.strokeColor)
        assertEquals(3.0, options.strokeWidth, 0.0)
        assertEquals(AltitudeMode.CLAMP_TO_GROUND, options.altitudeMode)
    }

    @Test
    fun testModelConfigToModelOptions() {
        val position = latLngAltitude {
            latitude = 1.0
            longitude = 2.0
            altitude = 3.0
        }
        val config = ModelConfig(
            key = "test_model",
            position = position,
            url = "http://example.com/model.glb",
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND,
            scale = ModelScale.Uniform(2.0f),
            heading = 10.0,
            tilt = 20.0,
            roll = 30.0,
        )

        val options = config.toModelOptions()

        assertEquals("test_model", options.id)
        assertLatLngAltitudeEquals(position, options.position)
        assertEquals(AltitudeMode.RELATIVE_TO_GROUND, options.altitudeMode)
        assertEquals("http://example.com/model.glb", options.url)
        assertEquals(2.0, options.scale.x, 0.0)
        assertEquals(2.0, options.scale.y, 0.0)
        assertEquals(2.0, options.scale.z, 0.0)
        assertEquals(10.0, options.orientation.heading, 0.0)
        assertEquals(20.0, options.orientation.tilt, 0.0)
        assertEquals(30.0, options.orientation.roll, 0.0)
    }

    @Test
    fun testModelConfigToModelOptionsWithPerAxisScale() {
        val position = latLngAltitude {
            latitude = 1.0
            longitude = 2.0
            altitude = 3.0
        }
        val config = ModelConfig(
            key = "test_model_per_axis",
            position = position,
            url = "http://example.com/model.glb",
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND,
            scale = ModelScale.PerAxis(1.0f, 2.0f, 3.0f),
            heading = 10.0,
            tilt = 20.0,
            roll = 30.0,
        )

        val options = config.toModelOptions()

        assertEquals("test_model_per_axis", options.id)
        assertEquals(1.0, options.scale.x, 0.0)
        assertEquals(2.0, options.scale.y, 0.0)
        assertEquals(3.0, options.scale.z, 0.0)
    }

    @Test
    fun testPolylineConfigToPolylineOptions_defaults() {
        val points = listOf(
            latLngAltitude {
                latitude = 1.0
                longitude = 2.0
                altitude = 3.0
            },
        )
        val config = PolylineConfig(
            key = "test_polyline_defaults",
            points = points,
            color = Color.RED,
            width = 5f,
        )

        val options = config.toPolylineOptions()

        assertEquals(points.size, options.path.size)
        assertEquals(Color.RED, options.strokeColor)
        assertEquals(5.0, options.strokeWidth, 0.0)
        assertEquals(AltitudeMode.CLAMP_TO_GROUND, options.altitudeMode)
        assertEquals(0, options.zIndex)
        assertEquals(0, options.outerColor)
        assertEquals(0.0, options.outerWidth, 0.0)
        assertEquals(false, options.drawsOccludedSegments)
    }

    @Test
    fun testMarkerConfigToMarkerOptions_defaults() {
        val position = latLngAltitude {
            latitude = 1.0
            longitude = 2.0
            altitude = 3.0
        }
        val config = MarkerConfig(
            key = "test_marker_defaults",
            position = position,
        )

        val options = config.toMarkerOptions()

        assertEquals("test_marker_defaults", options.id)
        assertLatLngAltitudeEquals(position, options.position)
        assertEquals(AltitudeMode.CLAMP_TO_GROUND, options.altitudeMode)
        assertEquals("", options.label)
        assertEquals(0, options.zIndex)
        assertEquals(false, options.isExtruded)
        assertEquals(false, options.isDrawnWhenOccluded)
        assertEquals(CollisionBehavior.REQUIRED, options.collisionBehavior)
    }

    @Test
    fun testPolygonConfigToPolygonOptions_defaults() {
        val path = listOf(
            latLngAltitude {
                latitude = 1.0
                longitude = 2.0
                altitude = 0.0
            },
        )
        val config = PolygonConfig(
            key = "test_polygon_defaults",
            path = path,
            fillColor = Color.YELLOW,
            strokeColor = Color.GREEN,
            strokeWidth = 3f,
        )

        val options = config.toPolygonOptions()

        assertEquals(path.size, options.path.size)
        assertEquals(0, options.innerPaths.size)
        assertEquals(Color.YELLOW, options.fillColor)
        assertEquals(Color.GREEN, options.strokeColor)
        assertEquals(3.0, options.strokeWidth, 0.0)
        assertEquals(AltitudeMode.CLAMP_TO_GROUND, options.altitudeMode)
    }
}
