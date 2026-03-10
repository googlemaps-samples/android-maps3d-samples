// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.maps3djava.markers;

import com.google.android.gms.maps3d.model.FlyAroundOptions;
import com.google.android.gms.maps3d.model.PopoverOptions;
import com.google.android.gms.maps3d.Popover;
import org.json.JSONObject;
import org.json.JSONArray;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;
import android.widget.PopupMenu;
import java.util.concurrent.CompletableFuture;
import android.os.Looper;
import android.os.Handler;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import static com.example.maps3d.common.UtilitiesKt.toValidCamera;

import android.view.View;
import android.widget.Button;

import com.example.maps3dcommon.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.CollisionBehavior;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.Glyph;
import com.google.android.gms.maps3d.model.ImageView;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.Marker;
import com.google.android.gms.maps3d.model.MarkerOptions;
import com.google.android.gms.maps3d.model.PinConfiguration;

import java.util.List;
import java.util.ArrayList;

/**
 * Demonstrates the use of different altitude modes for markers in a 3D map.
 * <p>
 * This activity showcases four markers with different altitude modes:
 * - **ABSOLUTE:** Marker at a fixed altitude above sea level.
 * - **RELATIVE_TO_GROUND:** Marker at a fixed height above the ground.
 * - **CLAMP_TO_GROUND:** Marker is always attached to the ground.
 * - **RELATIVE_TO_MESH:** Marker's altitude is relative to the 3D mesh (buildings, terrain features).
 */
public class MarkersActivity extends SampleBaseActivity {

    private Popover activePopover = null;

    @Override
    public final String getTAG() {
        return this.getClass().getSimpleName();
    }

    public final Camera getBerlinCamera() {
        return toValidCamera(new Camera(
                new LatLngAltitude(
                        52.51974795,
                        13.40715553,
                        150.0
                ),
                252.7,
                79.0,
                0.0,
                1500.0
        ));
    }

    private final List<Camera> monsterCameras = new ArrayList<>();
    private final List<Marker> monsterMarkers = new ArrayList<>();
    private final List<String> monsterIds = new ArrayList<>();
    private final List<String> monsterLabels = new ArrayList<>();

    private Handler tourHandler;
    private Runnable tourRunnable;
    private int tourIndex = 0;
    private boolean isTourActive = false;

    @Override
    public final Camera getInitialCamera() {
        return toValidCamera(new Camera(
                new LatLngAltitude(
                        40.748425,
                        -73.985590,
                        348.7),
                22.0,
                80.0,
                0.0,
                1518.0));
    }

    @Override
    public void onMap3DViewReady(GoogleMap3D googleMap3D) {
        super.onMap3DViewReady(googleMap3D);
        
        Log.d(getTAG(), "onMap3DViewReady called");
        
        googleMap3D.setOnMapReadyListener((map) -> {
            Log.w(getTAG(), "on map ready listener fired");
            googleMap3D.setOnMapReadyListener(null);
            onMapReady(googleMap3D);
        });
    }

    private void onMapReady(GoogleMap3D googleMap3D) {
        googleMap3D.setCamera(getInitialCamera());
        googleMap3D.setMapMode(Map3DMode.SATELLITE);

        Button flyBerlinButton = findViewById(R.id.fly_berlin_button);
        if (flyBerlinButton != null) {
            runOnUiThread(() -> flyBerlinButton.setVisibility(View.VISIBLE));
            flyBerlinButton.setOnClickListener(v -> {
                stopMonsterTour(googleMap3D);
                FlyToOptions options = new FlyToOptions(getBerlinCamera(), 4000L);
                googleMap3D.flyCameraTo(options);
            });
        }

        Button flyNycButton = findViewById(R.id.fly_nyc_button);
        if (flyNycButton != null) {
            runOnUiThread(() -> flyNycButton.setVisibility(View.VISIBLE));
            flyNycButton.setOnClickListener(v -> {
                stopMonsterTour(googleMap3D);
                FlyToOptions options = new FlyToOptions(getInitialCamera(), 4000L);
                googleMap3D.flyCameraTo(options);
            });
        }

        Button flyRandomMonsterButton = findViewById(R.id.fly_random_monster_button);
        if (flyRandomMonsterButton != null) {
            runOnUiThread(() -> flyRandomMonsterButton.setVisibility(View.VISIBLE));
            flyRandomMonsterButton.setOnClickListener(v -> {
                stopMonsterTour(googleMap3D);
                if (!monsterCameras.isEmpty()) {
                    Camera randomCamera = monsterCameras.get(new Random().nextInt(monsterCameras.size()));
                    FlyToOptions options = new FlyToOptions(randomCamera, 4000L);
                    googleMap3D.flyCameraTo(options);
                }
            });
            flyRandomMonsterButton.setOnLongClickListener(v -> {
                if (!monsterLabels.isEmpty()) {
                    PopupMenu popup = new PopupMenu(MarkersActivity.this, v);
                    for (int i = 0; i < monsterLabels.size(); i++) {
                        popup.getMenu().add(0, i, i, monsterLabels.get(i));
                    }
                    popup.setOnMenuItemClickListener(item -> {
                        stopMonsterTour(googleMap3D);
                        Camera selectedCamera = monsterCameras.get(item.getItemId());
                        FlyToOptions options = new FlyToOptions(selectedCamera, 4000L);
                        googleMap3D.flyCameraTo(options);
                        return true;
                    });
                    popup.show();
                }
                return true;
            });
        }

        Button tourMonstersButton = findViewById(R.id.tour_monsters_button);
        if (tourMonstersButton != null) {
            runOnUiThread(() -> tourMonstersButton.setVisibility(View.VISIBLE));
            tourMonstersButton.setOnClickListener(v -> {
                if (!monsterCameras.isEmpty() && monsterMarkers.size() == monsterCameras.size() && !isTourActive) {
                    startMonsterTour(googleMap3D);
                }
            });
        }

        Button stopButton = findViewById(R.id.stop_button);
        if (stopButton != null) {
            stopButton.setOnClickListener(v -> stopMonsterTour(googleMap3D));
        }

        googleMap3D.setMap3DClickListener((location, placeId) -> {
            runOnUiThread(() -> {
                if (activePopover != null) {
                    activePopover.remove();
                    activePopover = null;
                }
            });
        });

        // Marker 1: Absolute Altitude
        // This marker is placed at a fixed altitude of 150 meters above sea level.
        addMarkerWithToastListener(googleMap3D,
            new LatLngAltitude(52.519605780912585, 13.406867190588198, 150.0),
            "Absolute (150m)",
            AltitudeMode.ABSOLUTE,
            CollisionBehavior.REQUIRED_AND_HIDES_OPTIONAL
        );

        // Marker 2: Relative to Ground
        // This marker is positioned 50 meters above the ground directly beneath it.
        addMarkerWithToastListener(googleMap3D,
            new LatLngAltitude(52.519882191069016, 13.407410777254293, 50.0),
            "Relative to Ground (50m)",
            AltitudeMode.RELATIVE_TO_GROUND,
            CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY
        );

        // Marker 3: Clamped to Ground
        // This marker is attached to the ground. Its altitude value is effectively
        // ignored
        // for rendering purposes, but it's often set to 0.0 for clarity.
        addMarkerWithToastListener(googleMap3D,
            new LatLngAltitude(52.52027645136134, 13.408271658592406, 0.0),
            "Clamped to Ground",
            AltitudeMode.CLAMP_TO_GROUND,
            CollisionBehavior.REQUIRED
        );

        // Marker 4: Relative to Mesh
        // This marker is placed 10 meters above the 3D mesh, which includes buildings
        // and other structures. This is ideal for placing markers on or relative to 3D
        // objects.
        addMarkerWithToastListener(googleMap3D,
            new LatLngAltitude(52.520835071144226, 13.409426847943774, 10.0),
            "Relative to Mesh (10m)",
            AltitudeMode.RELATIVE_TO_MESH,
            CollisionBehavior.REQUIRED
        );

        MarkerOptions apeOptions = new MarkerOptions();
        apeOptions.setPosition(new LatLngAltitude(40.7484, -73.9857, 100.0));
        apeOptions.setZIndex(1);
        apeOptions.setLabel("Giant Ape / Empire State Building");
        apeOptions.setAltitudeMode(AltitudeMode.RELATIVE_TO_MESH);
        apeOptions.setCollisionBehavior(CollisionBehavior.REQUIRED);
        apeOptions.setExtruded(true);
        apeOptions.setDrawnWhenOccluded(true);

        apeOptions.setStyle(new ImageView(R.drawable.ook));

        Marker apeMarker = googleMap3D.addMarker(apeOptions);
        if (apeMarker != null) {
            apeMarker.setClickListener(() -> {
                TextView textView = new TextView(MarkersActivity.this);
                textView.setText(getString(R.string.monster_ape_blurb));
                textView.setPadding(32, 16, 32, 16);
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.WHITE);

                PopoverOptions popoverOptions = new PopoverOptions();
                popoverOptions.setPositionAnchor(apeMarker);
                popoverOptions.setAltitudeMode(AltitudeMode.RELATIVE_TO_MESH);
                popoverOptions.setContent(textView);
                popoverOptions.setAutoCloseEnabled(true);
                popoverOptions.setAutoPanEnabled(false);

                if (activePopover != null) {
                    runOnUiThread(() -> activePopover.remove());
                }

                Popover popover = googleMap3D.addPopover(popoverOptions);
                activePopover = popover;
                if (popover != null) {
                    runOnUiThread(() -> popover.show());
                }
            });
        }

        Glyph customColorGlyph = Glyph.fromColor(Color.CYAN);
        MarkerOptions customColorOptions = new MarkerOptions();
        customColorOptions.setPosition(new LatLngAltitude(40.7486, -73.9848, 600.0));
        customColorOptions.setExtruded(true);
        customColorOptions.setDrawnWhenOccluded(true);
        customColorOptions.setLabel("Custom Color Pin");
        customColorOptions.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        PinConfiguration.Builder colorPinBuilder = PinConfiguration.builder();
        colorPinBuilder.setBackgroundColor(Color.RED);
        colorPinBuilder.setBorderColor(Color.WHITE);
        colorPinBuilder.setGlyph(customColorGlyph);
        customColorOptions.setStyle(colorPinBuilder.build());

        Marker colorMarker = googleMap3D.addMarker(customColorOptions);
        if (colorMarker != null) {
            colorMarker.setClickListener(
                    () -> MarkersActivity.this.showToast("Clicked on marker: " + colorMarker.getLabel()));
        }

        Glyph textGlyph = Glyph.fromColor(Color.RED);
        textGlyph.setText("NYC\n 🍎 ");
        MarkerOptions textOptions = new MarkerOptions();
        textOptions.setPosition(new LatLngAltitude(40.7482, -73.9862, 600.0));
        textOptions.setExtruded(true);
        textOptions.setDrawnWhenOccluded(true);
        textOptions.setLabel("Custom Text Pin");
        textOptions.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        PinConfiguration.Builder textPinBuilder = PinConfiguration.builder();
        textPinBuilder.setBackgroundColor(Color.YELLOW);
        textPinBuilder.setBorderColor(Color.BLUE);
        textPinBuilder.setGlyph(textGlyph);
        textOptions.setStyle(textPinBuilder.build());

        Marker textMarker = googleMap3D.addMarker(textOptions);
        if (textMarker != null) {
            textMarker.setClickListener(
                    () -> MarkersActivity.this.showToast("Clicked on marker: " + textMarker.getLabel()));
        }

        // Monsters from JSON
        try {
            InputStream is = getAssets().open("monsters.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, StandardCharsets.UTF_8);

            List<com.example.maps3djava.markers.data.Monster> parsedMonsters = com.example.maps3djava.markers.data.MonsterParser
                    .parse(jsonString);

            for (com.example.maps3djava.markers.data.Monster monster : parsedMonsters) {
                Camera cam = new Camera(
                        new LatLngAltitude(
                                monster.latitude,
                                        monster.longitude,
                                monster.altitude),
                        monster.heading,
                        monster.tilt,
                        0.0,
                        monster.range);

                LatLngAltitude markerPos = new LatLngAltitude(
                        monster.markerLatitude,
                        monster.markerLongitude,
                        monster.markerAltitude);

                String monsterId = monster.id;
                String label = monster.label;
                int parsedAltitudeMode = monster.altitudeMode;

                int drawableId = getMonsterDrawableId(monster.drawable);
                if (drawableId != 0) {
                    MarkerOptions monsterOptions = new MarkerOptions();
                    monsterOptions.setPosition(markerPos);
                    monsterOptions.setLabel(label);
                    monsterOptions.setAltitudeMode(parsedAltitudeMode);
                    monsterOptions.setExtruded(true);
                    monsterOptions.setDrawnWhenOccluded(true);
                    monsterOptions.setStyle(new ImageView(drawableId));

                    Marker monsterMarker = googleMap3D.addMarker(monsterOptions);
                    if (monsterMarker != null) {
                        monsterCameras.add(cam);
                        monsterMarkers.add(monsterMarker);
                        monsterIds.add(monsterId);
                        monsterLabels.add(label);

                        setupMarkerClickListener(monsterMarker, getMonsterBlurbResId(monsterId), googleMap3D);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(getTAG(), "Error loading monsters.json", e);
        }

    }

    private void addMarkerWithToastListener(
        GoogleMap3D map,
        LatLngAltitude position,
        String label,
        int altitudeMode,
        int collisionBehavior
    ) {
        MarkerOptions options = new MarkerOptions();
        options.setPosition(position);
        options.setLabel(label);
        options.setAltitudeMode(altitudeMode);
        options.setCollisionBehavior(collisionBehavior);
        options.setExtruded(true);
        options.setDrawnWhenOccluded(true);

        Log.d(getTAG(), "Adding marker with options: "
            + "position=" + options.getPosition()
            + ", label=" + options.getLabel()
            + ", altitudeMode=" + options.getAltitudeMode()
            + ", collisionBehavior=" + options.getCollisionBehavior()
            + ", extruded=" + options.isExtruded()
            + ", drawnWhenOccluded=" + options.isDrawnWhenOccluded()
        );
        Marker marker = map.addMarker(options);
        if (marker != null) {
            Log.d(getTAG(), "Marker was not null!");
            marker.setClickListener(() -> MarkersActivity.this.showToast("Clicked on marker: " + label));
        }
    }

    private void setupMarkerClickListener(Marker marker, int blurbResId, GoogleMap3D googleMap3D) {
        marker.setClickListener(() -> runOnUiThread(() -> {
            if (blurbResId != 0) {
                showMonsterPopover(marker, blurbResId, googleMap3D);
            } else {
                showToast("Clicked on marker: " + marker.getLabel());
            }
        }));
    }

    private void showMonsterPopover(Marker marker, int blurbResId, GoogleMap3D googleMap3D) {
        if (blurbResId != 0) {
            TextView textView = new TextView(MarkersActivity.this);
            textView.setText(getString(blurbResId));
            textView.setPadding(32, 16, 32, 16);
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundColor(Color.WHITE);

            PopoverOptions popOptions = new PopoverOptions();
            popOptions.setPositionAnchor(marker);
            popOptions.setAltitudeMode(marker.getAltitudeMode());
            popOptions.setContent(textView);
            popOptions.setAutoCloseEnabled(true);
            popOptions.setAutoPanEnabled(false);

            Popover newPopover = googleMap3D.addPopover(popOptions);

            if (activePopover != null) {
                activePopover.remove();
            }
            activePopover = newPopover;
            activePopover.show();
        }
    }

    private void startMonsterTour(GoogleMap3D map) {
        isTourActive = true;
        tourIndex = 0;

        runOnUiThread(() -> {
            Button stopButton = findViewById(R.id.stop_button);
            if (stopButton != null)
                stopButton.setVisibility(View.VISIBLE);
            Button tourButton = findViewById(R.id.tour_monsters_button);
            if (tourButton != null)
                tourButton.setVisibility(View.GONE);
        });

        if (tourHandler == null) {
            tourHandler = new Handler(Looper.getMainLooper());
        }

        advanceTour(map);
    }

    private void advanceTour(GoogleMap3D map) {
        if (!isTourActive)
            return;

        if (activePopover != null) {
            activePopover.remove();
            activePopover = null;
        }

        Camera camera = monsterCameras.get(tourIndex);
        Marker marker = monsterMarkers.get(tourIndex);
        String monsterId = monsterIds.get(tourIndex);

        FlyToOptions flyOptions = new FlyToOptions(camera, 4000L);
        CompletableFuture<Void> flyFuture = com.example.maps3djava.common.MapUtils.awaitCameraAnimation(map,
                flyOptions);
        CompletableFuture<Boolean> steadyFuture = com.example.maps3djava.common.MapUtils.awaitMapSteady(map, 5,
                java.util.concurrent.TimeUnit.SECONDS);

        java.util.concurrent.Executor mainThread = this::runOnUiThread;

        CompletableFuture.allOf(flyFuture, steadyFuture).thenComposeAsync(v -> {
            if (!isTourActive)
                return CompletableFuture.completedFuture((Void) null);

            FlyAroundOptions orbitOptions = new FlyAroundOptions(camera, 5000L, 1.0);
            return com.example.maps3djava.common.MapUtils.awaitCameraAnimation(map, orbitOptions);
        }, mainThread).thenAcceptAsync(v -> {
            if (!isTourActive)
                return;

            showMonsterPopover(marker, getMonsterBlurbResId(monsterId), map);

            tourRunnable = () -> {
                tourIndex = (tourIndex + 1) % monsterCameras.size();
                advanceTour(map);
            };
            tourHandler.postDelayed(tourRunnable, 4000);
        }, mainThread);
    }

    private void stopMonsterTour(GoogleMap3D map) {
        isTourActive = false;
        if (tourHandler != null && tourRunnable != null) {
            tourHandler.removeCallbacks(tourRunnable);
        }
        map.stopCameraAnimation();
        map.setCameraAnimationEndListener(null);
        map.setOnMapSteadyListener(null);

        runOnUiThread(() -> {
            Button stopButton = findViewById(R.id.stop_button);
            if (stopButton != null)
                stopButton.setVisibility(View.GONE);
            Button tourButton = findViewById(R.id.tour_monsters_button);
            if (tourButton != null)
                tourButton.setVisibility(View.VISIBLE);
        });
    }

    private int getMonsterDrawableId(String drawableName) {
        switch (drawableName) {
            case "alien":
                return R.drawable.alien;
            case "bigfoot":
                return R.drawable.bigfoot;
            case "frank":
                return R.drawable.frank;
            case "godzilla":
                return R.drawable.godzilla;
            case "mothra":
                return R.drawable.mothra;
            case "mummy":
                return R.drawable.mummy;
            case "nessie":
                return R.drawable.nessie;
            case "yeti":
                return R.drawable.yeti;
            default:
                return 0;
        }
    }

    private int getMonsterBlurbResId(String monsterId) {
        switch (monsterId) {
            case "alien":
                return R.string.monster_alien_blurb;
            case "bigfoot":
                return R.string.monster_bigfoot_blurb;
            case "frank":
                return R.string.monster_frank_blurb;
            case "godzilla":
                return R.string.monster_godzilla_blurb;
            case "mothra":
                return R.string.monster_mothra_blurb;
            case "mummy":
                return R.string.monster_mummy_blurb;
            case "nessie":
                return R.string.monster_nessie_blurb;
            case "yeti":
                return R.string.monster_yeti_blurb;
            default:
                return 0;
        }
    }
}