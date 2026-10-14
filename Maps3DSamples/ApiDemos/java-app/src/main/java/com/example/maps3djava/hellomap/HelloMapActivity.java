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

package com.example.maps3djava.hellomap;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.maps3d.common.Map3DQuickSettingsButton;
import com.example.maps3d.common.Map3DSettingsController;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.Map3DView;
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;
import com.example.maps3dcommon.R;
import com.google.android.gms.maps3d.OnMapReadyListener;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;

/**
 * `HelloMapActivity` is an Android activity that demonstrates the usage of the `Map3DView`.  This
 * is close the minimal activity.  It inflates the `activity_hello_map.xml` layout file and
 * demonstrates how to initialize the `Map3DView` and get a `GoogleMap3D` reference.
 */
public class HelloMapActivity extends AppCompatActivity implements OnMap3DViewReadyCallback, OnMapReadyListener {
    private final String TAG = this.getClass().getSimpleName();
    private Map3DView map3DView;
    private GoogleMap3D googleMap3D = null;
    private boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        enableImmersiveMode();
        setContentView(R.layout.activity_hello_map);

        map3DView = findViewById(R.id.map3dView);
        map3DView.onCreate(savedInstanceState);
        map3DView.getMap3DViewAsync(this);
    }

    private void enableImmersiveMode() {
        WindowInsetsControllerCompat insetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            insetsController.hide(WindowInsetsCompat.Type.systemBars());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enableImmersiveMode();
        }
    }

    /**
     * Called when the [Map3DView] is ready to be used.
     *
     * This callback is triggered when the [GoogleMap3D] object is initialized and ready
     * for interaction. You can use this method to set up map features, add markers,
     * set the camera position, etc.
     *
     * @param googleMap3D The [GoogleMap3D] object representing the 3D map.
     */
    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
        this.googleMap3D = googleMap3D;

        googleMap3D.setOnMapReadyListener(this);

        // Workaround for bug where onMapReady is not called on reused instances.
        // Call initialization after a short delay.
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                initializeMap();
            }
        }, 2000);
    }

    @Override
    public void onMapReady(double v) {
        initializeMap();
    }

    private void initializeMap() {
        if (googleMap3D == null || isInitialized) return;
        
        isInitialized = true;
        
        Log.i(TAG, "Initializing map position to Delicate Arch");

        Map3DSettingsController.getShared().onSampleEntered(Map3DMode.HYBRID);
        Map3DSettingsController.getShared().getSettings().applyTo(googleMap3D);
        Map3DQuickSettingsButton quickSettings = findViewById(R.id.quick_settings_button);
        if (quickSettings != null) {
            quickSettings.setSettingsController(Map3DSettingsController.getShared());
            quickSettings.setSampleDefaultMapMode(Map3DMode.HYBRID);
            quickSettings.attachMap(googleMap3D);
        }
        
        LatLngAltitude center = new LatLngAltitude(38.743502, -109.499374, 1467.0);
        Camera camera = new Camera(center, 349.6, 58.1, 0.0, 138.2);
        
        googleMap3D.setCamera(camera);
    }

    @Override
    public void onError(@NonNull Exception error) {
        Log.e(TAG, "Error loading map", error);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableImmersiveMode();
        map3DView.onResume();
        if (googleMap3D != null) {
            Map3DSettingsController.getShared().getSettings().applyTo(googleMap3D);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        map3DView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map3DView.onDestroy();
    }

    @Deprecated
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map3DView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        map3DView.onSaveInstanceState(outState);
    }
}