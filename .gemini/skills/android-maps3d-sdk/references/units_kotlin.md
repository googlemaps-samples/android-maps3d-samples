# Unit Conversions (Kotlin)

These utilities provide type-safe measurements in meters and conversions to other units (feet, miles, kilometers), which are useful for displaying distances in 3D map applications.

## Meters Value Class

A value class to wrap a value representing a measurement in meters, preventing accidental mixing of units.

```kotlin
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.stringResource

const val METERS_PER_FOOT = 3.28084
const val METERS_PER_KILOMETER = 1000
const val FEET_PER_METER = 1 / METERS_PER_FOOT
const val FEET_PER_MILE = 5280
const val MILES_PER_METER = 0.000621371

@Immutable
@JvmInline
value class Meters(val value: Double) : Comparable<Meters> {
    override fun compareTo(other: Meters) = value.compareTo(other.value)
    operator fun minus(other: Meters) = Meters(value = this.value - other.value)
}

@Stable
inline val Number.meters: Meters get() = Meters(value = this.toDouble())

@Stable
inline val Number.m: Meters get() = Meters(value = this.toDouble())

@Stable
inline val Number.km: Meters get() = Meters(value = this.toDouble() * METERS_PER_KILOMETER)

@Stable
inline val Number.feet: Meters get() = Meters(value = this.toDouble() * FEET_PER_METER)

@Stable
inline val Number.miles: Meters get() = Meters(value = this.toDouble() / MILES_PER_METER)

@Stable
inline val Meters.toFeet: Double get() = value * METERS_PER_FOOT

@Stable
inline val Meters.toMeters: Double get() = value

@Stable
inline val Meters.toKilometers: Double get() = value / METERS_PER_KILOMETER

@Stable
inline val Meters.toMiles: Double get() = (value * MILES_PER_METER)

@Stable
fun Meters.plus(other: Meters) = Meters(value = this.value + other.value)
```

## Units Converter (Optional)

If you need to display localized strings, you can use a pattern like this (requires defining corresponding string resources in your project):

```kotlin
data class ValueWithUnitsTemplate(val value: Double, val unitLabel: String)

abstract class UnitsConverter {
    abstract fun toDistanceUnits(meters: Meters): ValueWithUnitsTemplate
    abstract fun toElevationUnits(meters: Meters): ValueWithUnitsTemplate
}

object ImperialUnitsConverter : UnitsConverter() {
    override fun toDistanceUnits(meters: Meters): ValueWithUnitsTemplate {
        return if (meters < 0.25.miles) {
            ValueWithUnitsTemplate(meters.toFeet, "ft")
        } else {
            ValueWithUnitsTemplate(meters.toMiles, "mi")
        }
    }

    override fun toElevationUnits(meters: Meters): ValueWithUnitsTemplate {
        return ValueWithUnitsTemplate(meters.toFeet, "ft")
    }
}

object MetricUnitsConverter : UnitsConverter() {
    override fun toDistanceUnits(meters: Meters): ValueWithUnitsTemplate {
        return if (meters < 1000.meters) {
            ValueWithUnitsTemplate(meters.toMeters, "m")
        } else {
            ValueWithUnitsTemplate(meters.toKilometers, "km")
        }
    }

    override fun toElevationUnits(meters: Meters): ValueWithUnitsTemplate {
        return ValueWithUnitsTemplate(meters.toMeters, "m")
    }
}
```
