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

package com.example.maps3d.common

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.maps.android.SphericalUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Type of vertical road inflection point.
 */
enum class ExtremaType {
    /** Local elevation peak where road transitions from uphill to downhill. */
    CREST,

    /** Local elevation trough where road transitions from downhill to uphill. */
    DIP
}

/**
 * Classification of upcoming terrain hazards along a 3D route.
 */
enum class TerrainAlertType {
    /** Rapid consecutive climbs and descents (≥ 2 crest/dip reversals with |grade| ≥ 5%). */
    UNDULATING_ROLLER_COASTER,

    /** Single sharp summit transition from uphill (≥ +5%) to downhill (≤ -5%) ahead. */
    BLIND_CREST,

    /** Sustained steep uphill grade (≥ +8%) without rapid oscillation. */
    STEEP_CLIMB,

    /** Sustained steep downhill grade (≤ -8%) without rapid oscillation. */
    STEEP_DESCENT,

    /** Normal flat or gently graded road (|grade| < 5% and < 2 reversals). */
    NONE
}

/**
 * Early warning urgency level based on distance to the detected terrain hazard.
 */
enum class AlertSeverity {
    /** No terrain hazard within the active lookahead window. */
    NONE,

    /** Early warning (Amber): hazard detected ≥ 150m ahead within the lookahead horizon. */
    CAUTION,

    /** Imminent or active alert (Red): hazard < 150m ahead or vehicle is inside the hazard zone. */
    WARNING
}

/**
 * Represents a detected crest (local peak) or dip (local trough) ahead of the vehicle.
 *
 * @property id Stable identifier anchored to the route vertex index (e.g., `"crest_5"`, `"dip_6"`).
 * @property position 3D geographic coordinates and elevation of the inflection point.
 * @property type Whether this point is a [ExtremaType.CREST] or [ExtremaType.DIP].
 * @property distanceAheadMeters Horizontal distance ahead of the vehicle in meters.
 * @property gradeDeltaPercent Signed grade change across the inflection point (`gIn - gOut`),
 *   positive for crests and negative for dips.
 * @property approachGradePercent Road grade (%) leading into the inflection point.
 */
data class TerrainExtremaPoint(
    @JvmField val id: String,
    @JvmField val position: LatLngAltitude,
    @JvmField val type: ExtremaType,
    @JvmField val distanceAheadMeters: Double,
    @JvmField val gradeDeltaPercent: Double,
    @JvmField val approachGradePercent: Double = gradeDeltaPercent
)

/**
 * Sampled elevation point within the forward lookahead horizon for HUD sparkline rendering.
 *
 * @property distanceOffsetMeters Distance ahead of the vehicle (`0.0` to `lookaheadHorizonMeters`).
 * @property altitudeMeters Interpolated terrain altitude in meters.
 * @property isHazardZone True if this sample lies inside a steep or undulating hazard segment.
 */
data class ElevationProfilePoint(
    @JvmField val distanceOffsetMeters: Double,
    @JvmField val altitudeMeters: Double,
    @JvmField val isHazardZone: Boolean
)

/**
 * Complete snapshot of the forward terrain analysis and early alert state.
 *
 * @property alertType Classified terrain hazard type in the lookahead window.
 * @property severity Alert urgency ([AlertSeverity.NONE], [AlertSeverity.CAUTION], [AlertSeverity.WARNING]).
 * @property distanceToHazardMeters Distance from the car to the start of the hazard zone (`0.0` when inside).
 * @property remainingHazardDistanceMeters Remaining length of the active or upcoming hazard zone in meters.
 * @property currentGradePercent Instantaneous road grade (%) at the vehicle's current position.
 * @property maxPositiveGradePercent Maximum uphill grade (%) within the lookahead window.
 * @property maxNegativeGradePercent Maximum downhill grade (%) within the lookahead window (≤ 0.0).
 * @property cumulativeVerticalChangeMeters Total vertical oscillation (∑ |Δh|) in the lookahead window.
 * @property reversalCount Number of crest/dip reversals detected in the lookahead window.
 * @property crestCount Number of [ExtremaType.CREST] points in [upcomingExtrema].
 * @property dipCount Number of [ExtremaType.DIP] points in [upcomingExtrema].
 * @property oscillationScore Composite roller-coaster intensity score.
 * @property upcomingExtrema Ordered list of crests and dips within the lookahead window.
 * @property lookaheadProfile Sampled elevation curve for the HUD sparkline.
 * @property hazardPolylineCoordinates 3D polyline coordinates highlighting the active hazard zone.
 */
data class TerrainAlertState(
    @JvmField val alertType: TerrainAlertType,
    @JvmField val severity: AlertSeverity,
    @JvmField val distanceToHazardMeters: Double,
    @JvmField val remainingHazardDistanceMeters: Double,
    @JvmField val currentGradePercent: Double,
    @JvmField val maxPositiveGradePercent: Double,
    @JvmField val maxNegativeGradePercent: Double,
    @JvmField val cumulativeVerticalChangeMeters: Double,
    @JvmField val reversalCount: Int,
    @JvmField val crestCount: Int,
    @JvmField val dipCount: Int,
    @JvmField val oscillationScore: Double,
    @JvmField val upcomingExtrema: List<TerrainExtremaPoint>,
    @JvmField val lookaheadProfile: List<ElevationProfilePoint>,
    @JvmField val hazardPolylineCoordinates: List<LatLngAltitude>
)

/**
 * Pure Kotlin terrain lookahead and undulation analyzer.
 *
 * Scans the upcoming `[currentDistanceMeters, currentDistanceMeters + lookaheadHorizonMeters]`
 * window along a 3D route to detect crests, dips, steep grades, and undulating ("roller-coaster")
 * road zones before the vehicle enters them.
 */
object RoadTerrainAnalyzer {

    /** Minimum horizontal distance in meters to guard against division by zero. */
    const val MIN_HORIZONTAL_DELTA_METERS = 1e-4

    /** Minimum grade reversal magnitude (|Δgrade| ≥ 4%) required to register a crest or dip. */
    const val MIN_GRADE_DELTA_PERCENT = 4.0

    /** Minimum vertical prominence (≥ 1.5m) required to register a crest or dip. */
    const val MIN_PROMINENCE_METERS = 1.5

    /** Grade threshold (|grade| ≥ 5%) for undulating and blind-crest hazard zones. */
    const val HAZARD_GRADE_THRESHOLD_PERCENT = 5.0

    /** Grade threshold (|grade| ≥ 8%) for sustained steep climb or descent alerts. */
    const val STEEP_GRADE_THRESHOLD_PERCENT = 8.0

    /** Distance threshold (< 150m) at which CAUTION escalates to WARNING. */
    const val WARNING_PROXIMITY_METERS = 150.0

    /** Horizontal baseline window (60m) used to smooth micro-segment GPS noise when segments are < 60m. */
    private const val WINDOWED_GRADE_BASELINE_METERS = 60.0

    /** Minimum horizontal span (15m) required when clamping a slope window to the active lookahead bounds. */
    private const val MIN_WINDOW_SLOPE_SPAN_METERS = 15.0

    /** Maximum spacing between adjacent extrema hill runs to be grouped into a single undulating cluster. */
    private const val CLUSTER_MAX_SPACING_METERS = 250.0

    /** Altitude bias (+0.65m) applied to the hazard highlight polyline so it renders above the base route. */
    private const val HAZARD_POLYLINE_ALTITUDE_BIAS_METERS = 0.65

    /** Number of sample intervals used to build the HUD elevation sparkline profile. */
    private const val SPARKLINE_SAMPLE_STEPS = 32

    private data class CandidateExtremum(
        val type: ExtremaType,
        val vertexIndex: Int
    )

    private data class RouteSegment(
        val index: Int,
        val startDist: Double,
        val endDist: Double,
        val startPoint: LatLngAltitude,
        val endPoint: LatLngAltitude,
        val gradePercent: Double,
        val monotonicStartIdx: Int,
        val monotonicEndIdx: Int
    )

    private data class RouteExtremum(
        val id: String,
        val vertexIndex: Int,
        val routeDistanceMeters: Double,
        val position: LatLngAltitude,
        val type: ExtremaType,
        val gradeDeltaPercent: Double,
        val approachGradePercent: Double,
        val hazardRunStartDist: Double,
        val hazardRunEndDist: Double,
        val clusterId: Int,
        val clusterSize: Int
    )

    private class PreparedRouteTopology(
        val sourcePath: List<LatLngAltitude>,
        val cleanedPath: List<LatLngAltitude>,
        val cumulativeDistances: DoubleArray,
        val totalDistance: Double,
        val segments: List<RouteSegment>,
        val allExtrema: List<RouteExtremum>
    )

    @Volatile
    private var cachedTopology: PreparedRouteTopology? = null

    private fun getOrBuildTopology(path: List<LatLngAltitude>): PreparedRouteTopology? {
        val existing = cachedTopology
        if (existing != null && (existing.sourcePath === path || existing.sourcePath == path)) {
            return existing
        }
        val cleanedPath = removeConsecutiveDuplicates(path)
        if (cleanedPath.size < 2) {
            return null
        }
        val cumulativeDistances = PathEngine.calculateCumulativeDistances(cleanedPath)
        val totalDistance = cumulativeDistances.lastOrNull() ?: 0.0
        if (totalDistance <= MIN_HORIZONTAL_DELTA_METERS) {
            return null
        }
        val candidates = findCandidateExtrema(cleanedPath)
        val segments = buildSegments(cleanedPath, cumulativeDistances, candidates)
        if (segments.isEmpty()) {
            return null
        }
        val allExtrema = detectRouteExtrema(cleanedPath, cumulativeDistances, segments, candidates)
        return PreparedRouteTopology(
            sourcePath = path,
            cleanedPath = cleanedPath,
            cumulativeDistances = cumulativeDistances,
            totalDistance = totalDistance,
            segments = segments,
            allExtrema = allExtrema
        ).also {
            cachedTopology = it
        }
    }

    /**
     * Computes road grade percentage `(Δaltitude / ΔhorizontalDistance) × 100`, guarding against
     * `Δd ≤ 1e-4m` so `NaN` or `Infinity` can never occur.
     */
    @JvmStatic
    fun computeGradePercent(
        deltaAltitudeMeters: Double,
        horizontalDistanceMeters: Double
    ): Double {
        if (horizontalDistanceMeters <= MIN_HORIZONTAL_DELTA_METERS ||
            deltaAltitudeMeters.isNaN() ||
            horizontalDistanceMeters.isNaN()
        ) {
            return 0.0
        }
        val grade = (deltaAltitudeMeters / horizontalDistanceMeters) * 100.0
        return if (grade.isFinite()) grade else 0.0
    }

    /**
     * Analyzes the upcoming road window `[currentDistanceMeters, currentDistanceMeters + lookaheadHorizonMeters]`
     * and returns the complete [TerrainAlertState].
     */
    @JvmStatic
    @JvmOverloads
    fun analyze(
        path: List<LatLngAltitude>,
        currentDistanceMeters: Double,
        lookaheadHorizonMeters: Double = 500.0
    ): TerrainAlertState {
        val topology = getOrBuildTopology(path) ?: return emptyAlertState()
        val cleanedPath = topology.cleanedPath
        val cumulativeDistances = topology.cumulativeDistances
        val totalDistance = topology.totalDistance
        val segments = topology.segments
        val allExtrema = topology.allExtrema

        val safeHorizon = lookaheadHorizonMeters.coerceAtLeast(10.0)
        val clampedCurrentDist = currentDistanceMeters.coerceIn(0.0, totalDistance)
        val windowEndDist = (clampedCurrentDist + safeHorizon).coerceAtMost(totalDistance)

        // Instantaneous grade at current vehicle position
        val currentSeg = segments.firstOrNull {
            clampedCurrentDist >= it.startDist && clampedCurrentDist <= it.endDist
        } ?: segments.last()
        val currentGradePercent = currentSeg.gradePercent

        // If at the very end of the route, return clear state
        if (windowEndDist - clampedCurrentDist <= MIN_HORIZONTAL_DELTA_METERS) {
            val endPt = cleanedPath.last()
            return emptyAlertState(
                currentGradePercent = currentGradePercent,
                lookaheadProfile = listOf(
                    ElevationProfilePoint(0.0, endPt.altitude, false)
                )
            )
        }

        // Extrema directly inside the lookahead window [clampedCurrentDist, windowEndDist]
        val directWindowExtrema = allExtrema.filter {
            it.routeDistanceMeters >= clampedCurrentDist - MIN_HORIZONTAL_DELTA_METERS &&
                it.routeDistanceMeters <= windowEndDist + MIN_HORIZONTAL_DELTA_METERS
        }

        // Also check if the lookahead window has entered the climb/descent run of a multi-undulation cluster
        val multiUndulationClusters = allExtrema
            .filter { it.clusterSize >= 2 && abs(it.approachGradePercent) >= HAZARD_GRADE_THRESHOLD_PERCENT }
            .groupBy { it.clusterId }

        val enteredClusterExtrema = mutableListOf<RouteExtremum>()
        for ((_, clusterList) in multiUndulationClusters) {
            val clusterStart = clusterList.first().hazardRunStartDist
            val clusterEnd = clusterList.last().hazardRunEndDist
            val overlapsWindow = windowEndDist > clusterStart + MIN_HORIZONTAL_DELTA_METERS &&
                clampedCurrentDist < clusterEnd - MIN_HORIZONTAL_DELTA_METERS
            if (overlapsWindow) {
                val upcomingInCluster = clusterList.filter {
                    it.routeDistanceMeters >= clampedCurrentDist - MIN_HORIZONTAL_DELTA_METERS
                }
                val previewCount = if (safeHorizon >= 700.0) 2 else 1
                enteredClusterExtrema.addAll(upcomingInCluster.take(previewCount))
            }
        }

        val windowExtremaInternal = (directWindowExtrema + enteredClusterExtrema)
            .distinctBy { it.id }
            .sortedBy { it.routeDistanceMeters }

        val upcomingExtrema = windowExtremaInternal.map { ext ->
            TerrainExtremaPoint(
                id = ext.id,
                position = ext.position,
                type = ext.type,
                distanceAheadMeters = (ext.routeDistanceMeters - clampedCurrentDist).coerceAtLeast(0.0),
                gradeDeltaPercent = ext.gradeDeltaPercent,
                approachGradePercent = ext.approachGradePercent
            )
        }

        // Segments overlapping [clampedCurrentDist, windowEndDist]
        val windowSegments = segments.filter { seg ->
            seg.endDist > clampedCurrentDist + MIN_HORIZONTAL_DELTA_METERS &&
                seg.startDist < windowEndDist - MIN_HORIZONTAL_DELTA_METERS
        }

        val windowStartVertexIdx = windowSegments.firstOrNull()?.index ?: 0
        val windowEndVertexIdx = (windowSegments.lastOrNull()?.index ?: 0) + 1

        var maxPosGrade = 0.0
        var maxNegGrade = 0.0
        var cumulativeVerticalChange = 0.0

        for (seg in windowSegments) {
            val overlapStart = max(seg.startDist, clampedCurrentDist)
            val overlapEnd = min(seg.endDist, windowEndDist)
            val overlapLen = overlapEnd - overlapStart
            if (overlapLen > MIN_HORIZONTAL_DELTA_METERS) {
                val lookaheadGrade = computeLookaheadSegmentGrade(
                    seg = seg,
                    cleanedPath = cleanedPath,
                    cumulativeDistances = cumulativeDistances,
                    windowStartVertexIdx = windowStartVertexIdx,
                    windowEndVertexIdx = windowEndVertexIdx
                )
                if (lookaheadGrade > maxPosGrade) maxPosGrade = lookaheadGrade
                if (lookaheadGrade < maxNegGrade) maxNegGrade = lookaheadGrade
                cumulativeVerticalChange += abs(seg.gradePercent / 100.0) * overlapLen
            }
        }

        val reversalCount = upcomingExtrema.size
        val crestCount = upcomingExtrema.count { it.type == ExtremaType.CREST }
        val dipCount = upcomingExtrema.count { it.type == ExtremaType.DIP }

        // Check whether any extremum in the window belongs to a multi-undulation roller-coaster cluster
        val hasMultiUndulationClusterInWindow = windowExtremaInternal.any { ext ->
            ext.clusterSize >= 2 && abs(ext.approachGradePercent) >= HAZARD_GRADE_THRESHOLD_PERCENT
        }

        val maxAbsGradeInWindow = max(maxPosGrade, abs(maxNegGrade))
        val oscillationScore = cumulativeVerticalChange * (1.0 + reversalCount * 0.6) +
            maxAbsGradeInWindow * 0.8

        // Classify TerrainAlertType
        val alertType = classifyAlertType(
            windowExtrema = windowExtremaInternal,
            hasMultiUndulationClusterInWindow = hasMultiUndulationClusterInWindow,
            maxPosGrade = maxPosGrade,
            maxNegGrade = maxNegGrade
        )

        // Identify hazard distance bounds & coordinates
        val hazardBounds = findHazardBounds(
            alertType = alertType,
            segments = segments,
            allExtrema = allExtrema,
            windowExtrema = windowExtremaInternal,
            clampedCurrentDist = clampedCurrentDist,
            windowEndDist = windowEndDist
        )

        val distanceToHazardMeters: Double
        val remainingHazardDistanceMeters: Double
        val severity: AlertSeverity
        val hazardPolylineCoordinates: List<LatLngAltitude>

        if (alertType == TerrainAlertType.NONE || hazardBounds == null) {
            distanceToHazardMeters = 0.0
            remainingHazardDistanceMeters = 0.0
            severity = AlertSeverity.NONE
            hazardPolylineCoordinates = emptyList()
        } else {
            val (hazardStartDist, hazardEndDist) = hazardBounds
            distanceToHazardMeters = if (hazardStartDist <= clampedCurrentDist + 0.5) {
                0.0
            } else {
                hazardStartDist - clampedCurrentDist
            }
            val activeStart = max(clampedCurrentDist, hazardStartDist)
            remainingHazardDistanceMeters = (hazardEndDist - activeStart).coerceAtLeast(0.0)
            severity = if (distanceToHazardMeters < WARNING_PROXIMITY_METERS) {
                AlertSeverity.WARNING
            } else {
                AlertSeverity.CAUTION
            }
            hazardPolylineCoordinates = sliceRouteSegment(
                path = cleanedPath,
                cumulativeDistances = cumulativeDistances,
                startDist = activeStart,
                endDist = max(activeStart + 1.0, min(hazardEndDist, windowEndDist)),
                altitudeBias = HAZARD_POLYLINE_ALTITUDE_BIAS_METERS
            )
        }

        val lookaheadProfile = sampleLookaheadProfile(
            path = cleanedPath,
            cumulativeDistances = cumulativeDistances,
            segments = segments,
            clampedCurrentDist = clampedCurrentDist,
            windowEndDist = windowEndDist,
            hazardBounds = hazardBounds
        )

        return TerrainAlertState(
            alertType = alertType,
            severity = severity,
            distanceToHazardMeters = distanceToHazardMeters,
            remainingHazardDistanceMeters = remainingHazardDistanceMeters,
            currentGradePercent = currentGradePercent,
            maxPositiveGradePercent = maxPosGrade,
            maxNegativeGradePercent = maxNegGrade,
            cumulativeVerticalChangeMeters = cumulativeVerticalChange,
            reversalCount = reversalCount,
            crestCount = crestCount,
            dipCount = dipCount,
            oscillationScore = oscillationScore,
            upcomingExtrema = upcomingExtrema,
            lookaheadProfile = lookaheadProfile,
            hazardPolylineCoordinates = hazardPolylineCoordinates
        )
    }

    /**
     * Finds the optimal route distance (~240m before the first hazard climb/crest) for the
     * "Jump to Hills" quick action so the user immediately sees the early warning trigger.
     */
    @JvmStatic
    @JvmOverloads
    fun findFirstHillyAlertTriggerDistance(
        path: List<LatLngAltitude>,
        preCrestOffsetMeters: Double = 240.0
    ): Double {
        val topology = getOrBuildTopology(path) ?: return 0.0
        val segments = topology.segments
        val extrema = topology.allExtrema

        val firstExt = extrema.firstOrNull()
        if (firstExt != null) {
            return (firstExt.hazardRunStartDist - preCrestOffsetMeters).coerceAtLeast(0.0)
        }

        val firstSteepSegDist = segments.firstOrNull {
            abs(it.gradePercent) >= HAZARD_GRADE_THRESHOLD_PERCENT
        }?.startDist ?: 0.0
        return (firstSteepSegDist - 180.0).coerceAtLeast(0.0)
    }

    private fun removeConsecutiveDuplicates(path: List<LatLngAltitude>): List<LatLngAltitude> {
        if (path.size <= 1) return path
        val result = ArrayList<LatLngAltitude>(path.size)
        result.add(path.first())
        for (i in 1 until path.size) {
            val prev = result.last()
            val curr = path[i]
            val dist = SphericalUtil.computeDistanceBetween(
                LatLng(prev.latitude, prev.longitude),
                LatLng(curr.latitude, curr.longitude)
            )
            if (dist > MIN_HORIZONTAL_DELTA_METERS) {
                result.add(curr)
            }
        }
        return result
    }

    /**
     * Identifies local elevation peaks ([ExtremaType.CREST]) and troughs ([ExtremaType.DIP])
     * using vertical prominence hysteresis ([MIN_PROMINENCE_METERS]).
     */
    private fun findCandidateExtrema(cleanedPath: List<LatLngAltitude>): List<CandidateExtremum> {
        val candidates = mutableListOf<CandidateExtremum>()
        var state = 0 // 0 = initial, 1 = climbing, -1 = descending
        var maxIdx = 0
        var minIdx = 0

        for (i in 1 until cleanedPath.size) {
            val alt = cleanedPath[i].altitude
            when (state) {
                1 -> {
                    if (alt >= cleanedPath[maxIdx].altitude) {
                        maxIdx = i
                    } else if (cleanedPath[maxIdx].altitude - alt >= MIN_PROMINENCE_METERS) {
                        candidates.add(CandidateExtremum(ExtremaType.CREST, maxIdx))
                        state = -1
                        minIdx = i
                    }
                }
                -1 -> {
                    if (alt <= cleanedPath[minIdx].altitude) {
                        minIdx = i
                    } else if (alt - cleanedPath[minIdx].altitude >= MIN_PROMINENCE_METERS) {
                        candidates.add(CandidateExtremum(ExtremaType.DIP, minIdx))
                        state = 1
                        maxIdx = i
                    }
                }
                else -> {
                    // state == 0 (initial undetermined direction): track both bounds until prominence locks direction
                    if (alt >= cleanedPath[maxIdx].altitude) {
                        maxIdx = i
                    }
                    if (alt <= cleanedPath[minIdx].altitude) {
                        minIdx = i
                    }
                    if (cleanedPath[maxIdx].altitude - alt >= MIN_PROMINENCE_METERS) {
                        if (cleanedPath[maxIdx].altitude - cleanedPath[0].altitude >= MIN_PROMINENCE_METERS) {
                            candidates.add(CandidateExtremum(ExtremaType.CREST, maxIdx))
                        }
                        state = -1
                        minIdx = i
                    } else if (alt - cleanedPath[minIdx].altitude >= MIN_PROMINENCE_METERS) {
                        if (cleanedPath[0].altitude - cleanedPath[minIdx].altitude >= MIN_PROMINENCE_METERS) {
                            candidates.add(CandidateExtremum(ExtremaType.DIP, minIdx))
                        }
                        state = 1
                        maxIdx = i
                    }
                }
            }
        }
        return candidates
    }

    private fun buildSegments(
        cleanedPath: List<LatLngAltitude>,
        cumulativeDistances: DoubleArray,
        candidates: List<CandidateExtremum>
    ): List<RouteSegment> {
        val n = cleanedPath.size
        val turningPoints = IntArray(candidates.size + 2)
        turningPoints[0] = 0
        for (k in candidates.indices) {
            turningPoints[k + 1] = candidates[k].vertexIndex
        }
        turningPoints[turningPoints.lastIndex] = n - 1

        var turnCursor = 0
        val segments = ArrayList<RouteSegment>(n - 1)
        for (i in 0 until n - 1) {
            while (turnCursor + 1 < turningPoints.lastIndex && turningPoints[turnCursor + 1] <= i) {
                turnCursor++
            }
            val monotonicStartIdx = turningPoints[turnCursor]
            val monotonicEndIdx = turningPoints[turnCursor + 1].coerceAtLeast(i + 1)

            val d1 = cumulativeDistances[i]
            val d2 = cumulativeDistances[i + 1]
            val deltaDist = d2 - d1
            val p1 = cleanedPath[i]
            val p2 = cleanedPath[i + 1]

            // Use direct segment grade for coarse routes (>= 60m) and a 60m windowed baseline
            // clamped to monotonic turning points [monotonicStartIdx, monotonicEndIdx] for dense
            // GPS/DEM traces (~1-15m spacing) so the smoothing window never straddles a crest or dip.
            val grade = if (deltaDist >= WINDOWED_GRADE_BASELINE_METERS) {
                computeGradePercent(p2.altitude - p1.altitude, deltaDist)
            } else {
                var s = i
                var e = i + 1
                while ((s > monotonicStartIdx || e < monotonicEndIdx) &&
                    (cumulativeDistances[e] - cumulativeDistances[s]) < WINDOWED_GRADE_BASELINE_METERS
                ) {
                    if (s > monotonicStartIdx) s--
                    if ((cumulativeDistances[e] - cumulativeDistances[s]) < WINDOWED_GRADE_BASELINE_METERS &&
                        e < monotonicEndIdx
                    ) {
                        e++
                    }
                }
                val winDist = cumulativeDistances[e] - cumulativeDistances[s]
                val winAlt = cleanedPath[e].altitude - cleanedPath[s].altitude
                computeGradePercent(winAlt, winDist)
            }

            segments.add(
                RouteSegment(
                    index = i,
                    startDist = d1,
                    endDist = d2,
                    startPoint = p1,
                    endPoint = p2,
                    gradePercent = grade,
                    monotonicStartIdx = monotonicStartIdx,
                    monotonicEndIdx = monotonicEndIdx
                )
            )
        }
        return segments
    }

    /**
     * Computes a segment's grade strictly within the intersection of its monotonic slope interval
     * `[seg.monotonicStartIdx, seg.monotonicEndIdx]` and the active lookahead window
     * `[windowStartVertexIdx, windowEndVertexIdx]`, preventing grades behind the vehicle from
     * bleeding into upcoming `Max Grade` telemetry.
     */
    private fun computeLookaheadSegmentGrade(
        seg: RouteSegment,
        cleanedPath: List<LatLngAltitude>,
        cumulativeDistances: DoubleArray,
        windowStartVertexIdx: Int,
        windowEndVertexIdx: Int
    ): Double {
        val segLen = seg.endDist - seg.startDist
        if (segLen >= WINDOWED_GRADE_BASELINE_METERS) {
            return seg.gradePercent
        }
        val leftBound = max(seg.monotonicStartIdx, windowStartVertexIdx).coerceAtMost(seg.index)
        val rightBound = min(seg.monotonicEndIdx, windowEndVertexIdx).coerceAtLeast(seg.index + 1)
        var s = seg.index
        var e = seg.index + 1
        while ((s > leftBound || e < rightBound) &&
            (cumulativeDistances[e] - cumulativeDistances[s]) < WINDOWED_GRADE_BASELINE_METERS
        ) {
            if (s > leftBound) s--
            if ((cumulativeDistances[e] - cumulativeDistances[s]) < WINDOWED_GRADE_BASELINE_METERS &&
                e < rightBound
            ) {
                e++
            }
        }
        val winDist = cumulativeDistances[e] - cumulativeDistances[s]
        val fullMonotonicSpan =
            cumulativeDistances[seg.monotonicEndIdx] - cumulativeDistances[seg.monotonicStartIdx]
        val minRequiredSpan = min(MIN_WINDOW_SLOPE_SPAN_METERS, fullMonotonicSpan)
        if (winDist < minRequiredSpan) {
            return 0.0
        }
        val winAlt = cleanedPath[e].altitude - cleanedPath[s].altitude
        return computeGradePercent(winAlt, winDist)
    }

    private fun detectRouteExtrema(
        cleanedPath: List<LatLngAltitude>,
        cumulativeDistances: DoubleArray,
        segments: List<RouteSegment>,
        candidates: List<CandidateExtremum>
    ): List<RouteExtremum> {
        if (segments.size < 2) return emptyList()

        data class RawExtremum(
            val id: String,
            val vertexIndex: Int,
            val routeDistanceMeters: Double,
            val position: LatLngAltitude,
            val type: ExtremaType,
            val gradeDeltaPercent: Double,
            val approachGradePercent: Double,
            val hazardRunStartDist: Double,
            val hazardRunEndDist: Double
        )

        val rawExtrema = mutableListOf<RawExtremum>()

        // 2. Evaluate incoming/outgoing grades and hazard run bounds for each candidate peak/trough
        for ((idx, candidate) in candidates.withIndex()) {
            val vIdx = candidate.vertexIndex
            if (vIdx <= 0 || vIdx >= cleanedPath.size - 1) continue

            val rStart = if (idx > 0) candidates[idx - 1].vertexIndex else 0
            val rEnd = if (idx + 1 < candidates.size) {
                candidates[idx + 1].vertexIndex
            } else {
                cleanedPath.size - 1
            }

            var effectiveGIn = segments[vIdx - 1].gradePercent
            for (segIdx in rStart until vIdx) {
                val segGrade = segments[segIdx].gradePercent
                if (candidate.type == ExtremaType.CREST && segGrade > effectiveGIn) {
                    effectiveGIn = segGrade
                } else if (candidate.type == ExtremaType.DIP && segGrade < effectiveGIn) {
                    effectiveGIn = segGrade
                }
            }

            var effectiveGOut = segments[vIdx].gradePercent
            for (segIdx in vIdx until rEnd) {
                val segGrade = segments[segIdx].gradePercent
                if (candidate.type == ExtremaType.CREST && segGrade < effectiveGOut) {
                    effectiveGOut = segGrade
                } else if (candidate.type == ExtremaType.DIP && segGrade > effectiveGOut) {
                    effectiveGOut = segGrade
                }
            }

            val gradeDelta = effectiveGIn - effectiveGOut // Positive at CREST, negative at DIP
            val hasSteepLeg = abs(effectiveGIn) >= HAZARD_GRADE_THRESHOLD_PERCENT ||
                abs(effectiveGOut) >= HAZARD_GRADE_THRESHOLD_PERCENT

            val isValid = if (candidate.type == ExtremaType.CREST) {
                effectiveGIn > 0.5 &&
                    effectiveGOut < -0.5 &&
                    gradeDelta >= MIN_GRADE_DELTA_PERCENT &&
                    hasSteepLeg
            } else {
                effectiveGIn < -0.5 &&
                    effectiveGOut > 0.5 &&
                    abs(gradeDelta) >= MIN_GRADE_DELTA_PERCENT &&
                    hasSteepLeg &&
                    !(idx == 0 && abs(effectiveGIn) < HAZARD_GRADE_THRESHOLD_PERCENT)
            }

            if (isValid) {
                val inStart = if (idx > 0) (rStart + vIdx) / 2 else 0
                val outEnd = if (idx + 1 < candidates.size) (vIdx + rEnd + 1) / 2 else rEnd

                val firstHazardInSegIdx = (inStart until vIdx).firstOrNull {
                    abs(segments[it].gradePercent) >= HAZARD_GRADE_THRESHOLD_PERCENT
                } ?: (vIdx - 1).coerceAtLeast(0)
                val lastHazardOutSegIdx = (vIdx until outEnd).lastOrNull {
                    abs(segments[it].gradePercent) >= HAZARD_GRADE_THRESHOLD_PERCENT
                } ?: vIdx.coerceAtMost(segments.size - 1)

                val hazardRunStartDist = segments[firstHazardInSegIdx].startDist
                val hazardRunEndDist = segments[lastHazardOutSegIdx].endDist
                val prefix = if (candidate.type == ExtremaType.CREST) "crest" else "dip"

                rawExtrema.add(
                    RawExtremum(
                        id = "${prefix}_$vIdx",
                        vertexIndex = vIdx,
                        routeDistanceMeters = cumulativeDistances[vIdx],
                        position = cleanedPath[vIdx],
                        type = candidate.type,
                        gradeDeltaPercent = gradeDelta,
                        approachGradePercent = effectiveGIn,
                        hazardRunStartDist = hazardRunStartDist,
                        hazardRunEndDist = hazardRunEndDist
                    )
                )
            }
        }

        if (rawExtrema.isEmpty()) return emptyList()

        // Group adjacent extrema into clusters when their hill runs connect or are spaced <= CLUSTER_MAX_SPACING_METERS
        val clusterIds = IntArray(rawExtrema.size)
        var currentClusterId = 0
        clusterIds[0] = currentClusterId
        for (idx in 1 until rawExtrema.size) {
            val prev = rawExtrema[idx - 1]
            val curr = rawExtrema[idx]
            val runGap = curr.hazardRunStartDist - prev.hazardRunEndDist
            val vertexSpacing = curr.routeDistanceMeters - prev.routeDistanceMeters
            val maxClusterVertexSpacing = CLUSTER_MAX_SPACING_METERS * 1.5
            if (vertexSpacing <= CLUSTER_MAX_SPACING_METERS ||
                (runGap <= CLUSTER_MAX_SPACING_METERS && vertexSpacing <= maxClusterVertexSpacing)
            ) {
                clusterIds[idx] = currentClusterId
            } else {
                currentClusterId++
                clusterIds[idx] = currentClusterId
            }
        }

        val clusterSizes = clusterIds.toList().groupingBy { it }.eachCount()

        return rawExtrema.mapIndexed { idx, raw ->
            val cid = clusterIds[idx]
            RouteExtremum(
                id = raw.id,
                vertexIndex = raw.vertexIndex,
                routeDistanceMeters = raw.routeDistanceMeters,
                position = raw.position,
                type = raw.type,
                gradeDeltaPercent = raw.gradeDeltaPercent,
                approachGradePercent = raw.approachGradePercent,
                hazardRunStartDist = raw.hazardRunStartDist,
                hazardRunEndDist = raw.hazardRunEndDist,
                clusterId = cid,
                clusterSize = clusterSizes[cid] ?: 1
            )
        }
    }

    private fun classifyAlertType(
        windowExtrema: List<RouteExtremum>,
        hasMultiUndulationClusterInWindow: Boolean,
        maxPosGrade: Double,
        maxNegGrade: Double
    ): TerrainAlertType {
        val significantExtremaCount = windowExtrema.count {
            abs(it.approachGradePercent) >= HAZARD_GRADE_THRESHOLD_PERCENT ||
                abs(it.gradeDeltaPercent) >= HAZARD_GRADE_THRESHOLD_PERCENT * 2.0
        }

        // 1. Undulating roller-coaster road: >= 2 significant reversals in window,
        // or leading/trailing extremum of a multi-undulation cluster inside the window.
        if (significantExtremaCount >= 2 || hasMultiUndulationClusterInWindow) {
            return TerrainAlertType.UNDULATING_ROLLER_COASTER
        }

        // 2. Single blind crest: sharp uphill (>= +5%) to downhill (<= -5%) transition
        val hasBlindCrest = windowExtrema.any { ext ->
            ext.type == ExtremaType.CREST &&
                ext.approachGradePercent >= HAZARD_GRADE_THRESHOLD_PERCENT &&
                (ext.gradeDeltaPercent - ext.approachGradePercent) >= HAZARD_GRADE_THRESHOLD_PERCENT
        }
        if (hasBlindCrest) {
            return TerrainAlertType.BLIND_CREST
        }

        // 3. Sustained steep climb or descent (>= 8% magnitude without rapid oscillation)
        if (maxPosGrade >= STEEP_GRADE_THRESHOLD_PERCENT && maxPosGrade >= abs(maxNegGrade)) {
            return TerrainAlertType.STEEP_CLIMB
        }
        if (maxNegGrade <= -STEEP_GRADE_THRESHOLD_PERCENT) {
            return TerrainAlertType.STEEP_DESCENT
        }

        return TerrainAlertType.NONE
    }

    private fun findHazardBounds(
        alertType: TerrainAlertType,
        segments: List<RouteSegment>,
        allExtrema: List<RouteExtremum>,
        windowExtrema: List<RouteExtremum>,
        clampedCurrentDist: Double,
        windowEndDist: Double
    ): Pair<Double, Double>? {
        if (alertType == TerrainAlertType.NONE) return null

        if (alertType == TerrainAlertType.UNDULATING_ROLLER_COASTER && windowExtrema.isNotEmpty()) {
            val activeClusterIds = windowExtrema.map { it.clusterId }.toSet()
            val clusterExtrema = allExtrema.filter { it.clusterId in activeClusterIds }
            val firstExt = clusterExtrema.firstOrNull() ?: windowExtrema.first()
            val lastExt = clusterExtrema.lastOrNull() ?: windowExtrema.last()

            return Pair(firstExt.hazardRunStartDist, lastExt.hazardRunEndDist)
        }

        if (alertType == TerrainAlertType.BLIND_CREST && windowExtrema.isNotEmpty()) {
            val crest = windowExtrema.firstOrNull { it.type == ExtremaType.CREST } ?: windowExtrema.first()
            return Pair(crest.hazardRunStartDist, crest.hazardRunEndDist)
        }

        val threshold = if (alertType == TerrainAlertType.STEEP_CLIMB ||
            alertType == TerrainAlertType.STEEP_DESCENT
        ) {
            STEEP_GRADE_THRESHOLD_PERCENT
        } else {
            HAZARD_GRADE_THRESHOLD_PERCENT
        }

        val firstHazardIdx = segments.indexOfFirst { seg ->
            seg.endDist > clampedCurrentDist + MIN_HORIZONTAL_DELTA_METERS &&
                seg.startDist < windowEndDist - MIN_HORIZONTAL_DELTA_METERS &&
                abs(seg.gradePercent) >= threshold
        }
        if (firstHazardIdx < 0) return null

        var lastHazardIdx = firstHazardIdx
        while (lastHazardIdx + 1 < segments.size &&
            abs(segments[lastHazardIdx + 1].gradePercent) >= threshold
        ) {
            lastHazardIdx++
        }

        return Pair(segments[firstHazardIdx].startDist, segments[lastHazardIdx].endDist)
    }

    private fun sampleLookaheadProfile(
        path: List<LatLngAltitude>,
        cumulativeDistances: DoubleArray,
        segments: List<RouteSegment>,
        clampedCurrentDist: Double,
        windowEndDist: Double,
        hazardBounds: Pair<Double, Double>?
    ): List<ElevationProfilePoint> {
        val span = (windowEndDist - clampedCurrentDist).coerceAtLeast(1.0)
        val samples = ArrayList<ElevationProfilePoint>(SPARKLINE_SAMPLE_STEPS + 1)

        for (step in 0..SPARKLINE_SAMPLE_STEPS) {
            val fraction = step.toDouble() / SPARKLINE_SAMPLE_STEPS.toDouble()
            val offsetMeters = span * fraction
            val sampleDist = (clampedCurrentDist + offsetMeters).coerceAtMost(windowEndDist)
            val interp = PathEngine.interpolatePoint(path, cumulativeDistances, sampleDist)

            val segAtSample = segments.firstOrNull {
                sampleDist >= it.startDist && sampleDist <= it.endDist
            }
            val inHazardBounds = hazardBounds != null &&
                sampleDist >= hazardBounds.first - 0.5 &&
                sampleDist <= hazardBounds.second + 0.5
            val isSteepAtSample = segAtSample != null &&
                abs(segAtSample.gradePercent) >= HAZARD_GRADE_THRESHOLD_PERCENT

            samples.add(
                ElevationProfilePoint(
                    distanceOffsetMeters = offsetMeters,
                    altitudeMeters = interp.altitude,
                    isHazardZone = inHazardBounds || isSteepAtSample
                )
            )
        }
        return samples
    }

    private fun sliceRouteSegment(
        path: List<LatLngAltitude>,
        cumulativeDistances: DoubleArray,
        startDist: Double,
        endDist: Double,
        altitudeBias: Double
    ): List<LatLngAltitude> {
        if (endDist - startDist <= MIN_HORIZONTAL_DELTA_METERS || path.size < 2) {
            return emptyList()
        }
        val coords = mutableListOf<LatLngAltitude>()
        val startInterp = PathEngine.interpolatePoint(path, cumulativeDistances, startDist)
        coords.add(
            LatLngAltitude(
                startInterp.latLng.latitude,
                startInterp.latLng.longitude,
                startInterp.altitude + altitudeBias
            )
        )

        for (i in path.indices) {
            val d = cumulativeDistances[i]
            if (d > startDist + 0.2 && d < endDist - 0.2) {
                val pt = path[i]
                coords.add(LatLngAltitude(pt.latitude, pt.longitude, pt.altitude + altitudeBias))
            }
        }

        val endInterp = PathEngine.interpolatePoint(path, cumulativeDistances, endDist)
        coords.add(
            LatLngAltitude(
                endInterp.latLng.latitude,
                endInterp.latLng.longitude,
                endInterp.altitude + altitudeBias
            )
        )
        return coords
    }

    @JvmStatic
    @JvmOverloads
    fun emptyAlertState(
        currentGradePercent: Double = 0.0,
        lookaheadProfile: List<ElevationProfilePoint> = emptyList()
    ): TerrainAlertState = TerrainAlertState(
        alertType = TerrainAlertType.NONE,
        severity = AlertSeverity.NONE,
        distanceToHazardMeters = 0.0,
        remainingHazardDistanceMeters = 0.0,
        currentGradePercent = currentGradePercent,
        maxPositiveGradePercent = 0.0,
        maxNegativeGradePercent = 0.0,
        cumulativeVerticalChangeMeters = 0.0,
        reversalCount = 0,
        crestCount = 0,
        dipCount = 0,
        oscillationScore = 0.0,
        upcomingExtrema = emptyList(),
        lookaheadProfile = lookaheadProfile,
        hazardPolylineCoordinates = emptyList()
    )
}
