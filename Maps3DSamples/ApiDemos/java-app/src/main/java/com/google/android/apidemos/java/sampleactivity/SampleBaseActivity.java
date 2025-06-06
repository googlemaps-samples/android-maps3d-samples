/*
 * Copyright 2023 Google LLC
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

package com.google.android.apidemos.java.sampleactivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.CallSuper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.apidemos.java.utils.JavaCameraUtils; // Placeholder
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.Map3DView;
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.example.maps3dcommon.R; // Assuming common module R file

public abstract class SampleBaseActivity extends AppCompatActivity implements OnMap3DViewReadyCallback {

    protected Map3DView map3DView;
    protected GoogleMap3D googleMap3D;
    protected MaterialButton snapshotButton;
    protected MaterialButton recenterButton;

    protected abstract Camera getInitialCamera();
    protected abstract String getTag();

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_map);

        View rootView = findViewById(R.id.map_container);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                WindowInsetsCompat statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                WindowInsetsCompat navigationBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

                v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop() + statusBarInsets.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom() + navigationBarInsets.bottom
                );
                return WindowInsetsCompat.CONSUMED;
            });
        }

        MaterialToolbar topBar = findViewById(R.id.top_bar);
        if (topBar != null) {
            topBar.setTitle(getTitle());
        }

        map3DView = findViewById(R.id.map3dView);
        if (map3DView != null) {
            map3DView.onCreate(savedInstanceState);
            map3DView.getMap3DViewAsync(this);
        } else {
            Log.e(getTag(), "Map3DView not found in layout!");
        }


        snapshotButton = findViewById(R.id.snapshot_button);
        if (snapshotButton != null) {
            snapshotButton.setOnClickListener(v -> snapshot());
            snapshotButton.setVisibility(View.VISIBLE);
        }

        recenterButton = findViewById(R.id.reset_view_button);
        if (recenterButton != null) {
            recenterButton.setOnClickListener(v -> {
                if (googleMap3D != null) {
                    googleMap3D.flyCameraTo(
                        new FlyToOptions.Builder()
                            .endCamera(getInitialCamera())
                            .durationInMillis(2000)
                            .build()
                    );
                }
            });
            recenterButton.setVisibility(View.VISIBLE);
        }
    }

    @CallSuper
    @Override
    protected void onResume() {
        super.onResume();
        if (map3DView != null) {
            map3DView.onResume();
        }
    }

    @CallSuper
    @Override
    protected void onPause() {
        super.onPause();
        if (map3DView != null) {
            map3DView.onPause();
        }
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (map3DView != null) {
            map3DView.onDestroy();
        }
    }

    @SuppressWarnings("deprecation") // For onLowMemory
    @CallSuper
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (map3DView != null) {
            map3DView.onLowMemory();
        }
    }

    @CallSuper
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (map3DView != null) {
            map3DView.onSaveInstanceState(outState);
        }
    }

    @CallSuper
    @Override
    public void onMap3DViewReady(GoogleMap3D map) {
        this.googleMap3D = map;
        if (this.googleMap3D != null) {
            // It's important that getInitialCamera() does not rely on googleMap3D itself,
            // as it might not be fully initialized or might be null during the first call.
            Camera initialCamera = getInitialCamera();
            if (initialCamera != null) {
                 this.googleMap3D.setCamera(initialCamera);
            } else {
                Log.e(getTag(), "Initial camera is null, map camera not set.");
            }
        } else {
            Log.e(getTag(), "GoogleMap3D instance is null in onMap3DViewReady.");
        }
    }

    @CallSuper
    @Override
    public void onError(Exception exception) {
        Log.e(getTag(), "Error loading map", exception);
        // AppCompatActivity does not have a super.onError(Exception) to call.
        // This method is from OnMap3DViewReadyCallback.
    }

    protected void snapshot() {
        if (googleMap3D != null) {
            Camera camera = googleMap3D.getCamera();
            if (camera != null) {
                // Assuming JavaCameraUtils.toValidCamera will be created
                snapshot(JavaCameraUtils.toValidCamera(camera));
            } else {
                Log.w(getTag(), "Cannot take snapshot, current camera is null.");
            }
        } else {
            Log.w(getTag(), "Cannot take snapshot, GoogleMap3D is null.");
        }
    }

    protected void snapshot(Camera camera) {
        // Assuming JavaCameraUtils.toCameraString will be created
        Log.d(getTag(), JavaCameraUtils.toCameraString(camera));
    }
}
