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

package com.example.maps3djava.sampleactivity;


import static com.example.maps3d.common.UtilitiesKt.toCameraString;
import static com.example.maps3d.common.UtilitiesKt.toValidCamera;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.maps3dcommon.R;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.Map3DView;
import com.google.android.gms.maps3d.OnCameraChangedListener;
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Base activity for sample map activities.
 * <p>
 * This abstract class provides common functionality and setup for activities that display
 * a 3D map using the Google Maps 3D API. It handles lifecycle events for the [Map3DView],
 * manages the [GoogleMap3D] instance, and includes utility buttons for taking snapshots
 * and recentering the map.
 * <p>
 * Subclasses must implement:
 * - [initialCamera]: The initial camera position and orientation for the map.
 * - [TAG]: A unique tag for logging purposes.
 * <p>
 * Subclasses can optionally override the following methods (with `@CallSuper` annotation):
 * - [onCreate] : to extend the base behavior.
 * - [onResume]: to extend the base behavior.
 * - [onPause]: to extend the base behavior.
 * - [onDestroy]: to extend the base behavior.
 * - [onSaveInstanceState]: to extend the base behavior.
 * - [onMap3DViewReady]: to extend the base behavior.
 * - [onError]: to extend the base behavior.
 * <p>
 * The activity layout includes a [Map3DView], a snapshot button, and a recenter button.
 */
public abstract class SampleBaseActivity extends Activity implements OnMap3DViewReadyCallback {
    protected Map3DView map3DView;
    protected GoogleMap3D googleMap3D;

    protected MaterialButton snapshotButton;
    protected MaterialButton recenterButton;

    public abstract Camera getInitialCamera();
    public abstract String getTAG();

    private OnCameraChangedListener cameraChangedListener;

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This tells the system that the app will handle drawing behind the system bars.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_common_map);
        View rootView = findViewById(R.id.map_container);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply the insets as padding to the view.
            // This will push the content down from behind the status bar and up from
            // behind the navigation bar.
            v.setPadding(
                insets.left,
                insets.top,
                insets.right,
                insets.bottom
            );

            // Return CONSUMED to signal that we've handled the insets.
            return WindowInsetsCompat.CONSUMED;
        });

        ((MaterialToolbar) findViewById(R.id.top_bar)).setTitle(getTitle());

        map3DView = findViewById(R.id.map3dView);
        map3DView.onCreate(savedInstanceState);
        map3DView.getMap3DViewAsync(this);

        snapshotButton = findViewById(R.id.snapshot_button);
        snapshotButton.setOnClickListener(v -> snapshot());
        snapshotButton.setVisibility(View.VISIBLE);

        recenterButton = findViewById(R.id.reset_view_button);
        recenterButton.setOnClickListener(v -> {
            if (googleMap3D != null) {
                googleMap3D.flyCameraTo(
                        new FlyToOptions(getInitialCamera(), 2000)
                );
            }
        });
        recenterButton.setVisibility(View.VISIBLE);
    }

    @CallSuper
    @Override
    protected void onResume() {
        super.onResume();
        map3DView.onResume();
    }

    @CallSuper
    @Override
    protected void onPause() {
        super.onPause();
        map3DView.onPause();
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
        map3DView.onDestroy();
        if (googleMap3D != null && cameraChangedListener != null) {
            googleMap3D.setCameraChangedListener(null);
        }
    }

    @Deprecated
    @CallSuper
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map3DView.onLowMemory();
    }

    @CallSuper
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        map3DView.onSaveInstanceState(outState);
    }

    /**
     * Called when the [Map3DView] is ready to be used.
     * <p>
     * This method is called after the [Map3DView] has been initialized and is ready to receive
     * commands. It sets the internal [googleMap3D] reference and sets the initial camera
     * position and orientation.
     *
     * @param googleMap3D The [GoogleMap3D] instance that is ready to be used.
     */
    @CallSuper
    @Override
    public void onMap3DViewReady(GoogleMap3D googleMap3D) {
        this.googleMap3D = googleMap3D;

        googleMap3D.setCamera(getInitialCamera());

        // Initialize and set the camera changed listener
        cameraChangedListener = cameraPosition -> {
        };
        googleMap3D.setCameraChangedListener(cameraChangedListener);

    }

    @CallSuper
    @Override
    public void onError(@NonNull Exception error) {
        Log.e(getTAG(), "Error loading map", error);
    }

    /**
     * Captures a snapshot of the current camera position and logs it.
     * <p>
     * This function retrieves the current camera from the [googleMap3D] instance,
     * converts it to a string representation using [toCameraString], and then logs
     * the string to the console using [Log.d]. This is useful for debugging
     * and understanding the current state of the map's camera.
     * <p>
     * The output may be pasted as code to create a camera object with the same location and
     * orientation.
     */
    protected void snapshot() {
        if (googleMap3D != null) {
            Camera camera = googleMap3D.getCamera();
            if (camera != null) {
                snapshot(toValidCamera(camera));
            }
        }
    }

    protected void snapshot(Camera camera) {
        Log.d(getTAG(), toCameraString(camera));
    }

    protected void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}