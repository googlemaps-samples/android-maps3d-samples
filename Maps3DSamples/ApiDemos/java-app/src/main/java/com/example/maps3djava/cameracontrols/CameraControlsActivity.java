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

package com.example.maps3djava.cameracontrols;

import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;

import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;
import com.example.maps3dcommon.R;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyAroundOptions;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.Polygon;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import android.os.Handler;
import android.os.Looper;

import static com.example.maps3d.common.UtilitiesKt.toValidCamera;
import static com.example.maps3d.common.UtilitiesKt.toCompassDirection;
import static com.example.maps3d.common.UtilitiesKt.wrapIn;



public class CameraControlsActivity extends SampleBaseActivity implements OnMap3DViewReadyCallback {
    @Override
    public final String getTAG() {
        return this.getClass().getSimpleName();
    }

    private TextView cameraStateText;
    private Slider rollSlider;
    private TextView rollSliderLabel;

    private final List<Polygon> restrictionCubeFaces = new ArrayList<>();

    @Override
    public final Camera getInitialCamera() {

        return toValidCamera(new Camera(
                new LatLngAltitude(
                        40.7128,
                        -74.0060,
                        150.0
                ),
                252.7,
                79.0,
                0.0,
                1500.0
        ));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_camera_controls);

        map3DView = findViewById(R.id.map3dView);
        map3DView.onCreate(savedInstanceState);
        map3DView.getMap3DViewAsync(this);

        cameraStateText = findViewById(R.id.camera_state_text);

        rollSlider = findViewById(R.id.roll_slider);
        rollSliderLabel = findViewById(R.id.roll_slider_label);

        updateRollSliderLabel(rollSlider.getValue());

        findViewById(R.id.fly_around).setOnClickListener(view -> flyAroundCurrentCenter());

        findViewById(R.id.fly_to).setOnClickListener(view -> flyToEmpireStateBuilding());

        findViewById(R.id.toggle_restriction).setOnClickListener(view -> {
            MaterialButton button = (MaterialButton) view;

            if (button.isChecked()) {
                button.setText(getText(R.string.camera_remove_restriction));
                if (googleMap3D != null) {
                    googleMap3D.setCameraRestriction(DataModel.nycCameraRestriction);
                }
            } else {
                button.setText(getText(R.string.camera_activate_restriction));
                if (googleMap3D != null) {
                    googleMap3D.setCameraRestriction(null);
                }
            }
        });

        findViewById(R.id.show_restriction).setOnClickListener(view -> {
            MaterialButton button = (MaterialButton) view;

            if (button.isChecked()) {
                button.setText(getText(R.string.camera_hide_restriction));

                for (Polygon polygon : restrictionCubeFaces) {
                    polygon.remove();
                }
                restrictionCubeFaces.clear();

                DataModel.nycPolygonOptions.forEach(polygonOptions -> {
                    if (googleMap3D != null) {
                        Polygon polygon = googleMap3D.addPolygon(polygonOptions);
                        restrictionCubeFaces.add(polygon);
                    }
                });
            } else {
                button.setText(getText(R.string.camera_show_restriction));
                for (Polygon polygon : restrictionCubeFaces) {
                    polygon.remove();
                }
                restrictionCubeFaces.clear();
            }
        });


        ((RadioGroup) findViewById(R.id.map_mode_radio_group)).setOnCheckedChangeListener((group, checkedId) -> {
            if (googleMap3D != null) {
                if (checkedId == R.id.map_mode_hybrid) {
                    googleMap3D.setMapMode(Map3DMode.HYBRID);
                } else if (checkedId == R.id.map_mode_satellite) {
                    googleMap3D.setMapMode(Map3DMode.SATELLITE);
                }
            }
        });

        rollSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                updateRollSliderLabel(value);
                updateMapRoll(value);
            }
        });


        findViewById(R.id.reset_roll_button).setOnClickListener(view -> {
            float resetValue = 0.0f;
            rollSlider.setValue(resetValue);
            updateRollSliderLabel(resetValue);
            updateMapRoll(resetValue);
        });
    }

    private void updateRollSliderLabel(float value) {
        rollSliderLabel.setText(getString(R.string.camera_roll_label_dynamic, value));
    }

    private void updateMapRoll(double newRoll) {
        if (googleMap3D != null && googleMap3D.getCamera() != null) {
            Camera currentCamera = toValidCamera(googleMap3D.getCamera());
            currentCamera.setRoll(newRoll);
            googleMap3D.setCamera(currentCamera);
        }
    }

    /**
     * Called when the [Map3DView] is ready to be used.
     * <p>
     * This callback is triggered when the [GoogleMap3D] object is initialized and ready
     * for interaction. You can use this method to set up map features, add markers,
     * set the camera position, etc.
     *
     * @param googleMap3D The [GoogleMap3D] object representing the 3D map.
     */
    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
        super.onMap3DViewReady(googleMap3D);
        this.googleMap3D = googleMap3D;

        if (googleMap3D.getCamera() != null) {
            updateCameraPosition(googleMap3D.getCamera());
        }

        googleMap3D.setCameraChangedListener(this::updateCameraPosition);

        googleMap3D.setOnMapSteadyListener(isSteady -> {
            if (isSteady) {
                googleMap3D.setOnMapSteadyListener(null);
                new Handler(Looper.getMainLooper()).postDelayed(this::flyToEmpireStateBuilding,
                        TimeUnit.SECONDS.toMillis(2));
            }
        });
    }

    /**
     * Initiates a camera flight animation to the Empire State Building.
     * <p>
     * This function uses the [GoogleMap3D.flyCameraTo] method to move the camera's viewpoint
     * smoothly to the Empire State Building's location. The camera is configured
     * with specific latitude, longitude, altitude, heading, tilt, and range values
     * to provide an engaging view of the landmark.
     * <p>
     * The flight duration is set to 2 seconds.
     * <p>
     * This function assumes that the [googleMap3D] object is initialized and ready for use.
     */
    private void flyToEmpireStateBuilding() {
        if (googleMap3D != null) {
            Camera endCamera = new Camera(
                    new LatLngAltitude(
                            DataModel.EMPIRE_STATE_BUILDING_LATITUDE,
                            DataModel.EMPIRE_STATE_BUILDING_LONGITUDE,
                            212.0
                    ),
                    34.0,
                    67.0,
                    0.0,
                    750.0
            );

            FlyToOptions flyToOptions = new FlyToOptions(endCamera, 2_000);

            googleMap3D.flyCameraTo(flyToOptions);
        }
    }

    /**
     * Initiates a camera fly-around animation around the current camera's center.
     * <p>
     * This function retrieves the current camera position from the map and then
     * starts a fly-around animation using the [GoogleMap3D.flyCameraAround] method. The animation
     * will orbit around the current center point, with the specified duration and number of rounds.
     * <p>
     * If the map or the current camera position is not available, this function
     * will do nothing.
     * <p>
     * The fly-around animation parameters are:
     * - `center`: The current camera position (latitude, longitude, altitude).
     * - `durationInMillis`: The duration of the fly-around animation in milliseconds (5000 ms).
     * - `rounds`: The number of times the camera will orbit around the center (1.0 round).
     */
    private void flyAroundCurrentCenter() {
        if (googleMap3D == null || googleMap3D.getCamera() == null) {
            return;
        }
        Camera camera = toValidCamera(googleMap3D.getCamera());

        FlyAroundOptions flyAroundOptions = new FlyAroundOptions(camera, 5_000, 1.0);

        googleMap3D.flyCameraAround(flyAroundOptions);
    }

    /**
     * Updates the camera state text view with the current camera's position information.
     * <p>
     * This function takes a [Camera] object and extracts its center coordinates (latitude,
     * longitude, altitude), heading, tilt, and range. It then formats these values into a
     * string that is displayed in the `cameraStateText` TextView.
     * <p>
     * It also appends the compass direction based on the camera's heading.
     * <p>
     * The update to the TextView is performed on the main thread using a coroutine.
     *
     * @param camera The [Camera] object representing the current camera position.
     */
    private void updateCameraPosition(@NonNull Camera camera) {
        String nbsp = "\u00A0";

        double heading = camera.getHeading() != null ? camera.getHeading() : 0.0;
        double tilt = camera.getTilt() != null ? camera.getTilt() : 0.0;
        double range = camera.getRange() != null ? camera.getRange() : 0.0;
        double roll = camera.getRoll() != null ? camera.getRoll() : 0.0;


        StringBuilder cameraStateStringBuilder = new StringBuilder();
        // Latitude
        cameraStateStringBuilder.append(getString(R.string.cam_lat_label, camera.getCenter().getLatitude()));
        cameraStateStringBuilder.append(", ");

        // Longitude
        cameraStateStringBuilder.append(getString(R.string.cam_lng_label, camera.getCenter().getLongitude()));
        cameraStateStringBuilder.append(",\n");

        // Altitude
        cameraStateStringBuilder.append(getString( R.string.cam_alt_label, camera.getCenter().getAltitude()));
        cameraStateStringBuilder.append(", ");

        // Heading
        cameraStateStringBuilder.append(getString(R.string.cam_hdg_label, heading));
        String compassString = toCompassDirection(heading);
        cameraStateStringBuilder.append(nbsp).append("(").append(compassString).append(")");
        cameraStateStringBuilder.append(", ");

        // Tilt
        cameraStateStringBuilder.append(getString(R.string.cam_tlt_label, tilt));
        cameraStateStringBuilder.append(", ");

        // Range
        cameraStateStringBuilder.append(getString(R.string.cam_rng_label, range));

        String cameraStateString = cameraStateStringBuilder.toString();

        float resetValue = (float) wrapIn(roll, -180f, 180f);

        new Handler(Looper.getMainLooper()).post(() -> {
            cameraStateText.setText(cameraStateString);
            rollSlider.setValue(resetValue);
            updateRollSliderLabel(resetValue);
        });
    }
}