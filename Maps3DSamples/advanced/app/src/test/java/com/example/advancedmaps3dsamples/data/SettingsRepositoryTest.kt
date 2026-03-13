package com.example.advancedmaps3dsamples.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SettingsRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepository
    private lateinit var testFile: File

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        testFile = File(context.filesDir, "test_datastore.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        // Note: we can't easily inject the mock datastore into SettingsRepository
        // if it uses context.dataStore property delegate inside.
        // For the sake of this test, we assume the repository uses the provided context.
        repository = SettingsRepository(context)
    }

    @After
    fun teardown() {
        testFile.delete()
    }

    @Test
    fun `test default polygon dimensions`() = testScope.runTest {
        assertEquals(SettingsRepository.DEFAULT_POLYGON_WIDTH_MILES, repository.polygonWidthMilesFlow.first())
        assertEquals(SettingsRepository.DEFAULT_POLYGON_HEIGHT_MILES, repository.polygonHeightMilesFlow.first())
    }

    @Test
    fun `test saving polygon dimensions`() = testScope.runTest {
        repository.savePolygonDimensions(10f, 20f)
        assertEquals(10f, repository.polygonWidthMilesFlow.first())
        assertEquals(20f, repository.polygonHeightMilesFlow.first())
    }

    @Test
    fun `test saving polygon center`() = testScope.runTest {
        repository.savePolygonCenter(12.34, 56.78)
        assertEquals(12.34, repository.polygonCenterLatFlow.first(), 0.001)
        assertEquals(56.78, repository.polygonCenterLngFlow.first(), 0.001)
    }

    @Test
    fun `test camera defaults to null`() = testScope.runTest {
        assertEquals(null, repository.cameraFlow.first())
    }

    @Test
    fun `test saving and loading camera`() = testScope.runTest {
        val newCam = camera {
            center = latLngAltitude {
                latitude = 1.0
                longitude = 2.0
                altitude = 3.0
            }
            heading = 4.0
            tilt = 5.0
            roll = 6.0
        }
        
        // SettingsRepository uses context.dataStore delegate internally right now,
        // so to test this properly, the context must be Robolectric Application context.
        repository.saveCamera(newCam)
        
        val loadedCam = repository.cameraFlow.first()
        requireNotNull(loadedCam)
        assertEquals(1.0, loadedCam.center.latitude, 0.0)
        assertEquals(2.0, loadedCam.center.longitude, 0.0)
        assertEquals(3.0, loadedCam.center.altitude, 0.0)
        assertEquals(4.0, loadedCam.heading)
        assertEquals(5.0, loadedCam.tilt)
        assertEquals(6.0, loadedCam.roll)
    }
}
