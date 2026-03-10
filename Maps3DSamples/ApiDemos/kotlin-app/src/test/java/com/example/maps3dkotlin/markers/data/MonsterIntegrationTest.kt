package com.example.maps3dkotlin.markers.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MonsterIntegrationTest {

    @Test
    fun testParseActualMonstersJson() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val jsonString = context.assets.open("monsters.json").bufferedReader().use { it.readText() }
        val monsters = MonsterParser.parse(jsonString)
        
        // Ensure that at least our 8 original monsters are there
        assertThat(monsters.size).isAtLeast(8)
        
        // Basic check on one of the known monsters
        val mothra = monsters.find { it.id == "mothra" }
        assertThat(mothra).isNotNull()
        assertThat(mothra?.label).isEqualTo("Tokyo Tower Mothra")
    }
}
