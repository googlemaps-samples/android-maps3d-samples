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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.Map3DView;
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;
import com.example.maps3dcommon.R;

/**
 * `HelloMapActivity` is an Android activity that demonstrates the usage of the `Map3DView`.  This
 * is close the minimal activity.  It inflates the `activity_hello_map.xml` layout file and
 * demonstrates how to initialize the `Map3DView` and get a `GoogleMap3D` reference.
 */
public class HelloMapActivity extends Activity implements OnMap3DViewReadyCallback {
    private final String TAG = this.getClass().getSimpleName();
    private Map3DView map3DView;
    private GoogleMap3D googleMap3D = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_map);

        View rootView = findViewById(R.id.map_container);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            Insets navigationBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            view.setPadding(
                    view.getPaddingLeft(),
                    view.getPaddingTop() + statusBarInsets.top,
                    view.getPaddingRight(),
                    view.getPaddingBottom() + navigationBarInsets.bottom
            );

            return WindowInsetsCompat.CONSUMED;
        });

        map3DView = findViewById(R.id.map3dView);
        map3DView.onCreate(savedInstanceState);
        map3DView.getMap3DViewAsync(this);
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
    }

    @Override
    public void onError(@NonNull Exception error) {
        Log.e(TAG, "Error loading map", error);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map3DView != null) {
            map3DView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map3DView != null) {
            map3DView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (map3DView != null) {
            map3DView.onDestroy();
        }
    }

    @Deprecated
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (map3DView != null) {
            map3DView.onLowMemory();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (map3DView != null) {
            map3DView.onSaveInstanceState(outState);
        }
    }
}