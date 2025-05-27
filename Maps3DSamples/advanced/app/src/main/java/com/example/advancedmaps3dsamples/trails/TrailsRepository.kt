package com.example.advancedmaps3dsamples.trails

import android.content.Context
import android.util.JsonReader
import android.util.JsonToken
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.ktx.utils.simplify
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Reader
import javax.inject.Inject


/**
 * Repository class for managing trail data.
 *
 * This class is responsible for reading trail data from a JSON asset file,
 * parsing it, and providing access to the list of trails and their combined
 * geographical boundary.
 *
 * @property context The application context, injected by Hilt, used to access assets.
 */
class TrailsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = this::class.java.simpleName
    private var job: Job? = null

    private val _trails = MutableStateFlow(emptyList<Trail>())
    val trails = _trails.asStateFlow()

    private val _boundary = MutableStateFlow(null as LatLngBounds?)
    val boundary = _boundary.asStateFlow()

    /**
     * Reads trail data from a JSON file in the assets folder.
     *
     * This function is designed to be called only once. If the trail data
     * is already loaded or a loading job is in progress, it will do nothing.
     *
     * The function reads the specified JSON file, parses it into a list of [Trail] objects,
     * calculates the geographical boundary ([LatLngBounds]) encompassing all trails,
     * and updates the internal state flows [_trails] and [_boundary].
     *
     * @param assetName The name of the JSON file (e.g., "trails.json") located in the assets folder.
     */
    suspend fun readTrailsFromAsset(assetName: String) {
        if (_trails.value.isEmpty() && job == null) {
            withContext(Dispatchers.IO) {
                job = launch {
                    val trailList =
                        parseTrailsFromJson(context.assets.open(assetName).bufferedReader())
                    _boundary.value = trailList.map {
                        it.coordinates.toLatLngBounds()
                    }.boundsToLatLngBounds()

                    val points = trailList.sumOf { trail -> trail.coordinates.size }

                    Log.w(TAG, "Total number of points: $points")

                    _trails.value = trailList
                    job = null
                }
            }
        }
    }
}

private fun Iterable<LatLngBounds>.boundsToLatLngBounds(): LatLngBounds {
    return LatLngBounds.Builder().apply {
        this@boundsToLatLngBounds.forEach {
            include(it.southwest)
            include(it.northeast)
        }
    }.build()
}

private fun Iterable<LatLng>.toLatLngBounds(): LatLngBounds {
    return LatLngBounds.Builder().apply {
        this@toLatLngBounds.forEach {
            include(it)
        }
    }.build()
}

/**
 * Parses a GeoJSON FeatureCollection into a list of [Trail] objects.
 *
 * The function expects the GeoJSON to have a "features" array, where each element
 * is a GeoJSON Feature object representing a trail.
 *
 * @param source A [Reader] for the GeoJSON data.
 * @return A list of [Trail] objects parsed from the GeoJSON.
 */
private fun parseTrailsFromJson(source: Reader): List<Trail> {
    val reader = JsonReader(source)

    return buildList {
        reader.beginObject()
        while (reader.hasNext()) {
            val thing = reader.peek()
            Log.w("parser", thing.name)
            if (thing.name == "NAME") {
                val attribute = reader.nextName()
                if (attribute == "features") {
                    reader.beginArray()
                    while(reader.hasNext()) {
                        add(parseTrail(reader))
                    }
                    reader.endArray()
                }
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()
    }
}

/**
 * Parses a GeoJSON Feature object into a [Trail] object.
 *
 * This function expects the [JsonReader] to be positioned at the beginning of a GeoJSON Feature object.
 * It reads the "properties" and "geometry" members of the Feature to populate the [Trail] data.
 * Other members of the Feature object are skipped.
 *
 * @param reader The [JsonReader] positioned at the start of a GeoJSON Feature object.
 * @return A [Trail] object populated with data from the JSON.
 */
private fun parseTrail(reader: JsonReader) : Trail {
    var trail = Trail()
    reader.beginObject()
    while (reader.hasNext()) {
        val thing = reader.peek()
        if (thing.name == "NAME") {
            val attribute = reader.nextName()
            trail = when (attribute) {
                "properties" -> parseProperties(reader, trail)
                "geometry" -> parseGeometry(reader, trail)
                else -> { reader.skipValue(); trail }
            }
        } else {
            reader.skipValue()
        }
    }
    reader.endObject()

    // TODO: validate trail
    return trail
}

/**
 * Parses the "properties" section of a Trail JSON object.
 *
 * This function reads the properties of a trail from the JSON stream and updates the provided [Trail] object.
 * It expects the [JsonReader] to be positioned at the beginning of the "properties" object.
 *
 * It handles known property names like "OSMPTrailsOSMPMILEAGE", "OSMPTrailsOSMPTRAILNAME", etc.,
 * and attempts to convert their values to the appropriate types for the [Trail] data class.
 * Unrecognized properties or properties with null values are skipped.
 * Any exceptions encountered during parsing a specific property are caught and ignored, allowing
 * the parsing of other properties to continue.
 *
 * @param reader The [JsonReader] instance, positioned at the start of the properties object.
 * @param trailIn The initial [Trail] object to be updated with parsed properties.
 * @return A new [Trail] object with the properties read from the JSON.
 */
fun parseProperties(reader: JsonReader, trailIn: Trail): Trail {
    reader.beginObject()

    var trail = trailIn

    while (reader.hasNext()) {
        val name = if (reader.peek().name == "NAME") reader.nextName() else break
        try {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                continue
            }

            when (name) {
                "OSMPTrailsOSMPMILEAGE" -> trail = trail.copy(mileage = reader.nextDouble())
                "OSMPTrailsOSMPTRAILNAME" -> trail = trail.copy(name = reader.nextString())
                "OSMPTrailsOSMPDIFFICULTY" -> trail = trail.copy(difficulty = reader.nextString().toDifficultyLevel())

                "OSMPTrailsOSMPMEASUREDFEET" -> trail = trail.copy(measuredFeet = reader.nextInt())
                "OSMPTrailsOSMPBICYCLES" -> trail = trail.copy(bicyclesAllowed = reader.nextString().yesNoToBoolean())
                "OSMPTrailsOSMPTRAILTYPE" -> trail = trail.copy(type = reader.nextString().toTrailType())
                "OSMPTrailsOSMPSEGMENTID" -> trail = trail.copy(segmentId = reader.nextString())
                "OSMPTrailsOSMPRID" -> trail = trail.copy(id = reader.nextInt())
                "OSMPTrailsOSMPDOGS" -> trail = trail.copy(dogsAllowed = reader.nextString().yesNoToBoolean())
                "OSMPTrailsOSMPDOGREGGEN" -> trail = trail.copy(dogRegulations = reader.nextString().toDogRegulation())
                "OSMPTrailsOSMPDOGREGDESC" -> trail = trail.copy(dogRegulationDescription = reader.nextString())
                "OSMPTrailsOSMPEBIKES" -> trail = trail.copy(eBikesAllowed = reader.nextString().yesNoToBoolean())

                // Assuming these fields map directly to Trail properties, adjust as needed
//                "OSMPTrailsOSMPGlobalID" -> trail.copy(globalId = reader.nextString())
//                "OSMPTrailsOSMPOBJECTID" -> trail.copy(objectId = reader.nextString())
//                "OSMPTrailsOSMPOWNER" -> trail.copy(owner = reader.nextString())
//                "OSMPTrailsOSMPDISPLAY" -> trail.copy(display = reader.nextString())
//                "OSMPTrailsOSMPDATEFROM" -> trail.copy(dateFrom = reader.nextString().toDate())
//                "OSMPTrailsOSMPDATETO" -> trail.copy(dateTo = reader.nextString().toDate())
//                "OSMPTrailsOSMPHORSES" -> trail.copy(horsesAllowed = reader.nextBoolean())
//                "OSMPTrailsOSMPTRLID" -> trail.copy(trlId = reader.nextString())
//                "SHAPESTLength" -> trail.copy(shapeLength = reader.nextDouble())

                // These seem related to closures, you might need a separate data class or logic to handle them
//                "OSMPTrailClosuresOBJECTID" -> {
//                    // TODO: Handle closure data, potentially create a Closure object or add to trail if applicable
//                    reader.skipValue()
//                    trail
//                }
//                "OSMPTrailClosuresRID" -> TODO("Handle OSMPTrailClosuresRID")
//                "OSMPTrailClosuresCLOSUREDURATION" -> TODO("Handle OSMPTrailClosuresCLOSUREDURATION")
//                "OSMPTrailClosuresWEBLINK" -> TODO("Handle OSMPTrailClosuresWEBLINK")
//                "OSMPTrailClosuresCLOSUREAREA" -> TODO("Handle OSMPTrailClosuresCLOSUREAREA")
//                "OSMPTrailClosuresTRAILSTATUS" -> TODO("Handle OSMPTrailClosuresTRAILSTATUS")
//                "OSMPTrailClosuresCLOSUREREASON" -> TODO("Handle OSMPTrailClosuresCLOSUREREASON")
//                "OSMPTrailClosuresLOCATIONDESCRIPTION" -> TODO("Handle OSMPTrailClosuresLOCATIONDESCRIPTION")
//                "OSMPTrailClosuresCONTACT" -> TODO("Handle OSMPTrailClosuresCONTACT")
//                "OSMPTrailClosuresCOMMENTS" -> TODO("Handle OSMPTrailClosuresCOMMENTS")
//                "OSMPTrailClosuresGLOBALID" -> TODO("Handle OSMPTrailClosuresGLOBALID")
//                "OSMPTrailClosuresSEGMENTID" -> TODO("Handle OSMPTrailClosuresSEGMENTID")

                else -> {
                    reader.skipValue()
                }
            }
        } catch (_: Exception) {
        }
    }
    reader.endObject()

    return trail
}

private fun String.yesNoToBoolean(): Boolean = this.lowercase() == "yes"

/**
 * Parses the geometry object from the JSON data.
 *
 * This function expects a JSON object with "type" and "coordinates" fields.
 * If the "type" is "LineString" and coordinates are present, it updates the
 * trail's coordinates.
 *
 * @param reader The JsonReader positioned at the beginning of the geometry object.
 * @param trailIn The Trail object to be updated with geometry information.
 * @return The updated Trail object.
 */
fun parseGeometry(reader: JsonReader, trailIn: Trail) : Trail {
    reader.beginObject()

    var trail = trailIn

    var geometryType = ""
    var coordinates = emptyList<LatLng>()

    while (reader.hasNext()) {
        val name = if (reader.peek().name == "NAME") reader.nextName() else break
        when (name) {
            "type" -> geometryType = reader.nextString()
            "coordinates" -> coordinates = parseCoordinates(reader).toMutableList().simplify(1.0)
            else -> {
                reader.skipValue()
            }
        }
    }

    if (geometryType == "LineString" && coordinates.isNotEmpty()) {
        trail = trail.copy(coordinates = coordinates)
    }

    reader.endObject()

    return trail
}

/**
 * Parses a JSON array of coordinate pairs (longitude, latitude) into a list of LatLng objects.
 *
 * @param reader The JsonReader positioned at the beginning of the coordinates array.
 * @return A list of LatLng objects representing the parsed coordinates.
 */
private fun parseCoordinates(reader: JsonReader): List<LatLng> {
    reader.beginArray()
    return buildList {
        while (reader.hasNext()) {
            reader.beginArray()
            val longitude = reader.nextDouble()
            val latitude = reader.nextDouble()
            add(LatLng(/* latitude = */ latitude, /* longitude = */ longitude))
            reader.endArray()
        }
        reader.endArray()
    }
}

private fun String.toDogRegulation(): DogRegulation {
    return when (this) {
        "LVS" -> DogRegulation.VOICE_AND_SIGHT_CONTROL
        "LR" -> DogRegulation.LEASH_REQUIRED
        "RV" -> DogRegulation.REGULATION_VARIES
        else -> DogRegulation.NO_DOGS_ALLOWED
    }
}

private fun String.toDifficultyLevel(): DifficultyLevel = DifficultyLevel.valueOf(this.uppercase())

private fun String.toTrailType(): TrailType {
    return when(this) {
        "Hiking Trail" -> TrailType.HIKING
        "Multi-Use Trail" -> TrailType.MULTI_USE
        else -> error("Unhandled type: $this")
    }
}
