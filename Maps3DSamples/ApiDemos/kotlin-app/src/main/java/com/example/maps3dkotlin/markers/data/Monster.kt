package com.example.maps3dkotlin.markers.data

data class Monster(
    val id: String,
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val heading: Double,
    val tilt: Double,
    val range: Double,
    val markerLatitude: Double,
    val markerLongitude: Double,
    val markerAltitude: Double,
    val drawable: String,
    val altitudeMode: Int
)
