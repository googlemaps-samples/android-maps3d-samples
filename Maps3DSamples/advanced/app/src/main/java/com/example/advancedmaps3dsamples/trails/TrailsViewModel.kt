package com.example.advancedmaps3dsamples.trails

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.advancedmaps3dsamples.common.ActiveMapObject
import com.example.advancedmaps3dsamples.common.Map3dViewModel
import com.example.advancedmaps3dsamples.utils.toCameraString
import com.google.android.gms.maps3d.Map3DOptions
import com.google.android.gms.maps3d.model.Map3DMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

sealed class ViewState {
    data object Loading : ViewState()
    data class TrailMap(
        val options: Map3DOptions
    ) : ViewState()
}

sealed class ViewEvent {
    data object LoadTrails : ViewEvent()
}

/**
 * ViewModel for the Trails screen.
 *
 * This ViewModel is responsible for loading and managing trail data,
 * and preparing it for display on a 3D map. It combines trail data and boundary
 * information from the [TrailsRepository] to create a [ViewState] that
 * dictates how the map should be configured and what trails to display.
 *
 * It extends [Map3dViewModel] to inherit common map interaction functionalities.
 *
 * @property trailsRepository The repository responsible for fetching and processing trail data.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class TrailsViewModel @Inject constructor(
    private val trailsRepository: TrailsRepository
) : Map3dViewModel() {
    override val TAG = this::class.java.simpleName

    val viewState = trailsRepository.trails.combine(
        trailsRepository.boundary
    ) { trails, boundary ->
        if (trails.isEmpty() || boundary == null) {
            ViewState.Loading
        } else {
            val latitude = boundary.center.latitude
            val longitude = boundary.center.longitude
            val altitude = 1616.0

            val heading = 0.0
            val tilt = 0.0
            val range = 65_000.0
            val roll = 0.0

            val options = Map3DOptions(
                defaultUiDisabled = true,
                centerLat = latitude,
                centerLng = longitude,
                centerAlt = altitude,
                heading = heading,
                tilt = tilt,
                roll = roll,
                range = range,
                minHeading = 0.0,
                maxHeading = 360.0,
                minTilt = 0.0,
                maxTilt = 90.0,
                bounds = null,
                mapMode = Map3DMode.SATELLITE,
                mapId = null,
            )

            val polylineOptions = trails.map { trail ->
                trail.toPolylineOptions()
            }

            clearPolylines()

            polylineOptions.forEach { addPolyline(it) }

            ViewState.TrailMap(
                options = options
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = ViewState.Loading,
    )

    private fun clearPolylines() {
        activeMapObjects.filter { it.value is ActiveMapObject.ActivePolyline }.map { it.key }
            .forEach { key ->
                activeMapObjects.remove(key)?.remove()
            }
    }

    init {
        loadTrails()

        viewModelScope.launch {
            currentCamera.debounce(1.seconds).collect { camera ->
                Log.d(TAG, "Current Camera: ${camera.toCameraString()}")
            }
        }
    }

    fun onEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.LoadTrails -> loadTrails()
        }
    }

    private fun loadTrails() {
        viewModelScope.launch {
            trailsRepository.readTrailsFromAsset("OSMP_Trails.geojson")
        }
    }
}
