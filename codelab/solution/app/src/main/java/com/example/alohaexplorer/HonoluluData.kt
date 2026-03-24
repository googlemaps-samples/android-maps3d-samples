package com.example.alohaexplorer

import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude

// Step 1: Honolulu
val HONOLULU = latLngAltitude {
    latitude = 21.3069
    longitude = -157.8583
    altitude = 0.0
}

// Step 2: Iolani Palace
val IOLANI_PALACE = latLngAltitude {
    latitude = 21.306740
    longitude = -157.858803
    altitude = 0.0
}

// Step 6: Waikiki Beach
val WAIKIKI = latLngAltitude {
    latitude = 21.2766
    longitude = -157.8286
    altitude = 0.0
}

val DIAMOND_HEAD = latLngAltitude {
    latitude = 21.26194
    longitude = -157.80556
    altitude = 0.0
}

val KOKO_HEAD = latLngAltitude {
    latitude = 21.270856
    longitude = -157.694442
    altitude = 0.0
}

val PEARL_HARBOR = latLngAltitude {
    latitude = 21.31861
    longitude = -157.92250
    altitude = 0.0
}

val MOUNT_KAALA = latLngAltitude {
    latitude = 21.50694
    longitude = -158.14278
    altitude = 0.0
}

val LANIKAI_BEACH = latLngAltitude {
    latitude = 21.39309
    longitude = -157.71546
    altitude = 0.0
}

// Models must be loaded from a URL.  Here we use Cloud Storage.
const val BALLOON_MODEL_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/balloon-pin-BlXF32yD.glb"
const val BALLOON_SCALE = 5.0

// Step 5: Iolani Palace Geometry (Lat, Lng pairs)
val IOLANI_PALACE_GEO = listOf(
    21.307180365, -157.858769898,
    21.306765552, -157.858390366,
    21.306476932, -157.858755146,
    21.306892995, -157.859134679,
)

val POLYGON_CAMERA = camera {
    center = latLngAltitude {
        latitude = 21.307051
        longitude = -157.858546
        altitude = 7.1
    }
    heading = 78.0
    tilt = 53.0
    range = 644.0
}

val MARKER_CAMERA = camera {
    center = latLngAltitude {
        latitude = 21.306648
        longitude = -157.859336
        altitude = 26.8
    }
    tilt = 68.0
    range = 1024.0
    heading = 61.0
}

val CUSTOM_MARKER_CAMERA = camera {
    this.center = latLngAltitude {
        this.latitude = 21.306370
        this.longitude = -157.855474
        this.altitude = 6.6
    }
    this.heading = 39.0
    this.tilt = 68.0
    this.range = 1399.0
}

val BALLOON_CAMERA = camera {
    center = WAIKIKI
    tilt = 60.0
    range = 1000.0
    heading = 0.0
}
