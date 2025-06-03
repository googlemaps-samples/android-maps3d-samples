package com.example.advancedmaps3dsamples.ainavigator

import org.junit.Test

import org.junit.Assert.*

class FloatToCardinalDirectionTest {
    @Test
    fun toCardinalDirection_convertsCorrectly() {
        assertEquals("N", 0f.toCardinalDirection())
        assertEquals("N", 359f.toCardinalDirection())
        assertEquals("N", 22.4f.toCardinalDirection()) // Edge case, still N
        assertEquals("NE", 22.5f.toCardinalDirection()) // Edge case, becomes NE
        assertEquals("NE", 45f.toCardinalDirection())
        assertEquals("NE", (45f + 22f).toCardinalDirection())
        assertEquals("E", (45f + 23f).toCardinalDirection())
        assertEquals("E", 90f.toCardinalDirection())
        assertEquals("SE", 135f.toCardinalDirection())
        assertEquals("S", 180f.toCardinalDirection())
        assertEquals("SW", 225f.toCardinalDirection())
        assertEquals("W", 270f.toCardinalDirection())
        assertEquals("NW", 315f.toCardinalDirection())
        assertEquals("N", 337.5f.toCardinalDirection()) // Boundary for N from NW
        assertEquals("NW", 337.4f.toCardinalDirection()) // Boundary for NW
    }
}
