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

package com.example.maps3djava.storelocator;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.maps3d.common.Map3DSettings;
import com.example.maps3d.common.Map3DSettingsController;
import com.example.maps3d.common.storelocator.StoreAmenity;
import com.example.maps3d.common.storelocator.StoreFlyToCommand;
import com.example.maps3d.common.storelocator.StoreLocation;
import com.example.maps3d.common.storelocator.StoreLocatorUiState;
import com.example.maps3d.common.storelocator.StoreLocatorViewModel;
import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.Popover;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.CollisionBehavior;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.Glyph;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.Marker;
import com.google.android.gms.maps3d.model.MarkerOptions;
import com.google.android.gms.maps3d.model.PinConfiguration;
import com.google.android.gms.maps3d.model.PopoverOptions;
import com.google.android.gms.maps3d.model.PopoverShadow;
import com.google.android.gms.maps3d.model.PopoverStyle;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Java Views implementation of the canonical 2D-to-3D Store Locator sample built in {@link
 * Map3DMode#ROADMAP} and {@link Map3DMode#HYBRID}.
 *
 * <p>Starts in a 2D-friendly low-tilt roadmap overview ({@code tilt = 20.0}, {@code range =
 * 4500.0}) with multi-amenity filter chips and a horizontal store carousel. Selecting a store or
 * tapping "Switch to 3D" transitions the map to {@link Map3DMode#HYBRID}, scales up its marker pin
 * ({@code 1.35f}), displays an info {@link Popover} with store hours and distance, and smoothly
 * flies the camera into a 3D inspection view ({@code tilt = 55.0}, {@code range = 650.0}).
 */
public class StoreLocatorActivity extends SampleBaseActivity {

    private static final String TAG = "StoreLocatorActivity";

    private static final int COLOR_STATUS_OPEN = Color.parseColor("#1E8E3E");

    private StoreLocatorViewModel viewModel;

    private LinearLayout collapsibleContent;
    private MaterialButton collapseToggleButton;
    private MaterialButton toggle2D3DButton;
    private TextView panelTitleText;
    private TextView storeCountText;
    private Chip chipOpenNow;
    private Chip chipDriveThru;
    private Chip chipOpen24h;
    private Chip chipEvCharging;
    private RecyclerView storesRecyclerView;
    private StoreCardAdapter storeAdapter;

    private final Map<String, Marker> activeMarkers = new HashMap<>();
    @Nullable private Popover activePopover = null;
    private List<String> lastRenderedStoreIds = Collections.emptyList();
    @Nullable private String lastRenderedSelectedStoreId = null;
    @Map3DMode private int lastAppliedMapMode = -1;

    private boolean isCollapsed = false;
    private boolean isUpdatingUiFromState = false;

    @NonNull
    @Override
    public String getTAG() {
        return TAG;
    }

    @NonNull
    @Override
    public Camera getInitialCamera() {
        return Map3DSettingsController.getShared().getSettings().getMapMode() != Map3DMode.ROADMAP
                ? StoreLocation.getOVERVIEW_CAMERA_3D()
                : StoreLocation.getOVERVIEW_CAMERA();
    }

    @Override
    @Map3DMode
    protected int getDefaultMapMode() {
        return Map3DMode.ROADMAP;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Map3DSettingsController.getShared().onSampleEntered(getDefaultMapMode());
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StoreLocatorViewModel.class);
        viewModel.syncInitialMapMode(
                Map3DSettingsController.getShared().getSettings().getMapMode());

        View baseScrollView = findViewById(R.id.control_scroll_view);
        if (baseScrollView != null) {
            baseScrollView.setVisibility(View.GONE);
        }

        MaterialToolbar topBar = findViewById(R.id.top_bar);
        if (topBar != null) {
            topBar.setTitle(R.string.feature_title_store_locator);
            topBar.setNavigationOnClickListener(v -> finish());
        }

        ConstraintLayout rootContainer = findViewById(R.id.map_container);
        View controlPanel =
                LayoutInflater.from(this)
                        .inflate(R.layout.control_panel_store_locator, rootContainer, false);
        rootContainer.addView(controlPanel);

        ViewCompat.setOnApplyWindowInsetsListener(
                controlPanel,
                (view, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                    ConstraintLayout.LayoutParams layoutParams =
                            (ConstraintLayout.LayoutParams) view.getLayoutParams();
                    int margin12dp = (int) (12 * getResources().getDisplayMetrics().density);
                    layoutParams.bottomMargin = insets.bottom + margin12dp;
                    view.setLayoutParams(layoutParams);
                    return windowInsets;
                });

        bindViews(controlPanel);
        setupListeners(controlPanel);
        observeViewModel();
    }

    @Override
    protected void onMapReady(@NonNull GoogleMap3D googleMap3D) {
        super.onMapReady(googleMap3D);
        lastRenderedStoreIds = Collections.emptyList();
        lastRenderedSelectedStoreId = null;
        lastAppliedMapMode = -1;
        syncMapMarkersAndCamera(googleMap3D, viewModel.getCurrentState());
    }

    @Override
    protected void onQuickSettingsChanged(@NonNull Map3DSettings settings) {
        super.onQuickSettingsChanged(settings);
        if (viewModel != null) {
            viewModel.setMapMode(settings.getMapMode());
            lastRenderedStoreIds.clear();
            if (googleMap3D != null) {
                syncMapMarkersAndCamera(googleMap3D, viewModel.getCurrentState());
            }
        }
    }

    private void bindViews(View panel) {
        collapsibleContent = panel.findViewById(R.id.card_content);
        collapseToggleButton = panel.findViewById(R.id.btn_collapse);
        toggle2D3DButton = panel.findViewById(R.id.btn_toggle_2d_3d);
        panelTitleText = panel.findViewById(R.id.tv_panel_title);
        storeCountText = panel.findViewById(R.id.tv_store_count);
        chipOpenNow = panel.findViewById(R.id.chip_open_now);
        chipDriveThru = panel.findViewById(R.id.chip_drive_thru);
        chipOpen24h = panel.findViewById(R.id.chip_open_24h);
        chipEvCharging = panel.findViewById(R.id.chip_ev_charging);
        storesRecyclerView = panel.findViewById(R.id.rv_stores);

        storeAdapter = new StoreCardAdapter(store -> viewModel.selectStore(store));
        storesRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        storesRecyclerView.setAdapter(storeAdapter);
    }

    private void setupListeners(View panel) {
        View header = panel.findViewById(R.id.card_header);
        if (header != null) {
            header.setOnClickListener(v -> togglePanelCollapse());
        }
        collapseToggleButton.setOnClickListener(v -> togglePanelCollapse());
        toggle2D3DButton.setOnClickListener(v -> viewModel.toggle2D3DMode());

        chipOpenNow.setOnCheckedChangeListener(
                (btn, checked) -> {
                    if (!isUpdatingUiFromState) {
                        viewModel.toggleAmenity(StoreAmenity.OPEN_NOW);
                    }
                });
        chipDriveThru.setOnCheckedChangeListener(
                (btn, checked) -> {
                    if (!isUpdatingUiFromState) {
                        viewModel.toggleAmenity(StoreAmenity.DRIVE_THRU);
                    }
                });
        chipOpen24h.setOnCheckedChangeListener(
                (btn, checked) -> {
                    if (!isUpdatingUiFromState) {
                        viewModel.toggleAmenity(StoreAmenity.OPEN_24_HOURS);
                    }
                });
        chipEvCharging.setOnCheckedChangeListener(
                (btn, checked) -> {
                    if (!isUpdatingUiFromState) {
                        viewModel.toggleAmenity(StoreAmenity.EV_CHARGING);
                    }
                });
    }

    private void observeViewModel() {
        viewModel
                .getLiveData()
                .observe(
                        this,
                        state -> {
                            renderUiState(state);
                            GoogleMap3D map = this.googleMap3D;
                            if (map != null) {
                                syncMapMarkersAndCamera(map, state);
                            }
                        });
    }

    private void renderUiState(StoreLocatorUiState state) {
        isUpdatingUiFromState = true;
        try {
            panelTitleText.setText(
                    state.is3DMode()
                            ? R.string.store_locator_panel_title_3d
                            : R.string.store_locator_panel_title);
            toggle2D3DButton.setText(
                    state.is3DMode()
                            ? R.string.store_locator_switch_to_2d
                            : R.string.store_locator_switch_to_3d);

            storeCountText.setText(
                    getString(
                            R.string.store_locator_count_format,
                            state.getFilteredStores().size(),
                            state.getAllStores().size()));

            chipOpenNow.setChecked(state.getSelectedAmenities().contains(StoreAmenity.OPEN_NOW));
            chipDriveThru.setChecked(
                    state.getSelectedAmenities().contains(StoreAmenity.DRIVE_THRU));
            chipOpen24h.setChecked(
                    state.getSelectedAmenities().contains(StoreAmenity.OPEN_24_HOURS));
            chipEvCharging.setChecked(
                    state.getSelectedAmenities().contains(StoreAmenity.EV_CHARGING));

            StoreLocation selectedStore = state.getSelectedStore();
            String selectedId = selectedStore != null ? selectedStore.getId() : null;
            storeAdapter.submitStores(state.getFilteredStores(), selectedId);

            if (selectedId != null) {
                List<StoreLocation> filtered = state.getFilteredStores();
                for (int i = 0; i < filtered.size(); i++) {
                    if (selectedId.equals(filtered.get(i).getId())) {
                        storesRecyclerView.smoothScrollToPosition(i);
                        break;
                    }
                }
            }
        } finally {
            isUpdatingUiFromState = false;
        }
    }

    private void syncMapMarkersAndCamera(GoogleMap3D map, StoreLocatorUiState state) {
        if (state.getMapMode() != lastAppliedMapMode) {
            lastAppliedMapMode = state.getMapMode();
            if (Map3DSettingsController.getShared().getSettings().getMapMode()
                    != state.getMapMode()) {
                Map3DSettingsController.getShared().setMapMode(state.getMapMode());
            }
            map.setMapMode(state.getMapMode());
        }

        List<String> currentIds = new ArrayList<>();
        for (StoreLocation store : state.getFilteredStores()) {
            currentIds.add(store.getId());
        }
        StoreLocation selectedStore = state.getSelectedStore();
        String selectedId = selectedStore != null ? selectedStore.getId() : null;

        if (!currentIds.equals(lastRenderedStoreIds)) {
            // Dataset / filters changed: reconcile active markers with filteredStores
            lastRenderedStoreIds = currentIds;
            lastRenderedSelectedStoreId = selectedId;
            reconcileStoreMarkers(map, state.getFilteredStores(), selectedStore);
        } else if (!Objects.equals(selectedId, lastRenderedSelectedStoreId)) {
            // Only selection changed (filters unchanged): update only previously and newly selected markers
            String previousId = lastRenderedSelectedStoreId;
            lastRenderedSelectedStoreId = selectedId;
            updateMarkerSelection(map, state.getAllStores(), previousId, selectedStore);
        }

        StoreFlyToCommand flyToCommand = state.getFlyToCommand();
        if (flyToCommand != null) {
            FlyToOptions flyOptions =
                    new FlyToOptions(
                            flyToCommand.getEndCamera(), flyToCommand.getDurationMillis());
            map.flyCameraTo(flyOptions);
            viewModel.onFlyToCompleted();
        }
    }

    private void reconcileStoreMarkers(
            GoogleMap3D map,
            List<StoreLocation> stores,
            @Nullable StoreLocation selectedStore) {
        Set<String> currentIdSet = new HashSet<>();
        for (StoreLocation store : stores) {
            currentIdSet.add(store.getId());
        }

        // 1. Remove markers no longer in filtered set
        List<String> removedIds = new ArrayList<>();
        for (String id : activeMarkers.keySet()) {
            if (!currentIdSet.contains(id)) {
                removedIds.add(id);
            }
        }
        for (String id : removedIds) {
            Marker marker = activeMarkers.remove(id);
            if (marker != null) {
                marker.remove();
            }
        }

        // 2. Add or update markers for currently filtered stores
        String selectedId = selectedStore != null ? selectedStore.getId() : null;
        for (StoreLocation store : stores) {
            boolean isSelected = store.getId().equals(selectedId);
            Marker existing = activeMarkers.get(store.getId());
            if (existing == null || isSelected) {
                createOrUpdateStoreMarker(map, store, isSelected);
            }
        }

        // 3. Update popover for selected store
        if (activePopover != null) {
            activePopover.remove();
            activePopover = null;
        }
        if (selectedStore != null) {
            Marker marker = activeMarkers.get(selectedStore.getId());
            if (marker != null) {
                showStorePopover(map, marker, selectedStore);
            }
        }
    }

    private void updateMarkerSelection(
            GoogleMap3D map,
            List<StoreLocation> allStores,
            @Nullable String previousSelectedId,
            @Nullable StoreLocation selectedStore) {
        // 1. Revert previously selected marker to unselected styling
        if (previousSelectedId != null && activeMarkers.containsKey(previousSelectedId)) {
            StoreLocation previousStore = null;
            for (StoreLocation s : allStores) {
                if (s.getId().equals(previousSelectedId)) {
                    previousStore = s;
                    break;
                }
            }
            if (previousStore != null) {
                createOrUpdateStoreMarker(map, previousStore, false);
            }
        }

        // 2. Elevate newly selected marker to selected styling and display popover
        if (activePopover != null) {
            activePopover.remove();
            activePopover = null;
        }
        if (selectedStore != null) {
            Marker marker = createOrUpdateStoreMarker(map, selectedStore, true);
            if (marker != null) {
                showStorePopover(map, marker, selectedStore);
            }
        }
    }

    @Nullable
    private Marker createOrUpdateStoreMarker(
            GoogleMap3D map,
            StoreLocation store,
            boolean isSelected) {
        int pinBgColor = store.getPinBackgroundColor(this, isSelected);
        float pinScale =
                isSelected
                        ? StoreLocation.SELECTED_PIN_SCALE
                        : StoreLocation.DEFAULT_PIN_SCALE;

        PinConfiguration pinConfig =
                PinConfiguration.builder()
                        .setScale(pinScale)
                        .setBackgroundColor(pinBgColor)
                        .setBorderColor(Color.WHITE)
                        .setGlyph(Glyph.fromColor(Color.WHITE))
                        .build();

        MarkerOptions options = new MarkerOptions();
        options.setId(store.getId());
        options.setPosition(store.toLatLngAltitude());
        options.setLabel(store.getName());
        options.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
        options.setCollisionBehavior(CollisionBehavior.REQUIRED);
        options.setExtruded(true);
        options.setDrawnWhenOccluded(true);
        options.setSizePreserved(true);
        options.setZIndex(isSelected ? 10 : 1);
        options.setStyle(pinConfig);

        Marker marker = map.addMarker(options);
        if (marker != null) {
            activeMarkers.put(store.getId(), marker);
            marker.setClickListener(() -> runOnUiThread(() -> viewModel.selectStore(store)));
        }
        return marker;
    }

    private void showStorePopover(GoogleMap3D map, Marker anchorMarker, StoreLocation store) {
        LinearLayout contentView = new LinearLayout(this);
        contentView.setOrientation(LinearLayout.VERTICAL);

        TextView titleView = new TextView(this);
        titleView.setText(store.getName());
        titleView.setTextSize(15f);
        titleView.setTextColor(Color.BLACK);
        contentView.addView(titleView);

        TextView addressView = new TextView(this);
        addressView.setText(
                getString(
                        R.string.store_locator_address_distance_format,
                        store.getAddress(),
                        store.getDistanceKm()));
        addressView.setTextSize(12f);
        addressView.setTextColor(Color.DKGRAY);
        contentView.addView(addressView);

        TextView hoursView = new TextView(this);
        hoursView.setText(
                getString(
                        R.string.store_locator_hours_phone_format,
                        store.getHours(),
                        store.getPhone()));
        hoursView.setTextSize(12f);
        hoursView.setTextColor(store.isOpenNow() ? COLOR_STATUS_OPEN : Color.GRAY);
        contentView.addView(hoursView);

        PopoverShadow shadow = new PopoverShadow();
        shadow.setColor(Color.argb(77, 0, 0, 0));
        shadow.setOffsetX(2.0f);
        shadow.setOffsetY(4.0f);
        shadow.setRadius(4.0f);

        PopoverStyle style = new PopoverStyle();
        style.setPadding(16.0f);
        style.setBackgroundColor(Color.WHITE);
        style.setBorderRadius(10.0f);
        style.setShadow(shadow);

        PopoverOptions popoverOptions = new PopoverOptions();
        popoverOptions.setContent(contentView);
        popoverOptions.setPositionAnchor(anchorMarker);
        popoverOptions.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
        popoverOptions.setAutoPanEnabled(false);
        popoverOptions.setAutoCloseEnabled(true);
        popoverOptions.setAnchorOffset(new Point(0, 0));
        popoverOptions.setPopoverStyle(style);

        activePopover = map.addPopover(popoverOptions);
        if (activePopover != null) {
            activePopover.show();
        }
    }

    @Override
    protected void onDestroy() {
        if (activePopover != null) {
            activePopover.remove();
            activePopover = null;
        }
        for (Marker marker : activeMarkers.values()) {
            marker.remove();
        }
        activeMarkers.clear();
        super.onDestroy();
    }

    private void togglePanelCollapse() {
        ConstraintLayout rootContainer = findViewById(R.id.map_container);
        TransitionManager.beginDelayedTransition(rootContainer);
        isCollapsed = !isCollapsed;
        collapsibleContent.setVisibility(isCollapsed ? View.GONE : View.VISIBLE);
        collapseToggleButton.setIconResource(
                isCollapsed ? R.drawable.expand_less_24px : R.drawable.expand_more_24px);
    }

    private interface OnStoreClickListener {
        void onStoreClick(StoreLocation store);
    }

    private static class StoreCardAdapter
            extends RecyclerView.Adapter<StoreCardAdapter.StoreViewHolder> {

        private final OnStoreClickListener onStoreClickListener;
        private List<StoreLocation> stores = Collections.emptyList();
        @Nullable private String selectedStoreId = null;

        StoreCardAdapter(OnStoreClickListener onStoreClickListener) {
            this.onStoreClickListener = onStoreClickListener;
        }

        @android.annotation.SuppressLint("NotifyDataSetChanged")
        void submitStores(List<StoreLocation> newStores, @Nullable String selectedId) {
            this.stores = newStores;
            this.selectedStoreId = selectedId;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_store_locator_card, parent, false);
            return new StoreViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
            StoreLocation store = stores.get(position);
            boolean isSelected = store.getId().equals(selectedStoreId);
            holder.bind(store, isSelected, onStoreClickListener);
        }

        @Override
        public int getItemCount() {
            return stores.size();
        }

        static class StoreViewHolder extends RecyclerView.ViewHolder {
            private final MaterialCardView cardRoot;
            private final TextView nameText;
            private final TextView distanceText;
            private final TextView addressText;
            private final TextView hoursText;
            private final TextView amenitiesText;

            StoreViewHolder(@NonNull View itemView) {
                super(itemView);
                cardRoot = itemView.findViewById(R.id.store_card_root);
                nameText = itemView.findViewById(R.id.tv_store_name);
                distanceText = itemView.findViewById(R.id.tv_store_distance);
                addressText = itemView.findViewById(R.id.tv_store_address);
                hoursText = itemView.findViewById(R.id.tv_store_hours);
                amenitiesText = itemView.findViewById(R.id.tv_store_amenities);
            }

            void bind(
                    StoreLocation store,
                    boolean isSelected,
                    OnStoreClickListener onClickListener) {
                nameText.setText(store.getName());
                distanceText.setText(
                        itemView.getContext()
                                .getString(
                                        R.string.store_locator_distance_format,
                                        store.getDistanceKm()));
                addressText.setText(store.getAddress());
                hoursText.setText(
                        itemView.getContext()
                                .getString(
                                        R.string.store_locator_hours_phone_format,
                                        store.getHours(),
                                        store.getPhone()));
                amenitiesText.setText(store.formattedAmenities());

                float density = itemView.getContext().getResources().getDisplayMetrics().density;
                cardRoot.setStrokeWidth((int) ((isSelected ? 2.5f : 1.0f) * density));
                int selectedStrokeColor =
                        ContextCompat.getColor(
                                itemView.getContext(),
                                com.example.maps3dcommon.R.color.store_pin_selected);
                cardRoot.setStrokeColor(isSelected ? selectedStrokeColor : Color.LTGRAY);
                cardRoot.setOnClickListener(v -> onClickListener.onStoreClick(store));
            }
        }
    }
}
