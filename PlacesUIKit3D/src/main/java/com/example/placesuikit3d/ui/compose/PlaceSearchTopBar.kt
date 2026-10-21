// Copyright 2026 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.placesuikit3d.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.placesuikit3d.R
import com.google.android.libraries.places.compose.autocomplete.components.PlacesAutocompleteTextField
import com.google.android.libraries.places.compose.autocomplete.models.AutocompletePlace

val DEFAULT_CATEGORIES = listOf(
    "🏛️ Landmarks",
    "☕ Cafes",
    "🍕 Restaurants",
    "🌳 Parks",
    "🏨 Hotels",
    "🛍️ Shopping",
    "🎨 Museums",
)

/**
 * Top Search Bar combining the official Google [PlacesAutocompleteTextField]
 * component from [places-compose] with 3D map exploration, category filter chips,
 * and a dynamic "Search this area" nearby search pill button.
 *
 * Utilizes the official UI search box and autocomplete predictions from [com.google.maps.android:places-compose]
 * while constraining input to 1 line (textFieldMaxLines = 1).
 */
@Composable
fun PlaceSearchTopBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    predictions: List<AutocompletePlace>,
    onPlaceSelected: (AutocompletePlace) -> Unit,
    selectedCategory: String = "🏛️ Landmarks",
    categories: List<String> = DEFAULT_CATEGORIES,
    onCategorySelected: (String) -> Unit = {},
    is3DView: Boolean = false,
    onToggle2D3D: () -> Unit = {},
    showSearchThisArea: Boolean = false,
    onSearchThisAreaClick: () -> Unit = {},
    isLoading: Boolean = false,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column {
                PlacesAutocompleteTextField(
                    searchText = query,
                    predictions = predictions,
                    onQueryChanged = onQueryChange,
                    onSelected = { place ->
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onPlaceSelected(place)
                    },
                    textFieldMaxLines = 1,
                    primaryTextMaxLines = 1,
                    secondaryTextMaxLines = 1,
                    scrollable = true,
                    placeHolderText = stringResource(id = R.string.search_places_hint),
                    modifier = Modifier.fillMaxWidth(),
                )

                AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }

        // Horizontal row of 2D/3D Map Toggle Chip + Category FilterChips
        LazyRow(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "toggle_2d_3d_chip") {
                FilterChip(
                    selected = is3DView,
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onToggle2D3D()
                    },
                    label = {
                        Text(
                            text = if (is3DView) {
                                stringResource(id = R.string.chip_view_3d)
                            } else {
                                stringResource(id = R.string.chip_view_2d)
                            },
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = MaterialTheme.colorScheme.primary,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        enabled = true,
                        selected = is3DView,
                    ),
                    modifier = Modifier.testTag("toggle_2d_3d_chip"),
                )
            }

            items(categories) { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onCategorySelected(category)
                    },
                    label = { Text(text = category) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        enabled = true,
                        selected = isSelected,
                    ),
                )
            }
        }

        // Floating "Search this area" pill button when camera pans/zooms away from last search center
        AnimatedVisibility(
            visible = showSearchThisArea && selectedCategory.isNotBlank(),
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            ElevatedButton(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onSearchThisAreaClick()
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp),
                modifier = Modifier.testTag("search_this_area_button"),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(id = R.string.search_this_area),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
