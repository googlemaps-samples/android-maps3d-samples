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

package com.example.maps3dkotlin.storelocator

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.maps3d.common.Map3DSettings
import com.example.maps3d.common.Map3DSettingsController
import com.example.maps3d.common.storelocator.StoreAmenity
import com.example.maps3d.common.storelocator.StoreLocation
import com.example.maps3d.common.storelocator.StoreLocatorUiState
import com.example.maps3d.common.storelocator.StoreLocatorViewModel
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Popover
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.Glyph
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.pinConfiguration
import com.google.android.gms.maps3d.model.popoverOptions
import com.google.android.gms.maps3d.model.popoverShadow
import com.google.android.gms.maps3d.model.popoverStyle
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

/**
 * Canonical 2D-to-3D Store Locator sample built in [Map3DMode.ROADMAP] and [Map3DMode.HYBRID].
 *
 * Starts in a 2D-friendly low-tilt roadmap overview (`tilt = 20.0`, `range = 4500.0`) with
 * multi-amenity filter chips and a horizontal store carousel. Selecting a store or tapping
 * "Switch to 3D" transitions the map to [Map3DMode.HYBRID], scales up the selected marker pin
 * (`1.35f`), displays an info [Popover] with store hours and distance, and smoothly flies the
 * camera into a 3D inspection view (`tilt = 55.0`, `range = 650.0`).
 */
class StoreLocatorActivity : SampleBaseActivity() {

    override val TAG = "StoreLocatorActivity"

    override val initialCamera: Camera
        get() = if (Map3DSettingsController.shared.getSettings().mapMode != Map3DMode.ROADMAP) {
            StoreLocation.OVERVIEW_CAMERA_3D
        } else {
            StoreLocation.OVERVIEW_CAMERA
        }

    @Map3DMode
    override val defaultMapMode: Int = Map3DMode.ROADMAP

    private val viewModel: StoreLocatorViewModel by viewModels()

    private lateinit var collapsibleContent: LinearLayout
    private lateinit var collapseToggleButton: MaterialButton
    private lateinit var toggle2D3DButton: MaterialButton
    private lateinit var panelTitleText: TextView
    private lateinit var storeCountText: TextView
    private lateinit var chipOpenNow: Chip
    private lateinit var chipDriveThru: Chip
    private lateinit var chipOpen24h: Chip
    private lateinit var chipEvCharging: Chip
    private lateinit var storesRecyclerView: RecyclerView

    private lateinit var storeAdapter: StoreCardAdapter

    private val activeMarkers = mutableMapOf<String, Marker>()
    private var activePopover: Popover? = null
    private var lastRenderedStoreIds: List<String> = emptyList()
    private var lastRenderedSelectedStoreId: String? = null

    @Map3DMode
    private var lastAppliedMapMode: Int = -1

    private var isCollapsed = false
    private var isUpdatingUiFromState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Map3DSettingsController.shared.onSampleEntered(defaultMapMode)
        super.onCreate(savedInstanceState)
        viewModel.syncInitialMapMode(Map3DSettingsController.shared.getSettings().mapMode)

        findViewById<View>(R.id.control_scroll_view)?.visibility = View.GONE

        findViewById<MaterialToolbar>(R.id.top_bar)?.apply {
            setTitle(R.string.feature_title_store_locator)
            setNavigationOnClickListener { finish() }
        }

        val rootContainer = findViewById<ConstraintLayout>(R.id.map_container)
        val controlPanel = LayoutInflater.from(this).inflate(
            R.layout.control_panel_store_locator,
            rootContainer,
            false,
        )
        rootContainer.addView(controlPanel)

        ViewCompat.setOnApplyWindowInsetsListener(controlPanel) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = view.layoutParams as ConstraintLayout.LayoutParams
            val margin12dp = (12 * resources.displayMetrics.density).toInt()
            layoutParams.bottomMargin = insets.bottom + margin12dp
            view.layoutParams = layoutParams
            windowInsets
        }

        bindViews(controlPanel)
        setupListeners()
        observeViewModel()
    }

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        lastRenderedStoreIds = emptyList()
        lastRenderedSelectedStoreId = null
        lastAppliedMapMode = -1
        syncMapMarkersAndCamera(googleMap3D, viewModel.currentState)
    }

    override fun onQuickSettingsChanged(settings: Map3DSettings) {
        super.onQuickSettingsChanged(settings)
        viewModel.setMapMode(settings.mapMode)
    }

    private fun bindViews(panel: View) {
        collapsibleContent = panel.findViewById(R.id.card_content)
        collapseToggleButton = panel.findViewById(R.id.btn_collapse)
        toggle2D3DButton = panel.findViewById(R.id.btn_toggle_2d_3d)
        panelTitleText = panel.findViewById(R.id.tv_panel_title)
        storeCountText = panel.findViewById(R.id.tv_store_count)
        chipOpenNow = panel.findViewById(R.id.chip_open_now)
        chipDriveThru = panel.findViewById(R.id.chip_drive_thru)
        chipOpen24h = panel.findViewById(R.id.chip_open_24h)
        chipEvCharging = panel.findViewById(R.id.chip_ev_charging)
        storesRecyclerView = panel.findViewById(R.id.rv_stores)

        storeAdapter = StoreCardAdapter { store ->
            viewModel.selectStore(store)
        }
        storesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        storesRecyclerView.adapter = storeAdapter
    }

    private fun setupListeners() {
        findViewById<View>(R.id.card_header)?.setOnClickListener {
            togglePanelCollapse()
        }
        collapseToggleButton.setOnClickListener {
            togglePanelCollapse()
        }
        toggle2D3DButton.setOnClickListener {
            viewModel.toggle2D3DMode()
        }

        chipOpenNow.setOnCheckedChangeListener { _, _ ->
            if (!isUpdatingUiFromState) viewModel.toggleAmenity(StoreAmenity.OPEN_NOW)
        }
        chipDriveThru.setOnCheckedChangeListener { _, _ ->
            if (!isUpdatingUiFromState) viewModel.toggleAmenity(StoreAmenity.DRIVE_THRU)
        }
        chipOpen24h.setOnCheckedChangeListener { _, _ ->
            if (!isUpdatingUiFromState) viewModel.toggleAmenity(StoreAmenity.OPEN_24_HOURS)
        }
        chipEvCharging.setOnCheckedChangeListener { _, _ ->
            if (!isUpdatingUiFromState) viewModel.toggleAmenity(StoreAmenity.EV_CHARGING)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderUiState(state)
                    val map = googleMap3D
                    if (map != null) {
                        syncMapMarkersAndCamera(map, state)
                    }
                }
            }
        }
    }

    private fun renderUiState(state: StoreLocatorUiState) {
        isUpdatingUiFromState = true
        try {
            panelTitleText.setText(
                if (state.is3DMode) {
                    R.string.store_locator_panel_title_3d
                } else {
                    R.string.store_locator_panel_title
                },
            )
            toggle2D3DButton.setText(
                if (state.is3DMode) {
                    R.string.store_locator_switch_to_2d
                } else {
                    R.string.store_locator_switch_to_3d
                },
            )

            storeCountText.text = getString(
                R.string.store_locator_count_format,
                state.filteredStores.size,
                state.allStores.size,
            )

            chipOpenNow.isChecked = StoreAmenity.OPEN_NOW in state.selectedAmenities
            chipDriveThru.isChecked = StoreAmenity.DRIVE_THRU in state.selectedAmenities
            chipOpen24h.isChecked = StoreAmenity.OPEN_24_HOURS in state.selectedAmenities
            chipEvCharging.isChecked = StoreAmenity.EV_CHARGING in state.selectedAmenities

            val selectedStore = state.selectedStore
            storeAdapter.submitStores(state.filteredStores, selectedStore?.id)
            if (selectedStore != null) {
                val index = state.filteredStores.indexOfFirst { it.id == selectedStore.id }
                if (index >= 0) {
                    storesRecyclerView.smoothScrollToPosition(index)
                }
            }
        } finally {
            isUpdatingUiFromState = false
        }
    }

    private fun syncMapMarkersAndCamera(map: GoogleMap3D, state: StoreLocatorUiState) {
        if (state.mapMode != lastAppliedMapMode) {
            lastAppliedMapMode = state.mapMode
            if (Map3DSettingsController.shared.getSettings().mapMode != state.mapMode) {
                Map3DSettingsController.shared.setMapMode(state.mapMode)
            }
            map.setMapMode(state.mapMode)
        }

        val currentIds = state.filteredStores.map { it.id }
        val selectedStore = state.selectedStore
        val selectedId = selectedStore?.id

        if (currentIds != lastRenderedStoreIds) {
            // Dataset / filters changed: reconcile active markers with filteredStores
            lastRenderedStoreIds = currentIds
            lastRenderedSelectedStoreId = selectedId
            reconcileStoreMarkers(map, state.filteredStores, selectedStore)
        } else if (selectedId != lastRenderedSelectedStoreId) {
            // Only selection changed (filters unchanged): update only previously and newly selected markers
            val previousId = lastRenderedSelectedStoreId
            lastRenderedSelectedStoreId = selectedId
            updateMarkerSelection(map, state.allStores, previousId, selectedStore)
        }

        val flyToCommand = state.flyToCommand
        if (flyToCommand != null) {
            map.flyCameraTo(
                flyToOptions {
                    endCamera = flyToCommand.endCamera
                    durationInMillis = flyToCommand.durationMillis
                },
            )
            viewModel.onFlyToCompleted()
        }
    }

    private fun reconcileStoreMarkers(
        map: GoogleMap3D,
        stores: List<StoreLocation>,
        selectedStore: StoreLocation?,
    ) {
        val currentIdSet = stores.map { it.id }.toSet()

        // 1. Remove markers no longer in filtered set
        val removedIds = activeMarkers.keys - currentIdSet
        for (id in removedIds) {
            activeMarkers.remove(id)?.remove()
        }

        // 2. Add or update markers for currently filtered stores
        val selectedId = selectedStore?.id
        for (store in stores) {
            val isSelected = store.id == selectedId
            val existing = activeMarkers[store.id]
            if (existing == null || isSelected) {
                createOrUpdateStoreMarker(map, store, isSelected)
            }
        }

        // 3. Update popover for selected store
        activePopover?.remove()
        activePopover = null
        if (selectedStore != null) {
            activeMarkers[selectedStore.id]?.let { marker ->
                showStorePopover(map, marker, selectedStore)
            }
        }
    }

    private fun updateMarkerSelection(
        map: GoogleMap3D,
        allStores: List<StoreLocation>,
        previousSelectedId: String?,
        selectedStore: StoreLocation?,
    ) {
        // 1. Revert previously selected marker to unselected styling
        if (previousSelectedId != null && activeMarkers.containsKey(previousSelectedId)) {
            val previousStore = allStores.find { it.id == previousSelectedId }
            if (previousStore != null) {
                createOrUpdateStoreMarker(map, previousStore, isSelected = false)
            }
        }

        // 2. Elevate newly selected marker to selected styling and display popover
        activePopover?.remove()
        activePopover = null
        if (selectedStore != null) {
            val marker = createOrUpdateStoreMarker(map, selectedStore, isSelected = true)
            if (marker != null) {
                showStorePopover(map, marker, selectedStore)
            }
        }
    }

    private fun createOrUpdateStoreMarker(
        map: GoogleMap3D,
        store: StoreLocation,
        isSelected: Boolean,
    ): Marker? {
        val pinBgColor = when {
            isSelected -> "#F4B400".toColorInt()
            store.isOpenNow -> "#1A73E8".toColorInt()
            else -> "#757575".toColorInt()
        }
        val pinScale = if (isSelected) {
            StoreLocation.SELECTED_PIN_SCALE
        } else {
            StoreLocation.DEFAULT_PIN_SCALE
        }

        val marker = map.addMarker(
            markerOptions {
                id = store.id
                position = store.toLatLngAltitude()
                label = store.name
                altitudeMode = AltitudeMode.CLAMP_TO_GROUND
                collisionBehavior = CollisionBehavior.REQUIRED
                isExtruded = true
                isDrawnWhenOccluded = true
                zIndex = if (isSelected) 10 else 1
                setStyle(
                    pinConfiguration {
                        scale = pinScale
                        backgroundColor = pinBgColor
                        borderColor = Color.WHITE
                        setGlyph(Glyph.fromColor(Color.WHITE))
                    },
                )
            },
        )

        if (marker != null) {
            activeMarkers[store.id] = marker
            marker.setClickListener {
                runOnUiThread {
                    viewModel.selectStore(store)
                }
            }
        }
        return marker
    }


    private fun showStorePopover(map: GoogleMap3D, anchorMarker: Marker, store: StoreLocation) {
        val contentView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(
                TextView(this@StoreLocatorActivity).apply {
                    text = store.name
                    textSize = 15f
                    setTextColor(Color.BLACK)
                },
            )
            addView(
                TextView(this@StoreLocatorActivity).apply {
                    text = getString(
                        R.string.store_locator_address_distance_format,
                        store.address,
                        store.distanceKm,
                    )
                    textSize = 12f
                    setTextColor(Color.DKGRAY)
                },
            )
            addView(
                TextView(this@StoreLocatorActivity).apply {
                    text = getString(
                        R.string.store_locator_hours_phone_format,
                        store.hours,
                        store.phone,
                    )
                    textSize = 12f
                    setTextColor(if (store.isOpenNow) "#1E8E3E".toColorInt() else Color.GRAY)
                },
            )
        }

        val popover = map.addPopover(
            popoverOptions {
                content = contentView
                positionAnchor = anchorMarker
                altitudeMode = AltitudeMode.CLAMP_TO_GROUND
                autoPanEnabled = false
                autoCloseEnabled = true
                anchorOffset = Point(0, 0)
                popoverStyle = popoverStyle {
                    padding = 16.0f
                    backgroundColor = Color.WHITE
                    borderRadius = 10.0f
                    shadow = popoverShadow {
                        color = Color.argb(77, 0, 0, 0)
                        offsetX = 2.0f
                        offsetY = 4.0f
                        radius = 4.0f
                    }
                }
            },
        )
        activePopover = popover
        popover.show()
    }

    override fun onDestroy() {
        activePopover?.remove()
        activePopover = null
        activeMarkers.values.forEach { it.remove() }
        activeMarkers.clear()
        super.onDestroy()
    }

    private fun togglePanelCollapse() {
        val rootContainer = findViewById<ConstraintLayout>(R.id.map_container)
        TransitionManager.beginDelayedTransition(rootContainer)
        isCollapsed = !isCollapsed
        collapsibleContent.visibility = if (isCollapsed) View.GONE else View.VISIBLE
        collapseToggleButton.setIconResource(
            if (isCollapsed) R.drawable.expand_less_24px else R.drawable.expand_more_24px,
        )
    }

    private class StoreCardAdapter(
        private val onStoreClick: (StoreLocation) -> Unit,
    ) : RecyclerView.Adapter<StoreCardAdapter.StoreViewHolder>() {

        private var stores: List<StoreLocation> = emptyList()
        private var selectedStoreId: String? = null

        @android.annotation.SuppressLint("NotifyDataSetChanged")
        fun submitStores(newStores: List<StoreLocation>, selectedId: String?) {
            stores = newStores
            selectedStoreId = selectedId
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_store_locator_card,
                parent,
                false,
            )
            return StoreViewHolder(view)
        }

        override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
            val store = stores[position]
            holder.bind(store, store.id == selectedStoreId, onStoreClick)
        }

        override fun getItemCount(): Int = stores.size

        class StoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val cardRoot: MaterialCardView = itemView.findViewById(R.id.store_card_root)
            private val nameText: TextView = itemView.findViewById(R.id.tv_store_name)
            private val distanceText: TextView = itemView.findViewById(R.id.tv_store_distance)
            private val addressText: TextView = itemView.findViewById(R.id.tv_store_address)
            private val hoursText: TextView = itemView.findViewById(R.id.tv_store_hours)
            private val amenitiesText: TextView = itemView.findViewById(R.id.tv_store_amenities)

            fun bind(
                store: StoreLocation,
                isSelected: Boolean,
                onClick: (StoreLocation) -> Unit,
            ) {
                val context = itemView.context
                nameText.text = store.name
                distanceText.text = context.getString(
                    R.string.store_locator_distance_format,
                    store.distanceKm,
                )
                addressText.text = store.address
                hoursText.text = context.getString(
                    R.string.store_locator_hours_phone_format,
                    store.hours,
                    store.phone,
                )
                amenitiesText.text = store.formattedAmenities()

                // Highlight selected store card border with canonical Google yellow (#F4B400)
                val density = context.resources.displayMetrics.density
                cardRoot.strokeWidth = ((if (isSelected) 2.5f else 1f) * density).toInt()
                cardRoot.strokeColor = if (isSelected) {
                    "#F4B400".toColorInt()
                } else {
                    Color.LTGRAY
                }
                cardRoot.setOnClickListener { onClick(store) }
            }
        }
    }
}
