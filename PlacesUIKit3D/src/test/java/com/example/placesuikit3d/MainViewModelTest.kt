package com.example.placesuikit3d

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MainViewModelTest {

    @Test
    fun `initial state should have null selected place and landmark`() {
        val viewModel = MainViewModel()

        assertThat(viewModel.placeId.value).isNull()
        assertThat(viewModel.selectedLandmark.value).isNull()
    }

    @Test
    fun `landmarks list is not empty on initialization`() {
        val viewModel = MainViewModel()

        assertThat(viewModel.landmarks).isNotEmpty()
    }

    @Test
    fun `setSelectedPlaceId updates placeId state`() {
        val viewModel = MainViewModel()
        val testPlaceId = "ChIJT_test_place_id"

        viewModel.setSelectedPlaceId(testPlaceId)

        assertThat(viewModel.placeId.value).isEqualTo(testPlaceId)
        // selectedLandmark should remain untouched when purely setting place ID
        assertThat(viewModel.selectedLandmark.value).isNull()
    }

    @Test
    fun `selectLandmark updates both selectedLandmark and placeId states`() {
        val viewModel = MainViewModel()
        val landmark = viewModel.landmarks.first()

        viewModel.selectLandmark(landmark)

        assertThat(viewModel.selectedLandmark.value).isEqualTo(landmark)
        assertThat(viewModel.placeId.value).isEqualTo(landmark.id)
    }

    @Test
    fun `setSelectedPlaceId to null clears placeId state`() {
        val viewModel = MainViewModel()
        val landmark = viewModel.landmarks.first()

        viewModel.selectLandmark(landmark)
        assertThat(viewModel.placeId.value).isEqualTo(landmark.id)

        viewModel.setSelectedPlaceId(null)
        
        assertThat(viewModel.placeId.value).isNull()
        // verify selectedLandmark remains the last selected one, just the place details overlay dismissed
        assertThat(viewModel.selectedLandmark.value).isEqualTo(landmark)
    }
}
