package com.example.advancedmaps3dsamples.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "altitude_settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val MIN_ALTITUDE_KEY = floatPreferencesKey("min_altitude")
        val MAX_ALTITUDE_KEY = floatPreferencesKey("max_altitude")
        const val DEFAULT_MIN_ALTITUDE = 5000f
        const val DEFAULT_MAX_ALTITUDE = 10000f

        val SWEEP_SPEED_KEY = floatPreferencesKey("sweep_speed")
        const val DEFAULT_SWEEP_SPEED = 50f

        val CAMERA_LAT_KEY = doublePreferencesKey("camera_lat")
        val CAMERA_LNG_KEY = doublePreferencesKey("camera_lng")
        val CAMERA_ALT_KEY = doublePreferencesKey("camera_alt")
        val CAMERA_HEADING_KEY = doublePreferencesKey("camera_heading")
        val CAMERA_TILT_KEY = doublePreferencesKey("camera_tilt")
        val CAMERA_ROLL_KEY = doublePreferencesKey("camera_roll")
        val CAMERA_RANGE_KEY = doublePreferencesKey("camera_range")
    }

    val minAltitudeFlow: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[MIN_ALTITUDE_KEY] ?: DEFAULT_MIN_ALTITUDE
        }

    val maxAltitudeFlow: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[MAX_ALTITUDE_KEY] ?: DEFAULT_MAX_ALTITUDE
        }

    val sweepSpeedFlow: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[SWEEP_SPEED_KEY] ?: DEFAULT_SWEEP_SPEED
        }

    suspend fun saveMinAltitude(minAltitude: Float) {
        dataStore.edit { preferences ->
            preferences[MIN_ALTITUDE_KEY] = minAltitude
        }
    }

    suspend fun saveMaxAltitude(maxAltitude: Float) {
        dataStore.edit { preferences ->
            preferences[MAX_ALTITUDE_KEY] = maxAltitude
        }
    }

    suspend fun saveSweepSpeed(speed: Float) {
        dataStore.edit { preferences ->
            preferences[SWEEP_SPEED_KEY] = speed
        }
    }

    val cameraFlow: Flow<Camera?> = dataStore.data.map { preferences ->
        val lat = preferences[CAMERA_LAT_KEY]
        val lng = preferences[CAMERA_LNG_KEY]
        if (lat != null && lng != null) {
            camera {
                center = latLngAltitude {
                    latitude = lat
                    longitude = lng
                    altitude = preferences[CAMERA_ALT_KEY] ?: 0.0
                }
                heading = preferences[CAMERA_HEADING_KEY] ?: 0.0
                tilt = preferences[CAMERA_TILT_KEY] ?: 0.0
                roll = preferences[CAMERA_ROLL_KEY] ?: 0.0
                range = preferences[CAMERA_RANGE_KEY] ?: 0.0
            }
        } else {
            null
        }
    }

    suspend fun saveCamera(camera: Camera) {
        dataStore.edit { preferences ->
            preferences[CAMERA_LAT_KEY] = camera.center.latitude
            preferences[CAMERA_LNG_KEY] = camera.center.longitude
            preferences[CAMERA_ALT_KEY] = camera.center.altitude
            camera.heading?.let { preferences[CAMERA_HEADING_KEY] = it }
            camera.tilt?.let { preferences[CAMERA_TILT_KEY] = it }
            camera.roll?.let { preferences[CAMERA_ROLL_KEY] = it }
            camera.range?.let { preferences[CAMERA_RANGE_KEY] = it }
        }
    }
}
